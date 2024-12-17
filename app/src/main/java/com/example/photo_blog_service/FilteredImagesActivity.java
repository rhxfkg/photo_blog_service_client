package com.example.photo_blog_service;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilteredImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter; // 기존 어댑터 재사용
    private List<Post> filteredImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_images);
        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerViewFilteredImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Intent에서 필터링된 이미지 리스트를 받기
        filteredImages = getIntent().getParcelableArrayListExtra("filteredImages");

        // 어댑터 설정 및 데이터 업데이트
        imageAdapter = new ImageAdapter(filteredImages, this);
        recyclerView.setAdapter(imageAdapter);
    }
}

