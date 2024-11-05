package com.example.photo_blog_service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView imgView;
    TextView textView;
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private String title;
    private String text;

    String site_url = "http://10.0.2.2:8000";
    Bitmap selectedBitmap = null;

    CloadImage taskDownload;
    PutPost taskUpload;
    // 다크모드 상태를 저장하는 변수
    private boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = findViewById(R.id.imgView);
        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        Button btnFavoriteList = findViewById(R.id.btnFavoriteList);
        // 다크모드 전환 버튼 초기화
        Button darkModeButton = findViewById(R.id.darkModeButton);
        darkModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDarkMode(darkModeButton);
            }
        });
    }

    // 다크모드 전환을 처리하는 메서드
    private void toggleDarkMode(Button darkModeButton) {
        if (isDarkMode) {
            findViewById(R.id.main_layout).setBackgroundColor(Color.WHITE); // 라이트 모드 배경색
            darkModeButton.setText("다크모드");
        } else {
            findViewById(R.id.main_layout).setBackgroundColor(Color.BLACK); // 다크 모드 배경색
            darkModeButton.setText("라이트모드");
        }
        isDarkMode = !isDarkMode; // 상태 반전
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onClickFavoriteList(View v) {
        ArrayList<Post> favoritePosts = new ArrayList<>();
        Log.d("PostListSize", "postList size: " + postList.size());
        for (Post post : postList) {
            if (post.isFavorited()) {
                favoritePosts.add(post);
            }
        }
        Intent intent = new Intent(MainActivity.this, FavoriteListActivity.class);
        intent.putParcelableArrayListExtra("favoritePosts", favoritePosts); // favoritePosts 추가
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgView.setImageBitmap(selectedBitmap);

                // 이미지 업로드 시작
                if (taskUpload != null && taskUpload.getStatus() == AsyncTask.Status.RUNNING) {
                    taskUpload.cancel(true);
                }
                taskUpload = new PutPost(selectedBitmap);
                taskUpload.execute(site_url + "/api_root/Post/");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지 선택 오류", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // MainActivity.java

    private class CloadImage extends AsyncTask<String, Integer, List<Post>> {
        @Override
        protected List<Post> doInBackground(String... urls) {
            List<Post> postList = new ArrayList<>();
            try {
                String apiUrl = urls[0];
                String token = "b6dad56ab6d96772db341385eb7106b823e4b2ab";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    Log.d("API Response", strJson);

                    JSONArray aryJson = new JSONArray(strJson);

                    for (int i = 0; i < aryJson.length(); i++) {
                        JSONObject postJson = (JSONObject) aryJson.get(i);
                        String imageUrl = postJson.getString("image");
                        boolean isFavorited = postJson.optBoolean("is_favorited", false);
                        int postId = postJson.getInt("id");
                        Log.d("Updated Image URL1", "imageUrl: " + imageUrl + ", length: " + imageUrl.length());

                        if (!imageUrl.isEmpty()) {
                            Log.d("Updated Image URL2", imageUrl);
                            imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");

                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();

                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);

                            postList.add(new Post(postId, imageBitmap, title, text, isFavorited));
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return postList;
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            if (posts.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공!");
                postList.addAll(posts);  // 다운로드된 이미지 리스트를 postList에 추가
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(posts, MainActivity.this);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }

    public void toggleFavorite(int postId) {
        new ToggleFavoriteTask(postId).execute(site_url + "/post/" + postId + "/toggle_favorite/");
    }

    private class ToggleFavoriteTask extends AsyncTask<String, Void, Boolean> {
        private int postId;

        public ToggleFavoriteTask(int postId) {
            this.postId = postId;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL urlAPI = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token " + "b6dad56ab6d96772db341385eb7106b823e4b2ab");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getBoolean("is_favorited");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isFavorited) {
            // UI 업데이트
            Toast.makeText(MainActivity.this, isFavorited ? "즐겨찾기 추가됨" : "즐겨찾기 취소됨", Toast.LENGTH_SHORT).show();
        }
    }


    private class PutPost extends AsyncTask<String, Void, String> {
        private Bitmap bitmap;

        public PutPost(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "Upload Failed";
            try {
                String apiUrl = urls[0];
                String token = "b6dad56ab6d96772db341385eb7106b823e4b2ab";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();

                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"title\"\r\n\r\n");
                outputStream.writeBytes("제목\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"text\"\r\n\r\n");
                outputStream.writeBytes("API 내용\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"author\"\r\n\r\n");
                outputStream.writeBytes("1\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"created_date\"\r\n\r\n");
                outputStream.writeBytes("2022-06-07T18:34:00+09:00\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"published_date\"\r\n\r\n");
                outputStream.writeBytes("2022-06-07T18:34:00+09:00\r\n");

// 이미지 전송
                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"\r\n");
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                outputStream.write(imageData);
                outputStream.writeBytes("\r\n--boundary--\r\n");
                outputStream.flush();
                outputStream.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = "Upload Successful";
                } else {
                    result = "Upload Failed with response code: " + responseCode;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("Upload Successful".equals(result)) {
                textView.setText("이미지 업로드 성공!");
            } else {
                textView.setText("이미지 업로드 실패!");
            }
        }
    }
}