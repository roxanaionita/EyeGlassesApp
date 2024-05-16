package com.example.eyeglassesapp

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.entities.PairEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.example.eyeglassesapp.repositories.LensRepository
import com.google.firestore.v1.StructuredQuery.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


//FOLOSIT PENTRU A INCARCA PERECHILE DINTR-UN ORDER, CU DETALII
//pentru user
class OrderPairAdapter(
    private var pairs: List<PairEntity>,
    private val frameRepository: FrameRepository,
    private val lensRepository: LensRepository,
    private val imageRepository: ImageRepository,
) : RecyclerView.Adapter<OrderPairAdapter.PairViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ordered_pair_element_user, parent, false)
        return PairViewHolder(view)
    }

    override fun onBindViewHolder(holder: PairViewHolder, position: Int) {
        val pair = pairs[position]
        holder.bind(pair)
    }

    override fun getItemCount(): Int {
        return pairs.size
    }

    fun updatePairs(newPairs: List<PairEntity>) {
        pairs = newPairs
        notifyDataSetChanged() // Notify adapter of data change
    }

    inner class PairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val frameBrandTextView: TextView = itemView.findViewById(R.id.frame_brand_value)
        private val frameDescriptionTextView :TextView = itemView.findViewById(R.id.frame_description_value)
        private val lensMaterialTextView: TextView = itemView.findViewById(R.id.lens_material_value)
        private val lensTypeTextView: TextView = itemView.findViewById(R.id.lens_type_value)
        private val lensUVTextView: TextView = itemView.findViewById(R.id.lens_uv_value)
        private val lensPCTextView: TextView = itemView.findViewById(R.id.lens_pc_value)
        private val pairPriceTextView: TextView = itemView.findViewById(R.id.pair_price_value)
        private val pairQuantityTextView: TextView = itemView.findViewById(R.id.quantity_value)
        private val pairImageView: ImageView = itemView.findViewById(R.id.pair_frame_image)

        fun bind(pair: PairEntity) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val frame = frameRepository.getFrameById(pair.frameId)
                    val lens = lensRepository.getLensById(pair.lensId)

                    if (frame != null && lens != null) {
                        frameBrandTextView.text = frame.brand
                        frameDescriptionTextView.text = frame.description
                        lensMaterialTextView.text = lens.material
                        lensTypeTextView.text = lens.type
                        lensUVTextView.text = if (lens.uvFilter) "Yes" else "No"
                        lensPCTextView.text = if (lens.pcFilter) "Yes" else "No"
                        pairPriceTextView.text = "$${pair.price}"
                        pairQuantityTextView.text = pair.quantity.toString()

                        // Load image for the frame
                        val images = imageRepository.findImagesByFrameId(frame.frameId)
                        if (images.isNotEmpty()) {
                            val firstImage = images[0]
                            loadImageIntoImageView(firstImage.imageUri, pairImageView, itemView.context)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun loadImageIntoImageView(imageFilePath: String, imageView: ImageView, context: Context) {
            val file = File(imageFilePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.default_image_placeholder)
            }
        }
    }
}