package com.example.eyeglassesapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class GenderAdapter(private var framesByGender : List<FrameWithImages>) : RecyclerView.Adapter<GenderAdapter.GenderViewHolder>() {
    inner class GenderViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val frameImage : ImageView = itemView.findViewById(R.id.frameFirstImage)
        val frameName: TextView = itemView.findViewById(R.id.frame_name)
        val framePrice: TextView = itemView.findViewById(R.id.frame_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenderViewHolder {
       val inflater = LayoutInflater.from(parent.context)
       val view = inflater.inflate(R.layout.mainact_frame_element, parent, false)
       return GenderViewHolder(view)
    }
    fun updateFrames(newFrames: List<FrameWithImages>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = framesByGender.size
            override fun getNewListSize(): Int = newFrames.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return framesByGender[oldItemPosition].frame.frameId == newFrames[newItemPosition].frame.frameId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return framesByGender[oldItemPosition] == newFrames[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        framesByGender = newFrames
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return framesByGender.size
    }

    override fun onBindViewHolder(holder: GenderAdapter.GenderViewHolder, position: Int) {
        val frame = framesByGender[position]
        // Bind data to the views in the ViewHolder
        holder.frameName.text = frame.frame.brand
        holder.framePrice.text = String.format("$ %.2f", frame.frame.price)
        // Load image from local path
        val imagePath = frame.images[0].imageUri  // Assuming imageUri holds the path
        val imageResource = getImageBitmapFromPath(imagePath)
        if (imageResource != null) {
            holder.frameImage.setImageBitmap(imageResource)
        } else {
            holder.frameImage.setImageResource(R.drawable.default_image_placeholder)
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