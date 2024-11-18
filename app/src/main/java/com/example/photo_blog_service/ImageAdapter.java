// ImageAdapter.java
package com.example.photo_blog_service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Post> postList;
    private Context context;

    public ImageAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    public void updateData(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }


    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.imageView.setImageBitmap(post.getImage());
        holder.favoriteButton.setImageResource(post.isFavorited() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        holder.favoriteButton.setOnClickListener(v -> {
            //mainActivity.toggleFavorite(post.getId());
            post.setFavorited(!post.isFavorited());  // 즐겨찾기 상태 반전
            notifyItemChanged(position);
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("text", post.getText());
            intent.putExtra("image", post.getImage()); // Assuming Post has a method to get Bitmap
            context.startActivity(intent);
        });

        // New download button click listener
        holder.downloadButton.setOnClickListener(v -> {
            downloadImageToGallery(post.getImage());
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        });
    }

    // Helper method to save image to gallery
    private void downloadImageToGallery(Bitmap bitmap) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap,
                "DownloadedImage",
                "Image downloaded from app"
        );

        // Optional: Check if the image was saved successfully
        if (savedImageURL == null) {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton favoriteButton;
        ImageButton editButton;
        ImageButton downloadButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            editButton = itemView.findViewById(R.id.editButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
