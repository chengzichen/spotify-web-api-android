package com.pghaz.spotify.webapi.auth

import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import net.openid.appauth.TokenResponse

object SpotifyAuthorizationCallback {

    interface Authorize {
        fun onAuthorizationStarted()

        fun onAuthorizationCancelled()

        fun onAuthorizationFailed(error: String?)

        fun onAuthorizationRefused(error: String?)

        fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
    }

    interface RefreshToken {
        fun onRefreshAccessTokenStarted()

        fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?)
    }
}