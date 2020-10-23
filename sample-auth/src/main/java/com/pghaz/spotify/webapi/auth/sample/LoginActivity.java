package com.pghaz.spotify.webapi.auth.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;

import com.google.android.material.snackbar.Snackbar;
import com.pghaz.spotify.webapi.activity.BaseSpotifyActivity;

import net.openid.appauth.TokenResponse;

import org.jetbrains.annotations.Nullable;

import io.github.kaaes.spotify.webapi.core.models.UserPrivate;

public final class LoginActivity extends BaseSpotifyActivity {

    private static final String TAG = "LoginActivity";

    private static final String EXTRA_FAILED = "failed";
    private static final int REQUEST_CODE_AUTHORIZATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSpotifyAuthClient().isAuthorized()) {
            Log.i(TAG, "User is already authenticated, proceeding to token activity");
            startActivity(new Intent(this, TokenActivity.class));
            finish();
            return;
        }

       /* completionIntent = Intent(context, TokenActivity::class.java)
        cancelIntent = Intent(context, LoginActivity::class.java)

        PendingIntent.getActivity(context, 0, completionIntent, 0),
        PendingIntent.getActivity(context, 0, cancelIntent, 0)
        cancelIntent.putExtra(EXTRA_FAILED, true)
        cancelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP*/

        setContentView(R.layout.activity_login);

        findViewById(R.id.retry).setOnClickListener((View view) ->
                getSpotifyAuthClient().authorize(this, REQUEST_CODE_AUTHORIZATION));
        findViewById(R.id.start_auth).setOnClickListener((View view) ->
                getSpotifyAuthClient().authorize(this, REQUEST_CODE_AUTHORIZATION));

        if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
            displayAuthCancelled();
        }

        displayAuthOptions();
    }

    @MainThread
    private void displayLoading(String loadingMessage) {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_container).setVisibility(View.GONE);
        findViewById(R.id.error_container).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.loading_description)).setText(loadingMessage);
    }

    @MainThread
    private void displayError(String error, boolean recoverable) {
        findViewById(R.id.error_container).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        findViewById(R.id.auth_container).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.error_description)).setText(error);
        findViewById(R.id.retry).setVisibility(recoverable ? View.VISIBLE : View.GONE);
    }

    @MainThread
    private void displayAuthOptions() {
        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        findViewById(R.id.error_container).setVisibility(View.GONE);

        /*AuthState state = mAuthStateManager.getCurrent();
        AuthorizationServiceConfiguration config = state.getAuthorizationServiceConfiguration();

        String authEndpointStr = "Static auth endpoint: \n";
        authEndpointStr += config.authorizationEndpoint;
        ((TextView) findViewById(R.id.auth_endpoint)).setText(authEndpointStr);

        String clientIdStr = "Static client ID: \n";
        clientIdStr += mClientId;
        ((TextView) findViewById(R.id.client_id)).setText(clientIdStr);*/
    }

    private void displayAuthCancelled() {
        Snackbar.make(findViewById(R.id.coordinator),
                "Authorization canceled",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onAuthorizationStarted() {
        displayLoading("onAuthorizationStarted");
    }

    @Override
    public void onAuthorizationCanceled() {
        displayAuthCancelled();
        displayAuthOptions();
    }

    @Override
    public void onAuthorizationFailed(@Nullable String error) {
        displayError(error, true);
    }

    @Override
    public void onAuthorizationRefused(@Nullable String error) {
        Snackbar.make(findViewById(R.id.coordinator),
                "Refused by user",
                Snackbar.LENGTH_SHORT)
                .show();

        displayAuthOptions();
    }

    @Override
    public void onAuthorizationSucceed(@Nullable TokenResponse tokenResponse, @Nullable UserPrivate user) {
        Toast.makeText(this, "AccessToken: " + tokenResponse.accessToken, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, TokenActivity.class);
        startActivity(intent);
        finish();
    }
}