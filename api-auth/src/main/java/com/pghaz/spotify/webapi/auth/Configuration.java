/*
 * Copyright 2016 The AppAuth for Android Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pghaz.spotify.webapi.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.openid.appauth.connectivity.ConnectionBuilder;
import net.openid.appauth.connectivity.DefaultConnectionBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;


/**
 * Reads and validates the app configuration from `res/raw/auth_config.json`.
 * Configuration changes are detected by comparing the hash of the last known configuration to the read
 * configuration. When a configuration change is detected, the app state is reset.
 */
public final class Configuration {

    private static final String TAG = "Configuration";

    private static final String PREFS_NAME = "config";
    private static final String KEY_LAST_HASH = "lastHash";

    private static WeakReference<Configuration> sInstance = new WeakReference<>(null);

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final Resources mResources;
    private JSONObject mConfigJson;
    private String mConfigHash;
    private String mConfigError;
    private String mClientId;
    private String mScope;
    private Uri mRedirectUri;
    private Uri mAuthEndpointUri;
    private Uri mTokenEndpointUri;
    private Uri mRegistrationEndpointUri;
    private Uri mUserInfoEndpointUri;
    @ColorInt
    private int mCustomTabsColor;

    private Configuration(Context context, String clientId, String redirectUri, String[] scopes, @ColorInt int colorResId) {
        mContext = context;
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mResources = context.getResources();

        try {
            readConfiguration(clientId, redirectUri, scopes, colorResId);
        } catch (InvalidConfigurationException | NoSuchAlgorithmException ex) {
            mConfigError = ex.getMessage();
        }
    }

    public static Configuration getInstance(Context context, String clientId, String redirectUri, String[] scopes, @ColorInt int colorResId) {
        Configuration config = sInstance.get();
        if (config == null) {
            config = new Configuration(context, clientId, redirectUri, scopes, colorResId);
            sInstance = new WeakReference<>(config);
        }

        return config;
    }

    /**
     * Indicates whether the configuration has changed from the last known valid state.
     */
    protected boolean hasConfigurationChanged() {
        String lastHash = getLastKnownConfigHash();
        return !mConfigHash.equals(lastHash);
    }

    /**
     * Indicates whether the current configuration is valid.
     */
    protected boolean isValid() {
        return mConfigError == null;
    }

    /**
     * Returns a description of the configuration error, if the configuration is invalid.
     */
    @Nullable
    protected String getConfigurationError() {
        return mConfigError;
    }

    private String getLastKnownConfigHash() {
        return mPrefs.getString(KEY_LAST_HASH, null);
    }

    /**
     * Indicates that the current configuration should be accepted as the "last known valid"
     * configuration.
     */
    protected void acceptConfiguration() {
        mPrefs.edit().putString(KEY_LAST_HASH, mConfigHash).apply();
    }

    @Nullable
    public String getClientId() {
        return mClientId;
    }

    @NonNull
    public String getScope() {
        return mScope;
    }

    @NonNull
    public Uri getRedirectUri() {
        return mRedirectUri;
    }

    @Nullable
    protected Uri getAuthEndpointUri() {
        return mAuthEndpointUri;
    }

    @Nullable
    protected Uri getTokenEndpointUri() {
        return mTokenEndpointUri;
    }

    @Nullable
    protected Uri getRegistrationEndpointUri() {
        return mRegistrationEndpointUri;
    }

    @Nullable
    public Uri getUserInfoEndpointUri() {
        return mUserInfoEndpointUri;
    }

    @ColorInt
    public int getCustomTabsColor() {
        return mCustomTabsColor;
    }

    protected ConnectionBuilder getConnectionBuilder() {
        return DefaultConnectionBuilder.INSTANCE;
    }

