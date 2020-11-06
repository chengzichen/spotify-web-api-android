package com.pghaz.spotify.webapi.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.annotation.WorkerThread
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import net.openid.appauth.*
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.ClientAuthentication.UnsupportedAuthenticationMethod
import net.openid.appauth.browser.BrowserMatcher
import net.openid.appauth.browser.BrowserWhitelist
import net.openid.appauth.browser.VersionedBrowserMatcher
import okio.Okio
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class SpotifyAuthorizationClient private constructor(context: Context, clientId: String,
                                                     redirectUri: String, scopes: Array<String?>,
                                                     @ColorInt colorInt: Int,
                                                     private var fetchUserAfterAuthorization: Boolean) {
    companion object {
        private const val TAG = "SpotifyAuthClient"

        private const val MARKET_VIEW_PATH = "market://"
        private const val MARKET_SCHEME = "market"
        private const val MARKET_PATH = "details"

        private const val PLAY_STORE_SCHEME = "https"
        private const val PLAY_STORE_AUTHORITY = "play.google.com"
        private const val PLAY_STORE_PATH = "store/apps/details"

        private const val SPOTIFY_ID = "com.spotify.music"
        private const val SPOTIFY_SDK = "spotify-sdk"
        private const val ANDROID_SDK = "android-sdk"
        private const val DEFAULT_CAMPAIGN = "android-sdk"

        internal object PlayStoreParams {
            const val ID = "id"
            const val REFERRER = "referrer"
            const val UTM_SOURCE = "utm_source"
            const val UTM_MEDIUM = "utm_medium"
            const val UTM_CAMPAIGN = "utm_campaign"
        }

        /**
         * Opens Spotify in the Play Store or browser.
         *
         * @param contextActivity The activity that should start the intent to open the download page.
         */
        fun openDownloadSpotifyActivity(contextActivity: Activity) {
            openDownloadSpotifyActivity(contextActivity, DEFAULT_CAMPAIGN)
        }

        /**
         * Opens Spotify in the Play Store or browser.
         *
         * @param contextActivity The activity that should start the intent to open the download page.
         * @param campaign A Spotify-provided campaign ID. `DEFAULT_CAMPAIGN` if not provided.
         */
        fun openDownloadSpotifyActivity(contextActivity: Activity, campaign: String?) {
            val uriBuilder = Uri.Builder()

            if (isAvailable(contextActivity, Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_VIEW_PATH)))) {
                uriBuilder.scheme(MARKET_SCHEME)
                        .appendPath(MARKET_PATH)
            } else {
                uriBuilder.scheme(PLAY_STORE_SCHEME)
                        .authority(PLAY_STORE_AUTHORITY)
                        .appendEncodedPath(PLAY_STORE_PATH)
            }

            uriBuilder.appendQueryParameter(PlayStoreParams.ID, SPOTIFY_ID)

            val referrerBuilder = Uri.Builder()
            referrerBuilder.appendQueryParameter(PlayStoreParams.UTM_SOURCE, SPOTIFY_SDK)
                    .appendQueryParameter(PlayStoreParams.UTM_MEDIUM, ANDROID_SDK)

            if (TextUtils.isEmpty(campaign)) {
                referrerBuilder.appendQueryParameter(PlayStoreParams.UTM_CAMPAIGN, DEFAULT_CAMPAIGN)
            } else {
                referrerBuilder.appendQueryParameter(PlayStoreParams.UTM_CAMPAIGN, campaign)
            }

            uriBuilder.appendQueryParameter(PlayStoreParams.REFERRER, referrerBuilder.build().encodedQuery)

            contextActivity.startActivity(Intent(Intent.ACTION_VIEW, uriBuilder.build()))
        }

        private fun isAvailable(context: Context, intent: Intent): Boolean {
            val packageManager = context.packageManager
            val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            return list.size > 0
        }

        fun isSpotifyInstalled(context: Context): Boolean {
            val packageManager: PackageManager = context.packageManager
            return try {
                packageManager.getPackageInfo(SPOTIFY_ID, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    class Builder(clientId: String?, redirectUri: String?) {

        private var mClientId: String
        private var mRedirectUri: String

        private var mScopes: Array<String?> = emptyArray()
        private var mCustomTabColor: Int = Color.BLACK
        private var mFetchUserAfterAuthorization: Boolean = false

        init {
            requireNotNull(clientId) { "Client ID can't be null" }
            require(!(redirectUri == null || redirectUri.isEmpty())) { "Redirect URI can't be null or empty" }
            mClientId = clientId
            mRedirectUri = redirectUri
        }

        fun setScopes(scopes: Array<String?>): Builder {
            mScopes = scopes
            return this
        }

        fun setCustomTabColor(@ColorInt colorInt: Int): Builder {
            mCustomTabColor = colorInt
            return this
        }

        fun setFetchUserAfterAuthorization(fetchUserAfterAuthorization: Boolean): Builder {
            mFetchUserAfterAuthorization = fetchUserAfterAuthorization
            return this
        }

        fun build(context: Context): SpotifyAuthorizationClient {
            return SpotifyAuthorizationClient(context, mClientId, mRedirectUri, mScopes,
                    mCustomTabColor, mFetchUserAfterAuthorization)
        }
    }

    private val mClientId = AtomicReference<String>()
    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()

    private var mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var mAuthIntentLatch = CountDownLatch(1)
    private var handler: Handler? = Handler(Looper.getMainLooper())

    private val mGson: Gson = Gson()
    private val mAuthStateManager: AuthStateManager
    private val mConfiguration: Configuration
    private lateinit var mAuthService: AuthorizationService

    private val mBrowserMatcher: BrowserMatcher = BrowserWhitelist(
            VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
            VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB)

    private var authorizationCallbacks: ArrayList<SpotifyAuthorizationCallback.Authorize> = ArrayList()
    private var refreshTokenCallbacks: ArrayList<SpotifyAuthorizationCallback.RefreshToken> = ArrayList()

    private var requestCode = -42
    private var mIsDebug: Boolean = false

    init {
        mAuthStateManager = AuthStateManager.getInstance(context, mGson)
        mConfiguration = Configuration.getInstance(context, clientId, redirectUri, scopes, colorInt)

        if (!mConfiguration.isValid) {
            throw Configuration.InvalidConfigurationException(mConfiguration.configurationError)
        }

        if (hasConfigurationChanged()) {
            // discard any existing authorization state due to the change of configuration
            if (mIsDebug) Log.i(TAG, "Configuration change detected, discarding old state")
            mAuthStateManager.replaceState(AuthState())
            mConfiguration.acceptConfiguration()
        }

        initializeAppAuth(context)
    }

    fun setDebugMode(enabled: Boolean) {
        mIsDebug = enabled
    }

    private fun runBlockOnMainThread(runnable: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler?.post(runnable)
        } else {
            runnable.run()
        }
    }

    fun addAuthorizationCallback(authorizationCallback: SpotifyAuthorizationCallback.Authorize) {
        this.authorizationCallbacks.add(authorizationCallback)
    }

    fun addRefreshTokenCallback(refreshTokenCallback: SpotifyAuthorizationCallback.RefreshToken) {
        this.refreshTokenCallbacks.add(refreshTokenCallback)
    }

    fun removeAuthorizationCallback(authorizationCallback: SpotifyAuthorizationCallback.Authorize) {
        this.authorizationCallbacks.remove(authorizationCallback)
    }

    fun removeRefreshTokenCallback(refreshTokenCallback: SpotifyAuthorizationCallback.RefreshToken) {
        this.refreshTokenCallbacks.remove(refreshTokenCallback)
    }

    fun authorize(context: Context, requestCode: Int) {
        this.requestCode = requestCode
        startAuth(context, requestCode, null, null)
    }

    fun authorize(context: Context, completionPendingIntent: PendingIntent?, cancelPendingIntent: PendingIntent?) {
        startAuth(context, 0, completionPendingIntent, cancelPendingIntent)
    }

    fun isAuthorized(): Boolean {
        return mAuthStateManager.currentState.isAuthorized && !hasConfigurationChanged()
    }

    fun hasConfigurationChanged(): Boolean {
        return mConfiguration.hasConfigurationChanged()
    }

    fun getLastTokenResponse(): TokenResponse? {
        return mAuthStateManager.currentState.lastTokenResponse
    }

    /**
     * Determines whether the access token is considered to have expired. If no refresh token
     * has been acquired, then this method will always return `false`. A token refresh
     * can be forced, regardless of the validity of any currently acquired access token, by
     * calling setNeedsTokenRefresh(boolean).
     */
    fun getNeedsTokenRefresh(): Boolean {
        return mAuthStateManager.currentState.needsTokenRefresh
    }

    /**
     * Sets whether to force an access token refresh, regardless of the current access token's
     * expiration time.
     */
    fun setNeedsTokenRefresh(needsTokenRefresh: Boolean) {
        mAuthStateManager.currentState.needsTokenRefresh = needsTokenRefresh
    }

    /**
     * Initializes the authorization service configuration if necessary from the local
     * static values
     */
    @WorkerThread
    private fun initializeAppAuth(context: Context) {
        if (mIsDebug) Log.i(TAG, "Initializing AppAuth")
        recreateAuthorizationService(context)
        if (mAuthStateManager.currentState.authorizationServiceConfiguration != null) {
            // configuration is already created, skip to client initialization
            if (mIsDebug) Log.i(TAG, "auth config already established")
            initializeClient()
            return
        }
        if (mIsDebug) Log.i(TAG, "Creating auth config from res/raw/auth_config.json")
        val config = AuthorizationServiceConfiguration(
                mConfiguration.authEndpointUri!!,
                mConfiguration.tokenEndpointUri!!,
                mConfiguration.registrationEndpointUri)
        mAuthStateManager.replaceState(AuthState(config))
        initializeClient()
    }

    @WorkerThread
    private fun initializeClient() {
        if (mIsDebug) Log.i(TAG, "Using static client ID: " + mConfiguration.clientId)
        mClientId.set(mConfiguration.clientId)
        runBlockOnMainThread { initializeAuthRequest() }
    }

    @MainThread
    private fun initializeAuthRequest() {
        createAuthRequest()
        warmUpBrowser()
    }

    @MainThread
    private fun startAuth(context: Context, requestCode: Int, completionPendingIntent: PendingIntent?, cancelPendingIntent: PendingIntent?) {
        authorizationCallbacks.forEach { it.onAuthorizationStarted() }
        mExecutor.submit { doAuth(context, requestCode, completionPendingIntent, cancelPendingIntent) }
    }

    /**
     * Performs the authorization request
     */
    @WorkerThread
    private fun doAuth(context: Context, requestCode: Int, completionPendingIntent: PendingIntent?, cancelPendingIntent: PendingIntent?) {
        try {
            mAuthIntentLatch.await()
        } catch (ex: InterruptedException) {
            if (mIsDebug) Log.w(TAG, "Interrupted while waiting for auth intent")
        }

        if (completionPendingIntent != null) {
            mAuthService.performAuthorizationRequest(
                    mAuthRequest.get(),
                    completionPendingIntent,
                    cancelPendingIntent,
                    mAuthIntent.get())
        } else {
            val intent = mAuthService.getAuthorizationRequestIntent(
                    mAuthRequest.get(),
                    mAuthIntent.get())

            if (context is Fragment) {
                context.startActivityForResult(intent, requestCode)
            } else if (context is Activity) {
                context.startActivityForResult(intent, requestCode)
            }
        }
    }

    private fun warmUpBrowser() {
        mAuthIntentLatch = CountDownLatch(1)
        mExecutor.execute {
            if (mIsDebug) Log.i(TAG, "Warming up browser instance for auth request")
            val intentBuilder = mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri())
            intentBuilder.setToolbarColor(mConfiguration.customTabsColor)
            mAuthIntent.set(intentBuilder.build())
            mAuthIntentLatch.countDown()
        }
    }

    private fun createAuthRequest() {
        if (mIsDebug) Log.i(TAG, "Creating auth request")
        val authRequestBuilder = AuthorizationRequest.Builder(
                mAuthStateManager.currentState.authorizationServiceConfiguration!!,
                mClientId.get(),
                ResponseTypeValues.CODE,
                mConfiguration.redirectUri)
                .setScope(mConfiguration.scope)
        mAuthRequest.set(authRequestBuilder.build())
    }

    private fun recreateAuthorizationService(context: Context) {
        if (this::mAuthService.isInitialized) {
            if (mIsDebug) Log.i(TAG, "Discarding existing AuthService instance")
            mAuthService.dispose()
        }
        mAuthService = createAuthorizationService(context)
        mAuthRequest.set(null)
        mAuthIntent.set(null)
    }

    private fun createAuthorizationService(context: Context): AuthorizationService {
        if (mIsDebug) Log.i(TAG, "Creating authorization service")
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(mBrowserMatcher)
        builder.setConnectionBuilder(mConfiguration.connectionBuilder)
        return AuthorizationService(context, builder.build())
    }

    /**
     * Should be called in ´onStart()´ of the Activity
     */
    fun onStart() {
        if (mExecutor.isShutdown) {
            mExecutor = Executors.newSingleThreadExecutor()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (this.requestCode == requestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                authorizationCallbacks.forEach { it.onAuthorizationCanceled() }
                return
            }

            data?.let { intent ->
                val response = AuthorizationResponse.fromIntent(intent)
                val ex = AuthorizationException.fromIntent(intent)

                if (response != null || ex != null) {
                    mAuthStateManager.updateAfterAuthorization(response, ex)
                }

                when {
                    response?.authorizationCode != null -> {
                        // authorization code exchange is required
                        mAuthStateManager.updateAfterAuthorization(response, ex)
                        exchangeAuthorizationCode(response)
                    }
                    ex != null -> {
                        authorizationCallbacks.forEach { it.onAuthorizationRefused("Authorization flow failed: " + ex.message) }
                    }
                    else -> {
                        authorizationCallbacks.forEach { it.onAuthorizationFailed("No authorization state retained - reauthorization required") }
                    }
                }
            }
        }
    }

    /**
     * Should be called in ´onStop()´ of the Activity
     */
    fun onStop() {
        mExecutor.shutdownNow()
    }

    /**
     * Should be called in ´onDestroy()´ of the Activity
     */
    fun onDestroy() {
        if (this::mAuthService.isInitialized) {
            mAuthService.dispose()
        }
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }

    @MainThread
    fun refreshAccessToken() {
        refreshTokenCallbacks.forEach { it.onRefreshAccessTokenStarted() }

        performTokenRequest(mAuthStateManager.currentState.createTokenRefreshRequest()) { tokenResponse: TokenResponse?, authException: AuthorizationException? ->
            handleRefreshAccessTokenResponse(tokenResponse, authException)
        }
    }

    @WorkerThread
    private fun handleRefreshAccessTokenResponse(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
        mAuthStateManager.updateAfterTokenResponse(tokenResponse, authException)

        runBlockOnMainThread {
            refreshTokenCallbacks.forEach { it.onRefreshAccessTokenSucceed(mAuthStateManager.currentState.lastTokenResponse, getCurrentUser()) }
        }
    }

    @MainThread
    private fun performTokenRequest(request: TokenRequest, callback: TokenResponseCallback) {
        val clientAuthentication: ClientAuthentication

        clientAuthentication = try {
            mAuthStateManager.currentState.clientAuthentication
        } catch (ex: UnsupportedAuthenticationMethod) {
            if (mIsDebug) Log.d(TAG, "Token request cannot be made, client authentication " +
                    "for the token endpoint could not be constructed (%s)", ex)
            authorizationCallbacks.forEach { it.onAuthorizationFailed("Client authentication method is unsupported") }
            return
        }

        mAuthService.performTokenRequest(request, clientAuthentication, callback)
    }

    @MainThread
    private fun exchangeAuthorizationCode(authorizationResponse: AuthorizationResponse) {
        performTokenRequest(authorizationResponse.createTokenExchangeRequest()) { tokenResponse: TokenResponse?, authException: AuthorizationException? ->
            handleCodeExchangeResponse(tokenResponse, authException)
        }
    }

    @WorkerThread
    private fun handleCodeExchangeResponse(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
        mAuthStateManager.updateAfterTokenResponse(tokenResponse, authException)

        if (!isAuthorized()) {
            val message = ("Authorization Code exchange failed"
                    + if (authException != null) authException.error else "")

            runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationRefused(message) } }
        } else {
            if (fetchUserAfterAuthorization) {
                runBlockOnMainThread { fetchUser() }
            } else {
                runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationSucceed(mAuthStateManager.currentState.lastTokenResponse, getCurrentUser()) } }
            }
        }
    }

    @MainThread
    fun logOut() {
        // discard the authorization and token state, but retain the configuration and
        // dynamic client registration (if applicable), to save from retrieving them again.
        val currentState: AuthState = mAuthStateManager.currentState
        val serviceConfiguration = currentState.authorizationServiceConfiguration

        serviceConfiguration?.let {
            val clearedState = AuthState(it)
            if (currentState.lastRegistrationResponse != null) {
                clearedState.update(currentState.lastRegistrationResponse)
            }
            mAuthStateManager.replaceState(clearedState)
            mAuthStateManager.updateUser(null)
        }
    }

    @Nullable
    fun getCurrentUser(): UserPrivate? {
        return mAuthStateManager.currentUser
    }

    /**
     * Demonstrates the use of [AuthState.performActionWithFreshTokens] to retrieve
     * user info from the IDP's user info endpoint. This callback will negotiate a new access
     * token / id token for use in a follow-up action, or provide an error if this fails.
     */
    @MainThread
    fun fetchUser() {
        mAuthStateManager.currentState.performActionWithFreshTokens(mAuthService, this::fetchUser)
    }

    @MainThread
    private fun fetchUser(accessToken: String?, idToken: String?, ex: AuthorizationException?) {
        if (ex != null) {
            Log.e(TAG, "Token refresh failed when fetching user info")
            mAuthStateManager.updateUser(null)
            runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationFailed("Failed while fetching user") } }
            return
        }

        val userInfoEndpoint: URL
        userInfoEndpoint = try {
            if (mConfiguration.userInfoEndpointUri != null) {
                URL(mConfiguration.userInfoEndpointUri.toString())
            } else {
                URL(null)
            }
        } catch (malformedURLException: MalformedURLException) {
            Log.e(TAG, "Failed to construct user info endpoint URL", malformedURLException)
            mAuthStateManager.updateUser(null)
            authorizationCallbacks.forEach { it.onAuthorizationFailed("Failed to construct user info endpoint URL") }
            return
        }

        mExecutor.submit {
            try {
                val conn = userInfoEndpoint.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.instanceFollowRedirects = false

                val response = Okio.buffer(Okio.source(conn.inputStream))
                        .readString(Charset.forName("UTF-8"))

                val user = mGson.fromJson(response, UserPrivate::class.java)
                mAuthStateManager.updateUser(user)

            } catch (ioException: IOException) {
                Log.e(TAG, "Network error when querying user info endpoint", ioException)
                runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationFailed("Network error") } }
            } catch (jsonException: JSONException) {
                Log.e(TAG, "Failed to parse user fetch response")
                runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationFailed("Failed to parse user fetch response") } }
            }

            runBlockOnMainThread { authorizationCallbacks.forEach { it.onAuthorizationSucceed(mAuthStateManager.currentState.lastTokenResponse, getCurrentUser()) } }
        }
    }
}