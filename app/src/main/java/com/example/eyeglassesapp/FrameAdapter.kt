package com.example.eyeglassesapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.databinding.FrameItemRecviewBinding
import com.example.eyeglassesapp.entities.FrameEntity

class FrameAdapter(private var frames: List<FrameWithImages>) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {

    inner class FrameViewHolder(val binding: FrameItemRecviewBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }
        fun bind(frameWithImages: FrameWithImages) {
            binding.frameWithImages = frameWithImages
            binding.executePendingBindings()

        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val frameWithImages = frames[position]
                val intent = Intent(v?.context, FramePageActivity::class.java)
                intent.putExtra("frameId", frameWithImages.frame.frameId)
                v?.context?.startActivity(intent)
            }

        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = FrameItemRecviewBinding.inflate(layoutInflater, parent, false)
            return FrameViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
            holder.bind(frames[position])
        }

        override fun getItemCount(): Int = frames.size

//        fun updateFrames(newFrames: List<FrameWithImages>) {
//            frames = newFrames
//            notifyDataSetChanged()
//        }

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
    fun deleteFrame(frameId: Int) {
        val position = frames.indexOfFirst { it.frame.frameId == frameId }
        if (position != -1) {
            frames = frames.toMutableList().apply { removeAt(position) }
            notifyItemRemoved(position)
        }
    }






}

