// Post.java
package com.example.photo_blog_service;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private int id;
    private Bitmap image;
    private boolean isFavorited;

    public Post(int id, Bitmap image, boolean isFavorited) {
        this.id = id;
        this.image = image;
        this.isFavorited = isFavorited;
    }

    protected Post(Parcel in) {
        id = in.readInt();
        image = in.readParcelable(Bitmap.class.getClassLoader());
        isFavorited = in.readByte() != 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(image, flags);
        dest.writeByte((byte) (isFavorited ? 1 : 0));
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
}
