package io.github.kaaes.spotify.webapi.retrofit.kt;

import java.io.IOException;

import io.github.kaaes.spotify.webapi.core.models.ErrorDetails;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class SpotifyError extends Exception {

    public static final int ERROR_UNEXPECTED = -1;
    public static final int ERROR_NETWORK = -2;

    public final ErrorDetails details;

    public SpotifyError(ErrorDetails details) {
        super(details.message);
        this.details = details;
    }

    public static <T> SpotifyError fromResponse(Response<T> response) {
        Converter<ResponseBody, ErrorDetails> errorConverter = Spotify.getErrorConverter();

        ErrorDetails details;

        try {
            details = errorConverter.convert(response.errorBody());
        } catch (IOException e) {
            e.printStackTrace();

            details = new ErrorDetails();
            details.status = ERROR_UNEXPECTED;
            details.message = "Can't read error response: fromResponse()";
        }

        if (details == null) {
            details = new ErrorDetails();
            details.status = SpotifyError.ERROR_UNEXPECTED;
            details.message = "Unexpected error: fromResponse()";
        }

        return new SpotifyError(details);
    }
}