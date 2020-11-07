package io.github.kaaes.spotify.webapi.core.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <a href="https://developer.spotify.com/web-api/object-model/#cursor-object">Cursor</a>
 */
public class Cursor implements Parcelable {
    public static final Creator<Cursor> CREATOR = new Creator<Cursor>() {
        public Cursor createFromParcel(Parcel source) {
            return new Cursor(source);
        }

        public Cursor[] newArray(int size) {
            return new Cursor[size];
        }
    };
    public String after;
    public String before;

    public Cursor() {
    }

    protected Cursor(Parcel in) {
        this.after = in.readString();
        this.before = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.after);
        dest.writeString(this.before);
    }
}
