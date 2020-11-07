package io.github.kaaes.spotify.webapi.core.models;

import android.os.Parcel;
import android.os.Parcelable;

public class RecentlyPlayedTrack implements Parcelable {

    public static final Creator<RecentlyPlayedTrack> CREATOR = new Creator<RecentlyPlayedTrack>() {
        @Override
        public RecentlyPlayedTrack createFromParcel(Parcel in) {
            return new RecentlyPlayedTrack(in);
        }

        @Override
        public RecentlyPlayedTrack[] newArray(int size) {
            return new RecentlyPlayedTrack[size];
        }
    };
    public Context context;
    public String played_at;
    public Track track;

    protected RecentlyPlayedTrack(Parcel in) {
        this.context = in.readParcelable(Context.class.getClassLoader());
        this.played_at = in.readString();
        this.track = in.readParcelable(Track.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.context, flags);
        dest.writeString(this.played_at);
        dest.writeParcelable(this.track, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}