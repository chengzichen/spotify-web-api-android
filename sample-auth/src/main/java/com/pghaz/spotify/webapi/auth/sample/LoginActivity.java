package com.pghaz.spotify.webapi.auth.sample;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;

import com.google.android.material.snackbar.Snackbar;

import net.openid.appauth.TokenResponse;

import org.jetbrains.annotations.Nullable;

import io.github.kaaes.spotify.webapi.core.models.UserPrivate;

public final class LoginActivity extends BaseSpotifyActivity {

    private static final String EXTRA_FAILED = "failed";

    private static final int REQUEST_CODE_AUTHORIZATION = 100;

    private boolean usingPendingIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        CheckBox pendingIntentCheckBox = findViewById(R.id.pending_intents_checkbox);
        pendingIntentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> usingPendingIntent = isChecked);

        findViewById(R.id.retry).setOnClickListener((View view) ->
                getSpotifyAuthClient().authorize(this, REQUEST_CODE_AUTHORIZATION));

        findViewById(R.id.go_to_token_activity).setOnClickListener((View view) -> {
            Intent intent = getTokenActivityIntent();
            startActivity(intent);
            finish();
        });

        findViewById(R.id.start_auth).setOnClickListener((View view) -> {
            if (usingPendingIntent) {
                Intent completionIntent = getTokenActivityIntent();
                Intent cancelIntent = new Intent(this, LoginActivity.class);
                cancelIntent.putExtra(EXTRA_FAILED, true);
                cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent completionPendingIntent = PendingIntent.getActivity(this, 6, completionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent cancelPendingIntent = PendingIntent.getActivity(this, 7, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                getSpotifyAuthClient().authorize(this, completionPendingIntent, cancelPendingIntent);
                finish();
            } else {
                getSpotifyAuthClient().authorize(this, REQUEST_CODE_AUTHORIZATION);
            }
        });


        if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
            showSnackBar("Authorization with PendingIntent refused");
        }

        displayAuthOptions();

        if (spotifyAuthClient.isAuthorized()) {
            if (spotifyAuthClient.getNeedsTokenRefresh()) {
                spotifyAuthClient.refreshAccessToken();
            } else {
                updateButtonsVisibility(true);
            }
        } else {
            updateButtonsVisibility(false);
        }
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
    }

    @MainThread
    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
                message,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onAuthorizationStarted() {
        super.onAuthorizationStarted();
        displayLoading("onAuthorizationStarted");
    }

    @Override
    public void onAuthorizationCancelled() {
        super.onAuthorizationCancelled();
        showSnackBar("Authorization cancelled");
        displayAuthOptions();
    }

    @Override
    public void onAuthorizationFailed(@Nullable String error) {
        super.onAuthorizationFailed(error);
        showSnackBar("Authorization failed");
        displayError(error, true);
    }

    @Override
    public void onAuthorizationRefused(@Nullable String error) {
        super.onAuthorizationRefused(error);
        showSnackBar("Authorization refused");
        displayAuthOptions();
    }

    @Override
    public void onAuthorizationSucceed(@Nullable TokenResponse tokenResponse, @Nullable UserPrivate user) {
        super.onAuthorizationSucceed(tokenResponse, user);
        Toast.makeText(this, "AccessToken: " + tokenResponse.accessToken, Toast.LENGTH_SHORT).show();

        Intent intent = getTokenActivityIntent();
        startActivity(intent);
        finish();
    }

    @Override
    public void onRefreshAccessTokenSucceed(@Nullable TokenResponse tokenResponse, @Nullable UserPrivate user) {
        super.onRefreshAccessTokenSucceed(tokenResponse, user);
        Intent intent = getTokenActivityIntent();
        startActivity(intent);
        finish();
    }

    private Intent getTokenActivityIntent() {
        Intent intent = new Intent(this, TokenActivity.class);
        intent.putExtra(BaseSpotifyActivity.EXTRA_USING_PENDING_INTENT, usingPendingIntent);
        return intent;
    }

    private void updateButtonsVisibility(boolean isAuthorized) {
        if (isAuthorized) {
            findViewById(R.id.start_auth).setVisibility(View.GONE);
            findViewById(R.id.go_to_token_activity).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.start_auth).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_token_activity).setVisibility(View.GONE);
        }
    }
}