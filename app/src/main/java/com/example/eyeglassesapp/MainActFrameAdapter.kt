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
import com.bumptech.glide.Glide
import java.io.File

class MainActFrameAdapter(private var frames: List<FrameWithImages>) : RecyclerView.Adapter<MainActFrameAdapter.ViewHolder>(){
    private val MAX_ITEMS = 10
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameImage: ImageView = itemView.findViewById(R.id.frameFirstImage)
        val frameName: TextView = itemView.findViewById(R.id.frame_name)
        val framePrice: TextView = itemView.findViewById(R.id.frame_price)

        init {
            // Setează OnClickListener pentru elementul din RecyclerView
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Obține elementul din lista frames la poziția curentă
                    val selectedFrame = frames[position]
                    val intent = Intent(itemView.context, CreatePairActivity::class.java)
                    intent.putExtra("frameId", selectedFrame.frame.frameId)
                    itemView.context.startActivity(intent)

                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.mainact_frame_element, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(position < frames.size){
            val frame = frames[position]
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
        }else{
            holder.itemView.visibility = View.GONE
        }

    }
    //function to manage the local path of a picture
    private fun getImageBitmapFromPath(imagePath: String): Bitmap? {
        val file = File(imagePath)
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return null
    }

    fun updateFrames(newFrames: List<FrameWithImages>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = frames.size
            override fun getNewListSize(): Int = newFrames.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return frames[oldItemPosition].frame.frameId == newFrames[newItemPosition].frame.frameId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return frames[oldItemPosition] == newFrames[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        frames = newFrames
        diffResult.dispatchUpdatesTo(this)
    }
    override fun getItemCount(): Int {
        return Math.min(frames.size, MAX_ITEMS)
    }


}