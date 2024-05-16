package com.example.eyeglassesapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.eyeglassesapp.entities.ImageEntity
import java.io.File
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


//ADAPTOR FOR VIEW PAGER

class ImagePagerAdapter(private val images: List<ImageEntity>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageEntity = images[position]
        holder.bindImage(imageEntity)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bindImage(imageEntity: ImageEntity) {
            val bitmap = getImageBitmapFromPath(imageEntity.imageUri)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.default_image_placeholder)
            }
        }
    }
}


internal fun getImageBitmapFromPath(imagePath: String): Bitmap? {
    val file = File(imagePath)
    if (file.exists()) {
        return BitmapFactory.decodeFile(file.absolutePath)
    }
    return null
}

