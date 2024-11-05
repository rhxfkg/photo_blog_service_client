package com.example.photo_blog_service;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Post> favoritePosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Intent로부터 favoritePosts를 가져옵니다.
        favoritePosts = getIntent().getParcelableArrayListExtra("favoritePosts");
        if (favoritePosts == null) {
            favoritePosts = new ArrayList<>();
        }

        imageAdapter = new ImageAdapter(favoritePosts, this);
        recyclerView.setAdapter(imageAdapter);
    }
}
