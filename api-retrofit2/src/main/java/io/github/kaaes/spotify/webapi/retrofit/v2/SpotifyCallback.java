package io.github.kaaes.spotify.webapi.retrofit.v2;

import java.io.IOException;

import io.github.kaaes.spotify.webapi.core.models.ErrorDetails;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class SpotifyCallback<T> implements Callback<T> {

    public abstract void onResponse(Call<T> call, Response<T> response, T payload);

    public abstract void onFailure(Call<T> call, SpotifyError error);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(call, response, response.body());
        } else {
            onFailure(call, SpotifyError.fromResponse(response));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        ErrorDetails details = new ErrorDetails();

        if (t instanceof IOException) {
            details.status = SpotifyError.ERROR_NETWORK;
            details.message = "Network error";
        } else {
            details.status = SpotifyError.ERROR_UNEXPECTED;
            details.message = "Unexpected error";
        }

        onFailure(call, new SpotifyError(details));
    }
}
