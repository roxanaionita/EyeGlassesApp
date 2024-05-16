package com.example.eyeglassesapp

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyeglassesapp.ViewModels.CartElementViewModel
import com.example.eyeglassesapp.entities.CartElementEntity
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.FrameRepository
import com.example.eyeglassesapp.repositories.ImageRepository
import com.example.eyeglassesapp.repositories.PairRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CartElementAdapter(
    private var cartElements: List<CartElementEntity>,
    private val pairRepository: PairRepository,
    private val frameRepository: FrameRepository,
    private val imageRepository: ImageRepository,
    private val cartElementViewModel: CartElementViewModel
) : RecyclerView.Adapter<CartElementAdapter.CartElementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_element, parent, false)
        return CartElementViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartElementViewHolder, position: Int) {
        val cartElement = cartElements[position]
        holder.bind(cartElement)
    }

    override fun getItemCount(): Int {
        return cartElements.size
    }
//    fun updateCartElements(newCartElements: List<CartElementEntity>) {
//        val diffCallback = CartElementDiffCallback(cartElements, newCartElements)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        cartElements = newCartElements
//        diffResult.dispatchUpdatesTo(this)
//    }
    fun updateCartElements(newCartElements: List<CartElementEntity>) {
        cartElements = newCartElements
        notifyDataSetChanged() // Notify adapter of data change
    }
    fun getCartElements(): List<CartElementEntity> {
        return cartElements
    }

    inner class CartElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val frameBrandTextView: TextView = itemView.findViewById(R.id.title_text)
        private val cartElementCostTextView: TextView = itemView.findViewById(R.id.price)
        private val totalCostTextView: TextView = itemView.findViewById(R.id.total_price)
        private val frameImageView: ImageView = itemView.findViewById(R.id.image)
        private val quantityTextView : TextView = itemView.findViewById(R.id.quantity)
        private val plusTextView: TextView = itemView.findViewById(R.id.plusItem)
        private val minusTextView: TextView = itemView.findViewById(R.id.minusItem)

        private lateinit var currentCartElement: CartElementEntity

        init {
            plusTextView.setOnClickListener {
                // Increment quantity and update UI
                updateQuantity(currentCartElement.quantity + 1)
            }

            minusTextView.setOnClickListener {
                // Decrement quantity if > 1, otherwise remove cart element
                if (currentCartElement.quantity > 1) {
                    updateQuantity(currentCartElement.quantity - 1)
                } else {
                    // Perform delete action when quantity is 1
                    deleteCartElement(currentCartElement)
                }
            }
        }

        private fun updateQuantity(newQuantity: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Update cart element quantity in database
                    currentCartElement.quantity = newQuantity
                    cartElementViewModel.updateCartElement(currentCartElement)

                    // Update UI with new quantity and total cost
                    quantityTextView.text = newQuantity.toString()
                    val totalCost = newQuantity * currentCartElement.price
                    totalCostTextView.text = "$${totalCost}"

                } catch (e: Exception) {
                    Log.e("CartElementUpdate", "Error updating cart element", e)
                }
            }
        }

        private fun deleteCartElement(cartElement: CartElementEntity) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Delete cart element from database
                    cartElementViewModel.deleteCartElement(cartElement)
                     //cartElements.remove(cartElement)
                     //notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.e("CartElementDelete", "Error deleting cart element", e)
                }
            }
        }


        fun bind(cartElement: CartElementEntity) {
            currentCartElement = cartElement
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val pair = pairRepository.getPairById(cartElement.pairId)
                    if (pair != null) {
                        val frame = frameRepository.getFrameById(pair.frameId)
                        if (frame != null) {
                            frameBrandTextView.text = "${frame.brand}"
                            val totalCost = cartElement.quantity * cartElement.price
                            cartElementCostTextView.text = "$${cartElement.price}"
                            totalCostTextView.text = "$${totalCost}"
                            quantityTextView.text = "${cartElement.quantity}"


                            // Get first image and display
                            val images = imageRepository.findImagesByFrameId(frame.frameId)
                            if (images.isNotEmpty()) {
                                val firstImage = images[0]
                                loadImageIntoImageView(firstImage.imageUri, frameImageView, itemView.context)
                            }


                        }
                    }
                } catch (e: Exception) {
                    frameImageView.setImageResource(R.drawable.default_image_placeholder)
                }
            }
        }

        fun loadImageIntoImageView(imageFilePath: String, imageView: ImageView, context: Context) {
            val file = File(imageFilePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.default_image_placeholder)
            }
        }


    }
    private class CartElementDiffCallback(
        private val oldList: List<CartElementEntity>,
        private val newList: List<CartElementEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

