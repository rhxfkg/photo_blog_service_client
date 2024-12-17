package com.example.photo_blog_service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private TextView textViewError;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewError = findViewById(R.id.textViewError);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editTextUsername.getText().toString();
                final String password = editTextPassword.getText().toString();

                // 로그인 시도
                new Thread(() -> {
                    try {
                        URL url = new URL("http://10.0.2.2:8000/api/login/");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");

                        // JSON 데이터 생성
                        JSONObject json = new JSONObject();
                        json.put("username", username);
                        json.put("password", password);

                        // 서버에 데이터 전송
                        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                        writer.write(json.toString());
                        writer.flush();

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // 로그인 성공 -> MainActivity로 이동
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // 로그인 실패 -> 에러 메시지 표시
                            runOnUiThread(() -> {
                                textViewError.setVisibility(View.VISIBLE);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            textViewError.setText("An error occurred. Please try again.");
                            textViewError.setVisibility(View.VISIBLE);
                        });
                    }
                }).start();
            }
        });
    }
}

