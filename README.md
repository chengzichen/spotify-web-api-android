[![](https://jitpack.io/v/pghazal/spotify-web-api-android.svg)](https://jitpack.io/#pghazal/spotify-web-api-android)

# Spotify Web API for Android

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

### Step 1: add your redirect URI scheme into your app build.gradle

´´´

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
´´´

### Step 2: initialize SpotifyAuthorizationClient

In your ´onCreate()´ Activity, initialize the ´SpotifyAuthorizationClient´.

´´´
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
 ´´´
 
If needed you can add [scopes](https://developer.spotify.com/documentation/general/guides/scopes/) at creation:

´´´
spotifyAuthClient = SpotifyAuthorizationClient.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            BuildConfig.SPOTIFY_REDIRECT_URI
        )
            .setScopes(
                arrayOf(
                    "app-remote-control",
                    "user-read-recently-played"
                )
            )
            .build(this)
            ´´´
            
You can custom the color of the Android ´CustomTabs´ when showing Spotify Authorization webview:

´´´
spotifyAuthClient = SpotifyAuthorizationClient.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            BuildConfig.SPOTIFY_REDIRECT_URI
        )
        .setScopes(
                arrayOf(
                    "app-remote-control",
                    "user-read-recently-played"
                )
            )
        .setCustomTabColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .build(this)
        ´´´
        
And also decide if you want to fetch user infos after authorization granted:

´´´
spotifyAuthClient = SpotifyAuthorizationClient.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            BuildConfig.SPOTIFY_REDIRECT_URI
        )
        .setScopes(
                arrayOf(
                    "app-remote-control",
                    "user-read-recently-played"
                )
            )
        .setCustomTabColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .setFetchUserAfterAuthorization(true)
        .build(this)
        ´´´
        
### Step 3: call the different appropriate methods:

´´´
    ...
    
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
    
    ...
    ´´´
        
### Step 4: implement and subscribe to Authorization and Refresh token callbacks

Implement ´SpotifyAuthorizationCallback.Authorize´ to receive callbacks according to Spotify authorization.

´´´ 
fun onAuthorizationStarted()

fun onAuthorizationCancelled()

fun onAuthorizationFailed(error: String?)

fun onAuthorizationRefused(error: String?)

fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
´´´
        
Implement ´SpotifyAuthorizationCallback.RefreshToken´ to receive callbacks when you get a new refresh token.

´´´
fun onRefreshAccessTokenStarted()

fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
´´´

´user: UserPrivate?´ will be null if you don't build the client with ´.setFetchUserAfterAuthorization(true)´.




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
