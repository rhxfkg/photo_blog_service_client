package com.example.photo_blog_service;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Post> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Intent로 전달받은 검색 결과 가져오기
        searchResults = getIntent().getParcelableArrayListExtra("searchResults");

        // RecyclerView 설정
        imageAdapter = new ImageAdapter(searchResults, this);
        recyclerView.setAdapter(imageAdapter);
    }
}
