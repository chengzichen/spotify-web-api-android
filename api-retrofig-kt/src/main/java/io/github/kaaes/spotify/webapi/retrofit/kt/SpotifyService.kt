package io.github.kaaes.spotify.webapi.retrofit.kt

import io.github.kaaes.spotify.webapi.core.models.Album
import io.github.kaaes.spotify.webapi.core.models.Albums
import io.github.kaaes.spotify.webapi.core.models.AlbumsPager
import io.github.kaaes.spotify.webapi.core.models.Artist
import io.github.kaaes.spotify.webapi.core.models.Artists
import io.github.kaaes.spotify.webapi.core.models.ArtistsCursorPager
import io.github.kaaes.spotify.webapi.core.models.ArtistsPager
import io.github.kaaes.spotify.webapi.core.models.AudioFeaturesTrack
import io.github.kaaes.spotify.webapi.core.models.AudioFeaturesTracks
import io.github.kaaes.spotify.webapi.core.models.CategoriesPager
import io.github.kaaes.spotify.webapi.core.models.Category
import io.github.kaaes.spotify.webapi.core.models.CursorPager
import io.github.kaaes.spotify.webapi.core.models.FeaturedPlaylists
import io.github.kaaes.spotify.webapi.core.models.NewReleases
import io.github.kaaes.spotify.webapi.core.models.Pager
import io.github.kaaes.spotify.webapi.core.models.Playlist
import io.github.kaaes.spotify.webapi.core.models.PlaylistFollowPrivacy
import io.github.kaaes.spotify.webapi.core.models.PlaylistSimple
import io.github.kaaes.spotify.webapi.core.models.PlaylistTrack
import io.github.kaaes.spotify.webapi.core.models.PlaylistsPager
import io.github.kaaes.spotify.webapi.core.models.RecentlyPlayedTrack
import io.github.kaaes.spotify.webapi.core.models.Recommendations
import io.github.kaaes.spotify.webapi.core.models.Result
import io.github.kaaes.spotify.webapi.core.models.SavedAlbum
import io.github.kaaes.spotify.webapi.core.models.SavedTrack
import io.github.kaaes.spotify.webapi.core.models.SearchResult
import io.github.kaaes.spotify.webapi.core.models.SeedsGenres
import io.github.kaaes.spotify.webapi.core.models.SnapshotId
import io.github.kaaes.spotify.webapi.core.models.Track
import io.github.kaaes.spotify.webapi.core.models.Tracks
import io.github.kaaes.spotify.webapi.core.models.TracksPager
import io.github.kaaes.spotify.webapi.core.models.TracksToRemove
import io.github.kaaes.spotify.webapi.core.models.TracksToRemoveWithPosition
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import io.github.kaaes.spotify.webapi.core.models.UserPublic
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface SpotifyService {
    /************
     * Profiles *
     */
    @GET("me")
    suspend fun me(): UserPrivate

    /**
     * Get a user's profile information.
     *
     * @param userId The user's User ID
     * @return The user's profile information.
     * @see [Get User's Public Profile](https://developer.spotify.com/web-api/get-users-profile/)
     */
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String?): UserPublic

    /*************
     * Playlists *
     */
    @GET("me/playlists")
    suspend fun myPlaylists(): Pager<PlaylistSimple>

    /**
     * Get a list of the playlists owned or followed by the current Spotify user.
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-a-list-of-current-users-playlists/)
     * @return List of user's playlists wrapped in a `Pager` object
     */
    @GET("me/playlists")
    suspend fun getMyPlaylists(@QueryMap options: Map<String, Any>): Pager<PlaylistSimple>

    /**
     * Get a list of the playlists owned or followed by a Spotify user.
     *
     * @param userId  The user's Spotify user ID.
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-users-playlists/)
     * @return List of user's playlists wrapped in a `Pager` object
     * @see [Get a List of a User’s Playlists](https://developer.spotify.com/web-api/get-list-users-playlists/)
     */
    @GET("users/{id}/playlists")
    suspend fun getPlaylists(
        @Path("id") userId: String,
        @QueryMap options: Map<String, Any>
    ):Pager<PlaylistSimple>?

    /**
     * Get a list of the playlists owned or followed by a Spotify user.
     *
     * @param userId The user's Spotify user ID.
     * @return List of user's playlists wrapped in a `Pager` object
     * @see [Get a List of a User’s Playlists](https://developer.spotify.com/web-api/get-list-users-playlists/)
     */
    @GET("users/{id}/playlists")
    suspend fun getPlaylists(@Path("id") userId: String?):Pager<PlaylistSimple>?

    /**
     * Get a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-playlist/)
     * @return Requested Playlist.
     * @see [Get a Playlist](https://developer.spotify.com/web-api/get-playlist/)
     */
    @GET("users/{user_id}/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String?, Any>
    ):Playlist

    /**
     * Get a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @return Requested Playlist.
     * @see [Get a Playlist](https://developer.spotify.com/web-api/get-playlist/)
     */
    @GET("users/{user_id}/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ):Playlist

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-playlists-tracks/)
     * @return List of playlist's tracks wrapped in a `Pager` object
     * @see [Get a Playlist’s Tracks](https://developer.spotify.com/web-api/get-playlists-tracks/)
     */
    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String?, Any>
    ):Pager<PlaylistTrack>?

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @return List of playlist's tracks wrapped in a `Pager` object
     * @see [Get a Playlist’s Tracks](https://developer.spotify.com/web-api/get-playlists-tracks/)
     */
    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ):Pager<PlaylistTrack>?

    /**
     * Create a playlist
     *
     * @param userId  The playlist's owner's User ID
     * @param options The body parameters
     * @return The created playlist
     * @see [Create a Playlist](https://developer.spotify.com/web-api/create-playlist/)
     */
    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Path("user_id") userId: String?,
        @Body options: Map<String?, Any>
    ):Playlist

    /**
     * Add tracks to a playlist
     *
     * @param userId          The owner of the playlist
     * @param playlistId      The playlist's ID
     * @param queryParameters Query parameters
     * @param body            The body parameters
     * @return A snapshot ID (the version of the playlist)
     * @see [Add Tracks to a Playlist](https://developer.spotify.com/web-api/add-tracks-to-playlist/)
     */
    @POST("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun addTracksToPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap queryParameters: Map<String?, Any>,
        @Body body : Map<String?, Any>
    ):SnapshotId

    /**
     * Remove one or more tracks from a user’s playlist.
     *
     * @param userId         The owner of the playlist
     * @param playlistId     The playlist's Id
     * @param tracksToRemove A list of tracks to remove
     * @return A snapshot ID (the version of the playlist)
     * @see [Remove Tracks from a Playlist](https://developer.spotify.com/web-api/remove-tracks-playlist/)
     */
    @DELETE("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun removeTracksFromPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body tracksToRemove: TracksToRemove?
    ):SnapshotId

    /**
     * Remove one or more tracks from a user’s playlist.
     *
     * @param userId                     The owner of the playlist
     * @param playlistId                 The playlist's Id
     * @param tracksToRemoveWithPosition A list of tracks to remove, together with their specific positions
     * @return A snapshot ID (the version of the playlist)
     * @see [Remove Tracks from a Playlist](https://developer.spotify.com/web-api/remove-tracks-playlist/)
     */
    @DELETE("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun removeTracksFromPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body tracksToRemoveWithPosition: TracksToRemoveWithPosition?
    ):SnapshotId

    /**
     * Replace all the tracks in a playlist, overwriting its existing tracks. This powerful request can be useful for
     * replacing tracks, re-ordering existing tracks, or clearing the playlist.
     *
     * @param userId     The owner of the playlist
     * @param playlistId The playlist's Id
     * @param trackUris  A list of comma-separated track uris
     * @return An empty result
     * @see [Replace a Playlist’s Tracks](https://developer.spotify.com/web-api/replace-playlists-tracks/)
     */
    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun replaceTracksInPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Query("uris") trackUris: String?
    ):Result

    /**
     * Change a playlist’s name and public/private state. (The user must, of course, own the playlist.)
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The playlist's Id
     * @param body       The body parameters. For list of supported parameters see [endpoint documentation](https://developer.spotify.com/web-api/change-playlist-details/)
     * @return An empty result
     * @see [Change a Playlist's Details](https://developer.spotify.com/web-api/change-playlist-details/)
     */
    @PUT("users/{user_id}/playlists/{playlist_id}")
    suspend fun changePlaylistDetails(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body body: Map<String?, Any>
    ):Result

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @return An empty result
     * @see [Follow a Playlist](https://developer.spotify.com/web-api/follow-playlist/)
     */
    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
    suspend fun followPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ):Result

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param userId                The Spotify user ID of the user who owns the playlist.
     * @param playlistId            The Spotify ID of the playlist
     * @param playlistFollowPrivacy The privacy state of the playlist
     * @return An empty result
     * @see [Follow a Playlist](https://developer.spotify.com/web-api/follow-playlist/)
     */
    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
    suspend fun followPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body playlistFollowPrivacy: PlaylistFollowPrivacy?
    ):Result

    /**
     * Unfollow a Playlist
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @return An empty result
     * @see [Unfollow a Playlist](https://developer.spotify.com/web-api/unfollow-playlist/)
     */
    @DELETE("users/{user_id}/playlists/{playlist_id}/followers")
    suspend fun unfollowPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ):Result

    /**
     * Reorder a Playlist's tracks
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @param body       The body parameters. For list of supported parameters see [endpoint documentation](https://developer.spotify.com/web-api/reorder-playlists-tracks/)
     * @return A snapshot ID (the version of the playlist)
     * @see [Reorder a Playlist](https://developer.spotify.com/web-api/reorder-playlists-tracks/)
     */
    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
    suspend fun reorderPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body body: Map<String?, Any>
    ):SnapshotId


    /**********
     * Albums *
     */
    /**
     * Get Spotify catalog information for a single album.
     *
     * @param albumId The Spotify ID for the album.
     * @return Requested album information
     * @see [Get an Album](https://developer.spotify.com/web-api/get-album/)
     */
    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") albumId: String?):Album

    /**
     * Get Spotify catalog information for a single album.
     *
     * @param albumId The Spotify ID for the album.
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-album/)
     * @return Requested album information
     * @see [Get an Album](https://developer.spotify.com/web-api/get-album/)
     */
    @GET("albums/{id}")
    suspend fun getAlbum(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String?, Any>
    ):Album

    /**
     * Get Spotify catalog information for multiple albums identified by their Spotify IDs.
     *
     * @param albumIds A comma-separated list of the Spotify IDs for the albums
     * @return Object whose key is "albums" and whose value is an array of album objects.
     * @see [Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)
     */
    @GET("albums")
    suspend fun getAlbums(@Query("ids") albumIds: String?):Albums

    /**
     * Get Spotify catalog information for multiple albums identified by their Spotify IDs.
     *
     * @param albumIds A comma-separated list of the Spotify IDs for the albums
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-several-albums/)
     * @return Object whose key is "albums" and whose value is an array of album objects.
     * @see [Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)
     */
    @GET("albums")
    suspend fun getAlbums(
        @Query("ids") albumIds: String?,
        @QueryMap options: Map<String?, Any>
    ):Albums

    /**
     * Get Spotify catalog information about an album’s tracks.
     *
     * @param albumId The Spotify ID for the album.
     * @return List of simplified album objects wrapped in a Pager object
     * @see [Get an Album’s Tracks](https://developer.spotify.com/web-api/get-albums-tracks/)
     */
    @GET("albums/{id}/tracks")
    suspend fun getAlbumTracks(@Path("id") albumId: String?):Pager<Track>?

    /**
     * Get Spotify catalog information about an album’s tracks.
     *
     * @param albumId The Spotify ID for the album.
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-albums-tracks/)
     * @return List of simplified album objects wrapped in a Pager object
     * @see [Get an Album’s Tracks](https://developer.spotify.com/web-api/get-albums-tracks/)
     */
    @GET("albums/{id}/tracks")
    suspend fun getAlbumTracks(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String?, Any>
    ):Pager<Track>?


    /***********
     * Artists *
     */
    /**
     * Get Spotify catalog information for a single artist identified by their unique Spotify ID.
     *
     * @param artistId The Spotify ID for the artist.
     * @return Requested artist information
     * @see [Get an Artist](https://developer.spotify.com/web-api/get-artist/)
     */
    @GET("artists/{id}")
    suspend fun getArtist(@Path("id") artistId: String?):Artist

    /**
     * Get Spotify catalog information for several artists based on their Spotify IDs.
     *
     * @param artistIds A comma-separated list of the Spotify IDs for the artists
     * @return An object whose key is "artists" and whose value is an array of artist objects.
     * @see [Get Several Artists](https://developer.spotify.com/web-api/get-several-artists/)
     */
    @GET("artists")
    suspend fun getArtists(@Query("ids") artistIds: String?):Artists

    /**
     * Get Spotify catalog information about an artist’s albums.
     *
     * @param artistId The Spotify ID for the artist.
     * @return An array of simplified album objects wrapped in a paging object.
     * @see [Get an Artist's Albums](https://developer.spotify.com/web-api/get-artists-albums/)
     */
    @GET("artists/{id}/albums")
    suspend fun getArtistAlbums(@Path("id") artistId: String?):Pager<Album>?

    /**
     * Get Spotify catalog information about an artist’s albums.
     *
     * @param artistId The Spotify ID for the artist.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-artists-albums/)
     * @return An array of simplified album objects wrapped in a paging object.
     * @see [Get an Artist's Albums](https://developer.spotify.com/web-api/get-artists-albums/)
     */
    @GET("artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") artistId: String?,
        @QueryMap options: Map<String?, Any>
    ):Pager<Album>?

    /**
     * Get Spotify catalog information about an artist’s top tracks by country.
     *
     * @param artistId The Spotify ID for the artist.
     * @param country  The country: an ISO 3166-1 alpha-2 country code.
     * @return An object whose key is "tracks" and whose value is an array of track objects.
     * @see [Get an Artist’s Top Tracks](https://developer.spotify.com/web-api/get-artists-top-tracks/)
     */
    @GET("artists/{id}/top-tracks")
    suspend fun getArtistTopTrack(
        @Path("id") artistId: String?,
        @Query("country") country: String?
    ):Tracks

    /**
     * Get Spotify catalog information about artists similar to a given artist.
     *
     * @param artistId The Spotify ID for the artist.
     * @return An object whose key is "artists" and whose value is an array of artist objects.
     * @see [Get an Artist’s Related Artists](https://developer.spotify.com/web-api/get-related-artists/)
     */
    @GET("artists/{id}/related-artists")
    suspend fun getRelatedArtists(@Path("id") artistId: String?):Artists


    /**********
     * Tracks *
     */
    /**
     * Get Spotify catalog information for a single track identified by their unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     * @return Requested track information
     * @see [Get a Track](https://developer.spotify.com/web-api/get-track/)
     */
    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String?):Track

    /**
     * Get Spotify catalog information for a single track identified by their unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-track/)
     * @return Requested track information
     * @see [Get a Track](https://developer.spotify.com/web-api/get-track/)
     */
    @GET("tracks/{id}")
    suspend fun getTrack(
        @Path("id") trackId: String?,
        @QueryMap options: Map<String?, Any>
    ):Track

    /**
     * Get Several Tracks
     *
     * @param trackIds A comma-separated list of the Spotify IDs for the tracks
     * @return An object whose key is "tracks" and whose value is an array of track objects.
     * @see [Get Several Tracks](https://developer.spotify.com/web-api/get-several-tracks/)
     */
    @GET("tracks")
    suspend fun getTracks(@Query("ids") trackIds: String?):Tracks

    /**
     * Get Several Tracks
     *
     * @param trackIds A comma-separated list of the Spotify IDs for the tracks
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-several-tracks/)
     * @return An object whose key is "tracks" and whose value is an array of track objects.
     * @see [Get Several Tracks](https://developer.spotify.com/web-api/get-several-tracks/)
     */
    @GET("tracks")
    suspend fun getTracks(
        @Query("ids") trackIds: String?,
        @QueryMap options: Map<String?, Any>
    ):Tracks


    /**********
     * Browse *
     */
    @GET("browse/featured-playlists")
    suspend fun featuredPlaylists():FeaturedPlaylists

    /**
     * Get a list of Spotify featured playlists (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-featured-playlists/)
     * @return n FeaturedPlaylists object with the featured playlists
     * @see [Get a List of Featured Playlists](https://developer.spotify.com/web-api/get-list-featured-playlists/)
     */
    @GET("browse/featured-playlists")
    suspend fun getFeaturedPlaylists(@QueryMap options: Map<String?, Any>):FeaturedPlaylists

    @GET("browse/new-releases")
    suspend fun newReleases():NewReleases

    /**
     * Get a list of new album releases featured in Spotify (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-new-releases/)
     * @return A NewReleases object with the new album releases
     * @see [Get a List of New Releases](https://developer.spotify.com/web-api/get-list-new-releases/)
     */
    @GET("browse/new-releases")
    suspend fun getNewReleases(@QueryMap options: Map<String?, Any>):NewReleases

    /**
     * Retrieve Spotify categories. Categories used to tag items in
     * Spotify (on, for example, the Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters.
     * @return A paging object containing categories.
     * @see [Get a List of Categories](https://developer.spotify.com/web-api/get-list-categories/)
     */
    @GET("browse/categories")
    suspend fun getCategories(@QueryMap options: Map<String?, Any>):CategoriesPager

    /**
     * Retrieve a Spotify category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @return A Spotify category.
     * @see [Get a Spotify Category](https://developer.spotify.com/web-api/get-category/)
     */
    @GET("browse/categories/{category_id}")
    suspend fun getCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String?, Any>
    ):Category

    /**
     * Retrieve playlists for a Spotify Category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @return Playlists for a Spotify Category.
     * @see [Get playlists for a Spotify Category](https://developer.spotify.com/web-api/get-categorys-playlists/)
     */
    @GET("browse/categories/{category_id}/playlists")
    suspend fun getPlaylistsForCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String?, Any>
    ):PlaylistsPager


    /************************
     * Library / Your Music *
     */
    @GET("me/tracks")
    suspend fun mySavedTracks():Pager<SavedTrack>?

    /**
     * Get a list of the songs saved in the current Spotify user’s “Your Music” library.
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-saved-tracks/)
     * @return A paginated list of saved tracks
     * @see [Get a User’s Saved Tracks](https://developer.spotify.com/web-api/get-users-saved-tracks/)
     */
    @GET("me/tracks")
    suspend fun getMySavedTracks(@QueryMap options: Map<String?, Any>):Pager<SavedTrack>?

    /**
     * Check if one or more tracks is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An array with boolean values that indicate whether the tracks are in the current Spotify user’s “Your Music” library.
     * @see [Check User’s Saved Tracks](https://developer.spotify.com/web-api/check-users-saved-tracks/)
     */
    @GET("me/tracks/contains")
    suspend fun containsMySavedTracks(@Query("ids") ids: String?):Array<Boolean>?

    /**
     * Save one or more tracks to the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An empty result
     * @see [Save Tracks for User](https://developer.spotify.com/web-api/save-tracks-user/)
     */
    @PUT("me/tracks")
    suspend fun addToMySavedTracks(@Query("ids") ids: String?):Result

    /**
     * Remove one or more tracks from the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An empty result
     * @see [Remove User’s Saved Tracks](https://developer.spotify.com/web-api/remove-tracks-user/)
     */
    @DELETE("me/tracks")
    suspend fun removeFromMySavedTracks(@Query("ids") ids: String?):Result

    @GET("me/albums")
    suspend fun mySavedAlbums():Pager<SavedAlbum>?

    /**
     * Get a list of the albums saved in the current Spotify user’s “Your Music” library.
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-saved-albums/)
     * @return A paginated list of saved albums
     * @see [Get a User’s Saved Albums](https://developer.spotify.com/web-api/get-users-saved-albums/)
     */
    @GET("me/albums")
    suspend fun getMySavedAlbums(@QueryMap options: Map<String?, Any>):Pager<SavedAlbum>?

    /**
     * Check if one or more albums is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An array with boolean values that indicate whether the albums are in the current Spotify user’s “Your Music” library.
     * @see [Check User’s Saved Albums](https://developer.spotify.com/web-api/check-users-saved-albums/)
     */
    @GET("me/albums/contains")
    suspend fun containsMySavedAlbums(@Query("ids") ids: String?):Array<Boolean>?

    /**
     * Save one or more albums to the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An empty result
     * @see [Save Albums for User](https://developer.spotify.com/web-api/save-albums-user/)
     */
    @PUT("me/albums")
    suspend fun addToMySavedAlbums(@Query("ids") ids: String?):Result

    /**
     * Remove one or more albums from the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An empty result
     * @see [Remove User’s Saved Albums](https://developer.spotify.com/web-api/remove-albums-user/)
     */
    @DELETE("me/albums")
    suspend fun removeFromMySavedAlbums(@Query("ids") ids: String?):Result

    /**********
     * Follow *
     */
    /**
     * Add the current user as a follower of one or more Spotify users.
     *
     * @param ids A comma-separated list of the Spotify IDs for the users
     * @return An empty result
     * @see [Follow Artists or Users](https://developer.spotify.com/web-api/follow-artists-users/)
     */
    @PUT("me/following?type=user")
    suspend fun followUsers(@Query("ids") ids: String?):Result

    /**
     * Add the current user as a follower of one or more Spotify artists.
     *
     * @param ids A comma-separated list of the Spotify IDs for the artists
     * @return An empty result
     * @see [Follow Artists or Users](https://developer.spotify.com/web-api/follow-artists-users/)
     */
    @PUT("me/following?type=artist")
    suspend fun followArtists(@Query("ids") ids: String?):Result

    /**
     * Remove the current user as a follower of one or more Spotify users.
     *
     * @param ids A comma-separated list of the Spotify IDs for the users
     * @return An empty result
     * @see [Unfollow Artists or Users](https://developer.spotify.com/web-api/unfollow-artists-users/)
     */
    @DELETE("me/following?type=user")
    suspend fun unfollowUsers(@Query("ids") ids: String?):Result

    /**
     * Remove the current user as a follower of one or more Spotify artists.
     *
     * @param ids A comma-separated list of the Spotify IDs for the artists
     * @return An empty result
     * @see [Unfollow Artists or Users](https://developer.spotify.com/web-api/unfollow-artists-users/)
     */
    @DELETE("me/following?type=artist")
    suspend fun unfollowArtists(@Query("ids") ids: String?):Result

    /**
     * Check to see if the current user is following one or more other Spotify users.
     *
     * @param ids A comma-separated list of the Spotify IDs for the users
     * @return An array with boolean values indicating whether the users are followed
     * @see [Check if Current User Follows Artists or Users](https://developer.spotify.com/web-api/check-current-user-follows/)
     */
    @GET("me/following/contains?type=user")
    suspend fun isFollowingUsers(@Query("ids") ids: String?):Array<Boolean>?

    /**
     * Check to see if the current user is following one or more other Spotify artists.
     *
     * @param ids A comma-separated list of the Spotify IDs for the artists
     * @return An array with boolean values indicating whether the artists are followed
     * @see [Check if Current User Follows Artists or Users](https://developer.spotify.com/web-api/check-current-user-follows/)
     */
    @GET("me/following/contains?type=artist")
    suspend fun isFollowingArtists(@Query("ids") ids: String?):Array<Boolean>?

    /**
     * Check to see if one or more Spotify users are following a specified playlist.
     *
     * @param userId     The Spotify user ID of the person who owns the playlist.
     * @param playlistId The Spotify ID of the playlist.
     * @param ids        A comma-separated list of the Spotify IDs for the users
     * @return An array with boolean values indicating whether the playlist is followed by the users
     * @see [Check if Users Follow a Playlist](https://developer.spotify.com/web-api/check-user-following-playlist/)
     */
    @GET("users/{user_id}/playlists/{playlist_id}/followers/contains")
    suspend fun areFollowingPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Query("ids") ids: String?
    ):Array<Boolean>?

    @GET("me/following?type=artist")
    suspend fun followedArtists():ArtistsCursorPager


    /**
     * Get the current user's followed artists.
     *
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-followed-artists/)
     * @return Object containing a list of artists that user follows wrapped in a cursor object.
     * @see [Get User's Followed Artists](https://developer.spotify.com/web-api/get-followed-artists/)
     */
    @GET("me/following?type=artist")
    suspend fun getFollowedArtists(@QueryMap options: Map<String?, Any>):ArtistsCursorPager

    /**********
     * Search *
     */
    /**
     * Get Spotify catalog information that match a keyword string.
     * Search results include hits from all the specified item types.
     *
     * @param q    The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param type Valid types are: album , artist, playlist, track, show and episode.
     * @return A SearchResult object with item for each type
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search")
    suspend fun search(@Query("q") q: String?, @Query("type") type: String?):SearchResult

    /**
     * Get Spotify catalog information that match a keyword string.
     * Search results include hits from all the specified item types.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param type    Valid types are: album , artist, playlist, track, show and episode.
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @return A SearchResult object with item for each type
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search")
    suspend fun search(
        @Query("q") q: String?,
        @Query("type") type: String?,
        @QueryMap options: Map<String?, Any>
    ):SearchResult

    /**
     * Get Spotify catalog information about tracks that match a keyword string.
     *
     * @param q The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=track")
    suspend fun searchTracks(@Query("q") q: String?):TracksPager

    /**
     * Get Spotify catalog information about tracks that match a keyword string.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=track")
    suspend fun searchTracks(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any>
    ):TracksPager

    /**
     * Get Spotify catalog information about artists that match a keyword string.
     *
     * @param q The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=artist")
    suspend fun searchArtists(@Query("q") q: String?):ArtistsPager

    /**
     * Get Spotify catalog information about artists that match a keyword string.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=artist")
    suspend fun searchArtists(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any>
    ):ArtistsPager

    /**
     * Get Spotify catalog information about albums that match a keyword string.
     *
     * @param q The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=album")
    suspend fun searchAlbums(@Query("q") q: String?):AlbumsPager

    /**
     * Get Spotify catalog information about albums that match a keyword string.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=album")
    suspend fun searchAlbums(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any>
    ):AlbumsPager

    /**
     * Get Spotify catalog information about playlists that match a keyword string.
     *
     * @param q The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=playlist")
    suspend fun searchPlaylists(@Query("q") q: String?):PlaylistsPager

    /**
     * Get Spotify catalog information about playlists that match a keyword string.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @return A paginated list of results
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("search?type=playlist")
    suspend fun searchPlaylists(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any>
    ):PlaylistsPager

    /******************
     * Audio features *
     */
    /**
     * Get audio features for multiple tracks based on their Spotify IDs.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks. Maximum: 100 IDs
     * @return An object whose key is "audio_features" and whose value is an array of audio features objects.
     */
    @GET("audio-features")
    suspend fun getTracksAudioFeatures(@Query("ids") ids: String?):AudioFeaturesTracks


    /**
     * Get audio feature information for a single track identified by its unique Spotify ID.
     *
     * @param id The Spotify ID for the track.
     * @return Audio features object
     */
    @GET("audio-features/{id}")
    suspend fun getTrackAudioFeatures(@Path("id") id: String?):AudioFeaturesTrack

    /*******************
     * Recommendations *
     */
    /**
     * Create a playlist-style listening experience based on seed artists, tracks and genres.
     *
     * @param options Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-recommendations/)
     * @return Recommendations response object
     */
    @GET("recommendations")
    suspend fun getRecommendations(@QueryMap options: Map<String?, Any>):Recommendations


    @GET("recommendations/available-genre-seeds")
    suspend fun seedsGenres():SeedsGenres


    /*****************************
     * User Top Artists & Tracks *
     */
    @GET("me/top/artists")
    suspend fun topArtists():Pager<Artist>?

    /**
     * Get the current user’s top artists based on calculated affinity.
     *
     * @param options Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-top-artists-and-tracks/)
     * @return The objects whose response body contains an artists or tracks object.
     * The object in turn contains a paging object of Artists or Tracks
     */
    @GET("me/top/artists")
    suspend fun getTopArtists(@QueryMap options: Map<String?, Any>):Pager<Artist>?

    @GET("me/top/tracks")
    suspend fun topTracks():Pager<Track>?

    /**
     * Get the current user’s top tracks based on calculated affinity.
     *
     * @param options Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-top-artists-and-tracks/)
     * @return The objects whose response body contains an artists or tracks object.
     * The object in turn contains a paging object of Artists or Tracks
     */
    @GET("me/top/tracks")
    suspend fun getTopTracks(@QueryMap options: Map<String?, Any>):Pager<Track>?

    /**
     * Get the Current User's Recently Played Tracks.
     *
     * @param options Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/documentation/web-api/reference-beta/#endpoint-get-recently-played)
     * @return Recently played tracks with their context (e.g: while playing a playlist)
     */
    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(@QueryMap options: Map<String?, Any>):CursorPager<RecentlyPlayedTrack>?
}
