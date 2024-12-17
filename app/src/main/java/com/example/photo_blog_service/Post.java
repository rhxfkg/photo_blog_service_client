package com.example.photo_blog_service;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Post implements Parcelable {
    private int id;
    private Bitmap image;
    private String title;      // 제목
    private String text;       // 내용
    private boolean isFavorited;
    private List<String> tags; // 태그 목록 추가

    public Post(int id, Bitmap image, String title, String text, boolean isFavorited, List<String> tags) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.text = text;
        this.isFavorited = isFavorited;
        this.tags = tags;
    }

    protected Post(Parcel in) {
        id = in.readInt();
        image = in.readParcelable(Bitmap.class.getClassLoader());
        title = in.readString();
        text = in.readString();
        isFavorited = in.readByte() != 0;
        tags = in.createStringArrayList(); // 태그 읽기
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
        dest.writeString(title);
        dest.writeString(text);
        dest.writeByte((byte) (isFavorited ? 1 : 0));
        dest.writeStringList(tags); // 태그 쓰기
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
