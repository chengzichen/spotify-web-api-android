/*
 * Copyright 2015 The AppAuth for Android Authors. All Rights Reserved.
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

package com.pghaz.spotify.webapi.auth.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import net.openid.appauth.TokenResponse;

import org.jetbrains.annotations.Nullable;
import org.joda.time.format.DateTimeFormat;

import io.github.kaaes.spotify.webapi.core.models.UserPrivate;
import io.github.kaaes.spotify.webapi.core.models.UserPublic;


/**
 * Displays the authorized state of the user. This activity is provided with the outcome of the
 * authorization flow, which it uses to negotiate the final authorized state,
 * by performing an authorization code exchange if necessary. After this, the activity provides
 * additional post-authorization operations if available, such as fetching user info and refreshing
 * access tokens.
 */
public class TokenActivity extends BaseSpotifyActivity {

    private static final String TAG = "TokenActivity";

    private static final String KEY_USER_INFO = "userInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSpotifyAuthClient().hasConfigurationChanged()) {
            Toast.makeText(this, "Configuration change detected", Toast.LENGTH_SHORT).show();
            signOut();
            return;
        }

        setContentView(R.layout.activity_token);

/*        displayLoading("Restoring state...");

        if (savedInstanceState != null) {
            try {
                String savedUserInfoJson = savedInstanceState.getString(KEY_USER_INFO);
                if (savedUserInfoJson != null) {
                    mUserInfoJson.set(new JSONObject(savedUserInfoJson));
                } else {
                    mUserInfoJson.set(null);
                }
            } catch (JSONException ex) {
                Log.e(TAG, "Failed to parse saved user info JSON, discarding", ex);
            }
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (getSpotifyAuthClient().isAuthorized()) {
            displayAuthorized(getSpotifyAuthClient().getLastTokenResponse(), getSpotifyAuthClient().getCurrentUser());
        } else {
            displayNotAuthorized("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        // user info is retained to survive activity restarts, such as when rotating the
        // device or switching apps. This isn't essential, but it helps provide a less
        // jarring UX when these events occur - data does not just disappear from the view.
      /*  if (mUserInfoJson.get() != null) {
            state.putString(KEY_USER_INFO, mUserInfoJson.toString());
        }*/
        super.onSaveInstanceState(state);
    }

    @MainThread
    private void displayLoading(String message) {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.loading_description)).setText(message);
    }

    @MainThread
    private void displayAuthorized(TokenResponse tokenResponse, UserPublic currentUser) {
        findViewById(R.id.authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);

        TextView refreshTokenInfoView = findViewById(R.id.refresh_token_info);
        refreshTokenInfoView.setText((tokenResponse.refreshToken == null)
                ? R.string.no_refresh_token_returned
                : R.string.refresh_token_returned);

        TextView idTokenInfoView = findViewById(R.id.id_token_info);
        idTokenInfoView.setText((tokenResponse.idToken) == null
                ? R.string.no_id_token_returned
                : R.string.id_token_returned);

        TextView accessTokenInfoView = findViewById(R.id.access_token_info);
        if (tokenResponse.accessToken == null) {
            accessTokenInfoView.setText(R.string.no_access_token_returned);
        } else {
            Long expiresAt = tokenResponse.accessTokenExpirationTime;
            if (expiresAt == null) {
                accessTokenInfoView.setText(R.string.no_access_token_expiry);
            } else if (expiresAt < System.currentTimeMillis()) {
                accessTokenInfoView.setText(R.string.access_token_expired);
            } else {
                String template = getResources().getString(R.string.access_token_expires_at);
                accessTokenInfoView.setText(String.format(template,
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt)));
            }
        }

        Button refreshTokenButton = findViewById(R.id.refresh_token);
        refreshTokenButton.setVisibility(tokenResponse.refreshToken != null
                ? View.VISIBLE
                : View.GONE);
        refreshTokenButton.setOnClickListener((View view) -> getSpotifyAuthClient().refreshAccessToken());

        Button viewProfileButton = findViewById(R.id.view_profile);
        viewProfileButton.setVisibility(View.VISIBLE);
        viewProfileButton.setOnClickListener((View view) -> getSpotifyAuthClient().fetchUser());

        findViewById(R.id.sign_out).setOnClickListener((View view) -> signOut());

        View userInfoCard = findViewById(R.id.userinfo_card);
        if (currentUser == null) {
            userInfoCard.setVisibility(View.INVISIBLE);
        } else {
            ((TextView) findViewById(R.id.userinfo_name)).setText(currentUser.display_name);
            ((TextView) findViewById(R.id.userinfo_json)).setText((new Gson()).toJson(currentUser));
            userInfoCard.setVisibility(View.VISIBLE);
        }
    }

    @MainThread
    private void displayNotAuthorized(String explanation) {
        findViewById(R.id.not_authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.explanation)).setText(explanation);
        findViewById(R.id.reauth).setOnClickListener((View view) -> signOut());
    }

    @MainThread
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
                message,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @MainThread
    private void signOut() {
        getSpotifyAuthClient().logOut();

        Intent mainIntent = new Intent(this, LoginActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onAuthorizationStarted() {
        displayLoading("on Authorization Started");
    }

    @Override
    public void onAuthorizationCanceled() {
        displayNotAuthorized("onAuthorizationCanceled");
    }

    @Override
    public void onAuthorizationFailed(@Nullable String error) {
        displayNotAuthorized(error);
    }

    @Override
    public void onAuthorizationRefused(@Nullable String error) {
        Snackbar.make(findViewById(R.id.coordinator),
                "Refused by user",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onAuthorizationSucceed(@Nullable TokenResponse tokenResponse, @Nullable UserPrivate user) {
        displayAuthorized(tokenResponse, user);
    }

    @Override
    public void onRefreshAccessTokenStarted() {
        displayLoading("on Refresh Access Token Started");
    }

    @Override
    public void onRefreshAccessTokenSucceed(@Nullable TokenResponse tokenResponse, @Nullable UserPrivate user) {
        displayAuthorized(tokenResponse, user);
    }
}