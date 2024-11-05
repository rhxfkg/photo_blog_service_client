// PostManager.java
package com.example.photo_blog_service;

import java.util.ArrayList;
import java.util.List;

public class PostManager {
    private static PostManager instance;
    private List<Post> postList = new ArrayList<>();

    private PostManager() {}

    public static PostManager getInstance() {
        if (instance == null) {
            instance = new PostManager();
        }
        return instance;
    }

    public List<Post> getPostList() {
        return postList;
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
    }
}
