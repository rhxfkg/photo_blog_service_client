package com.example.photo_blog_service;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 2;
    private EditText editTitle, editText;
    private ImageView imageView;
    private Bitmap selectedBitmap;
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editTitle = findViewById(R.id.editTitle);
        editText = findViewById(R.id.editText);
        imageView = findViewById(R.id.imageViewEdit);

        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);
        Button btnDelete = findViewById(R.id.btnDelete);

        // Get post data from the intent
        Intent intent = getIntent();
        postId = intent.getIntExtra("postId", -1);
        editTitle.setText(intent.getStringExtra("title"));
        editText.setText(intent.getStringExtra("text"));
        selectedBitmap = intent.getParcelableExtra("image");

        if (selectedBitmap != null) {
            imageView.setImageBitmap(selectedBitmap);
        }

        // Image selection
        imageView.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
        });

        // Save changes
        btnSaveChanges.setOnClickListener(v -> confirmSaveChanges());

        // Delete post
        btnDelete.setOnClickListener(v -> confirmDeletePost());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void confirmSaveChanges() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Changes")
                .setMessage("Are you sure you want to save these changes?")
                .setPositiveButton("Yes", (dialog, which) -> saveChanges())
                .setNegativeButton("No", null)
                .show();
    }

    private void saveChanges() {
        String newTitle = editTitle.getText().toString();
        String newText = editText.getText().toString();
        new UpdatePostTask(postId, newTitle, newText, selectedBitmap).execute("http://10.0.2.2:8000/api_root/Post/" + postId + "/");
    }

    private void confirmDeletePost() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> deletePost())
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePost() {
        new DeletePostTask(postId).execute("http://10.0.2.2:8000/api_root/Post/" + postId + "/");
    }

    // Task to update post
    private class UpdatePostTask extends AsyncTask<String, Void, String> {
        private int postId;
        private String title, text;
        private Bitmap image;

        public UpdatePostTask(int postId, String title, String text, Bitmap image) {
            this.postId = postId;
            this.title = title;
            this.text = text;
            this.image = image;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                String apiUrl = urls[0];
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Token YOUR_API_TOKEN");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");
                conn.setDoOutput(true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"title\"\r\n\r\n");
                outputStream.writeBytes(title + "\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"text\"\r\n\r\n");
                outputStream.writeBytes(text + "\r\n");

                outputStream.writeBytes("--boundary\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"update.jpg\"\r\n");
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                outputStream.write(imageData);
                outputStream.writeBytes("\r\n--boundary--\r\n");
                outputStream.flush();
                outputStream.close();

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK ? "Update successful" : "Update failed";

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Update failed";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditActivity.this, result, Toast.LENGTH_SHORT).show();
            finish(); // Return to main screen after updating
        }
    }

    // Task to delete post
    private class DeletePostTask extends AsyncTask<String, Void, String> {
        private int postId;

        public DeletePostTask(int postId) {
            this.postId = postId;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL urlAPI = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Token YOUR_API_TOKEN");

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_NO_CONTENT ? "Delete successful" : "Delete failed";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Delete failed";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditActivity.this, result, Toast.LENGTH_SHORT).show();
            finish(); // Return to main screen after deletion
        }
    }
}
