package io.github.kaaes.spotify.webapi.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Context implements Parcelable {

    public static final Parcelable.Creator<Context> CREATOR = new Parcelable.Creator<Context>() {
        @Override
        public Context createFromParcel(Parcel in) {
            return new Context(in);
        }

        @Override
        public Context[] newArray(int size) {
            return new Context[size];
        }
    };
    public String uri;
    public String href;
    public Map<String, String> external_urls;
    public String type;

    protected Context(Parcel in) {
        this.uri = in.readString();
        this.href = in.readString();
        this.external_urls = in.readHashMap(Map.class.getClassLoader());
        this.type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeString(this.href);
        dest.writeMap(this.external_urls);
        dest.writeString(this.type);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}