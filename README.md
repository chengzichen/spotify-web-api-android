[![](https://jitpack.io/v/pghazal/spotify-web-api-android.svg)](https://jitpack.io/#pghazal/spotify-web-api-android)

# Spotify Web API and Authentication for Android

This project is a fork from [this library](https://github.com/kaaes/spotify-web-api-android) with few adjustments and fixes.

This project is a wrapper for the [Spotify Web API](https://developer.spotify.com/web-api/).
It uses [Retrofit](http://square.github.io/retrofit/) to create Java interfaces from API endpoints.

This library supports both Retrofit 1.9 and Retrofit 2.0 (experimental).

This library adds also an alternative to Spotify Android SDK Authentication for Single Sign-on in order to silently refresh token.

## Integrating into your project

This library is available in [JitPack.io](https://jitpack.io/) repository.
To use it make sure that repository's url is added to the `build.gradle` file in your app:

```groovy

repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    // To import for Retrofit 1.9
    implementation 'com.github.pghazal.spotify-web-api-android:api-retrofit:1.0.0'

    // To import for Retrofit 2.0 (experimantal)
    implementation 'com.github.pghazal.spotify-web-api-android:api-retrofit2:1.0.0'
    
    // To import Authentication
    implementation 'com.github.pghazal.spotify-web-api-android:api-auth:1.0.0'

    // Other dependencies your app might use
}
```

## Using with Retrofit 2.0

Basic usage

```java
SpotifyService spotifyService = Spotify.createAuthenticatedService(accessToken);

// Access token is strongly advised but optional for certain endpoints
// so if you know you'll only use the ones that don't require authorisation
// you can use unauthenticated service instead:

SpotifyService spotifyService = Spotify.createNotAuthenticatedService()

Call<Album> call = spotifyService.getAlbum("2dIGnmEIy1WZIcZCFSj6i8");
Response<Album> response = call.execute();
Album album = response.body();
```

If default configuration doesn't work for you, you can create your own instance:

```java
Retrofit retrofit = new Retrofit.Builder()
        .client(customHttpClient)
        .addConverterFactory(customConverterFactory)
        .baseUrl(Config.API_URL)
        .build();

SpotifyService spotifyService = retrofit.create(SpotifyService.class);
```

## Using with Retrofit 1.9

Basic usage

```java
SpotifyService spotifyService = Spotify.createAuthenticatedService(accessToken);

// Access token is strongly advised but optional for certain endpoints
// so if you know you'll only use the ones that don't require authorisation
// you can use unauthenticated service instead:

SpotifyService spotifyService = Spotify.createNotAuthenticatedService()

Album album = spotifyService.getAlbum("2dIGnmEIy1WZIcZCFSj6i8");
```

If default configuration doesn't work for you, you can create your own instance:

```java
RestAdapter adapter = new RestAdapter.Builder()
        .setEndpoint(Config.API_URL)
        .setRequestInterceptor(customRequestInterceptor)
        .setExecutors(customHttpExecutor, customCallbackExecutor)
        .build();

SpotifyService spotifyService = adapter.create(SpotifyService.class);
```

## Obtaining Access Tokens

To handle Spotify authentication, authorization and refresh token, the library uses [AppAuth](https://github.com/openid/AppAuth-Android).

TL;DR: show me [complete code](https://github.com/pghazal/spotify-web-api-android#a-complete-code-)

### Step 1: add your redirect URI scheme into your app build.gradle

```groovy
android {
    ...
    
    defaultConfig {
        applicationId "..."
        
        // For example if your Spotify redirect URI is goatscheme://callback
        // make sure you set ´goatscheme´ instead of ´SPOTIFY_REDIRECT_URI_SCHEME´
        manifestPlaceholders = [
            'appAuthRedirectScheme': 'SPOTIFY_REDIRECT_URI_SCHEME'
        ]

        ...
    }
    ...
}
```

### Step 2: initialize SpotifyAuthorizationClient

In your ```onCreate()``` Activity, initialize the ```SpotifyAuthorizationClient```.

```java
lateinit var spotifyAuthClient: SpotifyAuthorizationClient

private fun initSpotifyAuthClient() {
    spotifyAuthClient = SpotifyAuthorizationClient
        .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
        .build(this)
}
        
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
       
    initSpotifyAuthClient()
       
    ...
}
```
 
If needed you can add [scopes](https://developer.spotify.com/documentation/general/guides/scopes/) at creation:

```java
spotifyAuthClient = SpotifyAuthorizationClient
    .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
    .setScopes(
        arrayOf(
            "app-remote-control",
            "user-read-recently-played"
        )
    )
    .build(this)
```
            
You can custom the color of the Android ```CustomTabs``` when showing Spotify Authorization webview:

```java
spotifyAuthClient = SpotifyAuthorizationClient
    .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
    .setScopes(
        arrayOf(
            "app-remote-control",
            "user-read-recently-played"
        )
    )
    .setCustomTabColor(ContextCompat.getColor(this, R.color.colorPrimary))
    .build(this)
```
        
And also decide if you want to fetch user infos after authorization granted:

```java
spotifyAuthClient = SpotifyAuthorizationClient
    .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
    .setScopes(
        arrayOf(
            "app-remote-control",
            "user-read-recently-played"
        )
    )
    .setCustomTabColor(ContextCompat.getColor(this, R.color.colorPrimary))
    .setFetchUserAfterAuthorization(true)
    .build(this)
```
        
### Step 3: call the different appropriate methods:

```java
override fun onStart() {
    super.onStart()
    spotifyAuthClient.onStart()
}
    
override fun onStop() {
    super.onStop()
    spotifyAuthClient.onStop()
}

override fun onDestroy() {
    super.onDestroy()
    spotifyAuthClient.onDestroy()
}
```
        
### Step 4: implement and subscribe to Authorization and Refresh token callbacks

Make your Activity implements ```SpotifyAuthorizationCallback.Authorize``` to receive callbacks for Spotify authorization.

```java
fun onAuthorizationStarted()
fun onAuthorizationCancelled()
fun onAuthorizationFailed(error: String?) 
fun onAuthorizationRefused(error: String?)
fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
```
        
Same with ```SpotifyAuthorizationCallback.RefreshToken``` to receive callbacks when you get a new refresh token.

```java
fun onRefreshAccessTokenStarted()
fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
```

Note: ```user: UserPrivate?``` fields will be empty if you don't build the client with ```.setFetchUserAfterAuthorization(true)```.

Don't forget to add listeners:

```java
spotifyAuthClient = SpotifyAuthorizationClient
        .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
        .build(this)

spotifyAuthClient.addAuthorizationCallback(this)
spotifyAuthClient.addRefreshTokenCallback(this)
```

### Step 5: Authorization

There are two ways to authorize your app. Each way is done in three easy steps:
1) Show login page
2) Handle authorization response
3) Get access token from ```onAuthorizationSucceed(...)``` callback


#### Using a request code

1) Show login page
```java
spotifyAuthClient.authorize(this, REQUEST_CODE_SPOTIFY_LOGIN)
```

2) Handle authorization response in ```onActivityResult()```
```java
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    // At this point it is authorized but we don't have access token yet.
    // We get it when onAuthorizationSucceed() is called
    spotifyAuthClient.onActivityResult(requestCode, resultCode, data)
}
```

#### Using PendingIntent

1) Show login page
```java
val completionIntent = Intent(this, SpotifyActivity::class.java)
val cancelIntent = Intent(this, LoginActivity::class.java)
cancelIntent.putExtra(EXTRA_FAILED, true)
cancelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

val completionPendingIntent = PendingIntent.getActivity(this, 6, completionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
val cancelPendingIntent = PendingIntent.getActivity(this, 7, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

spotifyAuthClient.authorize(this, completionPendingIntent, cancelPendingIntent)
```

2) Handle authorization response by calling this in the completion Intent Activity (here it's SpotifyActivity):
```java
if (getIntent() != null) {
    // At this point it is authorized but we don't have access token yet.
    // We get it at when onAuthorizationSucceed() is called
    spotifyAuthClient.onCompletionActivity(getIntent())
}
```


3) Then, code exchange is done and access token will be given in: 

```onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)```


### Step 6: Refresh token

Simply call ```spotifyAuthClient.refreshAccessToken()```.

The new token will be given in:

```onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)```


### Need the access token ?

```val accessToken = spotifyAuthClient.getLastTokenResponse()?.accessToken```


### Need current user ?

```val currentUser = spotifyAuthClient.getCurrentUser()```


### A complete code ?

A nice way to handle authorization and silently getting a new token can be achieved like this:

```java
class BaseSpotifyActivity : AppCompatActivity(),
    SpotifyAuthorizationCallback.Authorize,
    SpotifyAuthorizationCallback.RefreshToken{
    
    companion object {
        private const val REQUEST_CODE_SPOTIFY_LOGIN = 42
    }

    lateinit var spotifyAuthClient: SpotifyAuthorizationClient

    private fun initSpotifyAuthClient() {
        spotifyAuthClient = SpotifyAuthorizationClient
            .Builder(SPOTIFY_CLIENT_ID, SPOTIFY_REDIRECT_URI)
            .setScopes(
                arrayOf(
                    "app-remote-control",
                    "user-read-recently-played"
                )
            )
            .setCustomTabColor(Color.RED)
            .setFetchUserAfterAuthorization(true)
            .build(this)
            
        spotifyAuthClient.addAuthorizationCallback(this)
        spotifyAuthClient.addRefreshTokenCallback(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSpotifyAuthClient()

        if (spotifyAuthClient.isAuthorized()) {
            if (spotifyAuthClient.getNeedsTokenRefresh()) {
                spotifyAuthClient.refreshAccessToken()
            } else {
                onSpotifyAuthorizedAndAvailable(spotifyAuthClient.getLastTokenResponse()?.accessToken)
            }
        } else {
            spotifyAuthClient.authorize(this, REQUEST_CODE_SPOTIFY_LOGIN)
        }
    }

    private fun onSpotifyAuthorizedAndAvailable(accessToken: String?) {
        // make your Spotify Web API calls here
        Toast.makeText(this, accessToken, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // At this point it is authorized but we don't have access token yet.
        // We get it when onAuthorizationSucceed() is called
        spotifyAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        spotifyAuthClient.onStart()
    }

    override fun onStop() {
        super.onStop()
        spotifyAuthClient.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyAuthClient.removeAuthorizationCallback(this)
        spotifyAuthClient.removeRefreshTokenCallback(this)
        spotifyAuthClient.onDestroy()
    }

    override fun onAuthorizationCancelled() {
        Toast.makeText(this, "auth cancelled", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthorizationFailed(error: String?) {
        Toast.makeText(this, "auth failed", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthorizationRefused(error: String?) {
        Toast.makeText(this, "auth refused", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthorizationStarted() {
        Toast.makeText(this, "auth start", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable(tokenResponse?.accessToken)
    }

    override fun onRefreshAccessTokenStarted() {
        Toast.makeText(this, "refresh start", Toast.LENGTH_SHORT).show()
    }

    override fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable(tokenResponse?.accessToken)
    }
}

```

## Error Handling

### With Retrofit 1.9

When using Retrofit, errors are returned as [`RetrofitError`](http://square.github.io/retrofit/javadoc/retrofit/RetrofitError.html)
objects. These objects, among others, contain HTTP status codes and their descriptions,
for example `400 - Bad Request`.
In many cases this will work well enough but in some cases Spotify Web API returns more detailed information,
for example `400 - No search query`.

To use the data returned in the response from the Web API `SpotifyCallback` object should be passed to the
request method instead of regular Retrofit's `Callback`:
```java
spotifyService.getMySavedTracks(new SpotifyCallback<Pager<SavedTrack>>() {
    @Override
    public void success(Pager<SavedTrack> savedTrackPager, Response response) {
        // handle successful response
    }

    @Override
    public void failure(SpotifyError error) {
        // handle error
    }
});
```

For synchronous requests `RetrofitError` can be converted to `SpotifyError` if needed:

```java
try {
    Pager<SavedTrack> mySavedTracks = spotifyService.getMySavedTracks();
} catch (RetrofitError error) {
    SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
    // handle error
}
```

### With Retrofit 2.0

To use the data returned in the response from the Web API `SpotifyCallback` object should be passed to the
request method instead of regular Retrofit's `Callback`:

```java
Call<TracksPager> call = spotifyService.searchTracks(query, options);

call.enqueue(new SpotifyCallback<TracksPager>() {
     @Override
    public void onResponse(Call<TracksPager> call, Response<TracksPager> response, TracksPager payload) {
        // handle successful response
    }

    @Override
    public void onFailure(Call<TracksPager> call, SpotifyError error) {
        // handle error response
    }
});
```

For synchronous callse `RetrofitError` can be converted to `SpotifyError` if needed:

```java
Call<TracksPager> call = spotifyService.searchTracks(query, options);
Response<TracksPager> response = call.execute();
SpotifyError.fromResponse(response);
```

## ProGuard

```
# Spotify Retrofit
-keep class io.github.kaaes.spotify.webapi.retrofit.** { *; }
-keep interface io.github.kaaes.spotify.webapi.retrofit.** { *; }

# Spotify Core
-keep class io.github.kaaes.spotify.webapi.core.** { *; }
-keep interface io.github.kaaes.spotify.webapi.core.** { *; }

# Spotify Auth
-keep class com.pghaz.spotify.webapi.auth.** { *; }
-keep interface com.pghaz.spotify.webapi.auth.** { *; }
```

## Help

#### Versioning policy
We use [Semantic Versioning 2.0.0](http://semver.org/) as our versioning policy.

#### Bugs, Feature requests
Found a bug? Something that's missing? Feedback is an important part of improving the project, so please [open an issue](https://github.com/kaaes/spotify-web-api-android/issues).

#### Code
Fork this project and start working on your own feature branch. When you're done, send a Pull Request to have your suggested changes merged into the master branch by the project's collaborators. Read more about the [GitHub flow](https://guides.github.com/introduction/flow/).
