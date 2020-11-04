package io.github.kaaes.spotify.webapi.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://developer.spotify.com/web-api/object-model/#paging-object">Paging object model</a>
 *
 * @param <T> expected object that is paged
 */
public class Pager<T extends Parcelable> implements Parcelable {
    public String href;
    public List<T> items;
    public int limit;
    public String next;
    public int offset;
    public String previous;
    public int total;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(href);

        if (items == null || items.size() == 0)
            dest.writeInt(0);
        else {
            dest.writeInt(items.size());
            final Class<?> objectsType = items.get(0).getClass();
            dest.writeSerializable(objectsType);
            dest.writeList(items);
        }

        dest.writeInt(limit);
        dest.writeString(next);
        dest.writeInt(offset);
        dest.writeString(previous);
        dest.writeInt(total);
    }

    public Pager() {
    }

    protected Pager(Parcel in) {
        this.href = in.readString();

        int size = in.readInt();
        if (size == 0) {
            items = new ArrayList<>();
        } else {
            Class<?> type = (Class<?>) in.readSerializable();
            items = new ArrayList<>(size);
            in.readList(items, type.getClassLoader());
        }

        this.limit = in.readInt();
        this.next = in.readString();
        this.offset = in.readInt();
        this.previous = in.readString();
        this.total = in.readInt();
    }

    public static final Parcelable.Creator<Pager> CREATOR = new Parcelable.Creator<Pager>() {
        public Pager createFromParcel(Parcel source) {
            return new Pager(source);
        }

        public Pager[] newArray(int size) {
            return new Pager[size];
        }
    };

}
