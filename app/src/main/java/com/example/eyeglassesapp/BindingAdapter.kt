package com.example.eyeglassesapp

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (imageUrl != null && imageUrl.isNotEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .into(view)
    } else {
        // Optional: Set a default image if no URL is provided
        view.setImageResource(R.drawable.default_image_placeholder)
    }
}
