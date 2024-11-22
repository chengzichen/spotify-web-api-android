package io.github.kaaes.spotify.webapi.retrofit.kt;

import java.io.IOException;
import java.lang.annotation.Annotation;

import io.github.kaaes.spotify.webapi.core.Config;
import io.github.kaaes.spotify.webapi.core.models.ErrorDetails;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Creates and configures a REST adapter for Spotify Web API.
 * <p>
 * Basic usage:
 * SpotifyService spotifyService = Spotify.createAuthenticatedService(accessToken);
 * <p>
 * Access token is optional for certain endpoints
 * so if you know you'll only use the ones that don't require authorisation
 * you can use unauthenticated service:
 * <p>
 * SpotifyService spotifyService = Spotify.createNotAuthenticatedService()
 * <p>
 * Call<Album> call = spotifyService.getAlbum("2dIGnmEIy1WZIcZCFSj6i8");
 * Response<Album> response = call.execute();
 * Album album = response.body();
 */
public class Spotify {

    private static Converter<ResponseBody, ErrorDetails> errorConverter = null;

    public static SpotifyService createAuthenticatedService(String accessToken) {

        Retrofit build = new Retrofit.Builder()
                .client(createHttpClient(accessToken))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Config.API_URL)
                .build();

        return build.create(SpotifyService.class);
    }

    public static SpotifyService createNotAuthenticatedService() {

        Retrofit build = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Config.API_URL)
                .build();

        return build.create(SpotifyService.class);
    }


    public static OkHttpClient createHttpClient(String accessToken) {
        return new OkHttpClient.Builder()
                .addInterceptor(new ApiAuthenticator(accessToken))
                .build();
    }

    protected static Converter<ResponseBody, ErrorDetails> getErrorConverter() {
        if (errorConverter == null) {
            errorConverter = buildErrorConverter();
        }
        return errorConverter;
    }

    private static Converter<ResponseBody, ErrorDetails> buildErrorConverter() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Retrofit restAdapter = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Config.API_URL)
                .build();

        return restAdapter.responseBodyConverter(ErrorDetails.class, new Annotation[0]);
    }

    /**
     * The request interceptor that will add the header with OAuth
     * token to every request made with the wrapper.
     */
    public static class ApiAuthenticator implements Interceptor {

        private String mAccessToken;

        public ApiAuthenticator(String accessToken) {
            mAccessToken = accessToken;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (mAccessToken != null) {
                Request authRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + mAccessToken)
                        .build();
                return chain.proceed(authRequest);
            }
            return chain.proceed(request);
        }
    }
}
