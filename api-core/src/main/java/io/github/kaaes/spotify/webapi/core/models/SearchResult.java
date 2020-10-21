package io.github.kaaes.spotify.webapi.core.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchResult implements Parcelable {

    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        @Override
        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };
    public Pager<Artist> artists;
    public Pager<Album> albums;
    public Pager<Track> tracks;
    public Pager<PlaylistSimple> playlists;

    //TODO add episodes

    protected SearchResult(Parcel in) {
        this.artists = in.readParcelable(Pager.class.getClassLoader());
        this.albums = in.readParcelable(Pager.class.getClassLoader());
        this.tracks = in.readParcelable(Pager.class.getClassLoader());
        this.playlists = in.readParcelable(Pager.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.artists, flags);
        dest.writeParcelable(this.albums, flags);
        dest.writeParcelable(this.tracks, flags);
        dest.writeParcelable(this.playlists, flags);
    }
}
