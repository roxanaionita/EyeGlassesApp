package com.example.eyeglassesapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.entities.ImageEntity
import java.io.File

class OtherImagesAdapter(private val images: List<ImageEntity>) : RecyclerView.Adapter<OtherImagesAdapter.OtherImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_other_image, parent, false)
        return OtherImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OtherImageViewHolder, position: Int) {
        val imageEntity = images[position]
        holder.bindImage(imageEntity)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class OtherImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.otherImageView)

        fun bindImage(imageEntity: ImageEntity) {
            val bitmap = getImageBitmapFromPath(imageEntity.imageUri)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.default_image_placeholder)
            }
        }
    }
    private fun getImageBitmapFromPath(imagePath: String): Bitmap? {
        val file = File(imagePath)
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return null
    }

}