    private void readConfiguration(String clientId, String redirectUri,
                                   String[] scopes, @ColorInt int colorResId)
            throws InvalidConfigurationException, NoSuchAlgorithmException {

        BufferedSource configSource =
                Okio.buffer(Okio.source(mResources.openRawResource(R.raw.auth_config)));
        Buffer configData = new Buffer();
        try {
            configSource.readAll(configData);
            mConfigJson = new JSONObject(configData.readString(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw new InvalidConfigurationException(
                    "Failed to read configuration: " + ex.getMessage());
        } catch (JSONException ex) {
            throw new InvalidConfigurationException(
                    "Unable to parse configuration: " + ex.getMessage());
        }

        mAuthEndpointUri = getRequiredConfigWebUri("authorization_endpoint_uri");
        mTokenEndpointUri = getRequiredConfigWebUri("token_endpoint_uri");
        mUserInfoEndpointUri = getRequiredConfigWebUri("user_info_endpoint_uri");

        mClientId = clientId;
        mRedirectUri = Uri.parse(redirectUri);
        mScope = buildScope(scopes);
        mCustomTabsColor = colorResId;

        if (mClientId == null) {
            mRegistrationEndpointUri = getRequiredConfigWebUri("registration_endpoint_uri");
        }

        mConfigHash = configData.sha256().base64() + encodeSHA1Base64(mClientId + mRedirectUri + mScope);

        if (!isRedirectUriRegistered()) {
            throw new InvalidConfigurationException(
                    "Error with redirect_uri: ensure that the appAuthRedirectScheme in your build.gradle file is set, " +
                            "that it matches the redirect_uri scheme in the SpotifyAuthorizationClient.Builder and " +
                            "that you set the full redirect_uri in the SpotifyAuthorizationClient.Builder");
        }
    }

    private String encodeSHA1Base64(String toEncode) throws NoSuchAlgorithmException {
        String result;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(toEncode.getBytes(Charset.forName("UTF-8")), 0, toEncode.length());
        byte[] sha1hash = md.digest();
        result = Base64.encodeToString(sha1hash, Base64.DEFAULT);
        result = result.substring(0, result.length() - 1);
        return result;
    }

    private String buildScope(String[] scopes) {
        StringBuilder builder = new StringBuilder();

        for (String scope : scopes) {
            builder.append(scope);
            builder.append(" ");
        }

        return builder.toString().trim();
    }

    @Nullable
    protected String getConfigString(String propName) {
        String value = mConfigJson.optString(propName);
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        return value;
    }

    @NonNull
    private String getRequiredConfigString(String propName)
            throws InvalidConfigurationException {
        String value = getConfigString(propName);
        if (value == null) {
            throw new InvalidConfigurationException(
                    propName + " is required but not specified in the configuration");
        }

        return value;
    }

    @NonNull
    protected Uri getRequiredConfigUri(String propName)
            throws InvalidConfigurationException {
        String uriStr = getRequiredConfigString(propName);
        Uri uri;
        try {
            uri = Uri.parse(uriStr);
        } catch (Throwable ex) {
            throw new InvalidConfigurationException(propName + " could not be parsed", ex);
        }

        if (!uri.isHierarchical() || !uri.isAbsolute()) {
            throw new InvalidConfigurationException(
                    propName + " must be hierarchical and absolute");
        }

        if (!TextUtils.isEmpty(uri.getEncodedUserInfo())) {
            throw new InvalidConfigurationException(propName + " must not have user info");
        }

        if (!TextUtils.isEmpty(uri.getEncodedQuery())) {
            throw new InvalidConfigurationException(propName + " must not have query parameters");
        }

        if (!TextUtils.isEmpty(uri.getEncodedFragment())) {
            throw new InvalidConfigurationException(propName + " must not have a fragment");
        }

        return uri;
    }

    protected Uri getRequiredConfigWebUri(String propName)
            throws InvalidConfigurationException {
        Uri uri = getRequiredConfigUri(propName);
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme) || !("http".equals(scheme) || "https".equals(scheme))) {
            throw new InvalidConfigurationException(
                    propName + " must have an http or https scheme");
        }

        return uri;
    }

    private boolean isRedirectUriRegistered() {
        // ensure that the redirect URI declared in the configuration is handled by some activity
        // in the app, by querying the package manager speculatively
        Intent redirectIntent = new Intent();
        redirectIntent.setPackage(mContext.getPackageName());
        redirectIntent.setAction(Intent.ACTION_VIEW);
        redirectIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        redirectIntent.setData(mRedirectUri);

        return !mContext.getPackageManager().queryIntentActivities(redirectIntent, 0).isEmpty();
    }

    public static final class InvalidConfigurationException extends Exception {
        InvalidConfigurationException(String reason) {
            super(reason);
        }

        InvalidConfigurationException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }
}