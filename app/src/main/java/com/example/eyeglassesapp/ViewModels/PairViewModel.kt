package com.example.eyeglassesapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.entities.CartElementEntity
import com.example.eyeglassesapp.entities.PairEntity
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.PairRepository
import kotlinx.coroutines.launch


class PairViewModel(
    private val pairRepository: PairRepository,
    private val cartElementRepository: CartElementRepository // Injectați repository-ul CartElement
) : ViewModel() {

    private val _insertedPairId = MutableLiveData<Long?>()
    val insertedPairId: LiveData<Long?> = _insertedPairId

    // Insert Pair and Cart Element for User ACtivity
    fun insertPairAndCartElement(pair: PairEntity, userId: Int) {
        viewModelScope.launch {
            try {
                // Check if the pair already exists in the database based on its characteristics
                val existingPair = pairRepository.getPairByDetails(pair.frameId, pair.lensId, pair.rightDiopter, pair.leftDiopter)

                if (existingPair != null) {
                    // Pair already exists, check if a cart element exists for this pair and user
                    val existingCartElement = cartElementRepository.getCartElementByPairAndUser(existingPair.pairId, userId)

                    if (existingCartElement != null) {
                        // Cart element already exists, update its quantity
                        val updatedQuantity = existingCartElement.quantity + 1
                        existingCartElement.quantity = updatedQuantity

                        viewModelScope.launch {
                            try {
                                cartElementRepository.updateCartElement(existingCartElement)
                                Log.d("CartElementUpdate", "Cart Element updated successfully")
                            } catch (e: Exception) {
                                Log.e("CartElementUpdate", "Error updating cart element", e)
                            }
                        }
                    } else {
                        // Cart element does not exist, create a new one
                        val cartElement = CartElementEntity(userId = userId, pairId = existingPair.pairId, quantity = 1, price = pair.price)
                        cartElementRepository.insertCartElement(cartElement)
                    }

                    _insertedPairId.value = existingPair.pairId
                } else {
                    // Pair does not exist, create a new pair and cart element
                    val insertedPairId = pairRepository.insertPair(pair)
                    _insertedPairId.value = insertedPairId

                    val cartElement = CartElementEntity(userId = userId, pairId = insertedPairId, quantity = 1, price = pair.price)
                    cartElementRepository.insertCartElement(cartElement)
                }

                Log.d("PairInsertion", "Inserted Pair ID: ${_insertedPairId.value}")
            } catch (e: Exception) {
                _insertedPairId.value = null
                Log.e("PairInsertion", "Error inserting pair", e)
            }
        }
    }


    //get pairs by order id
    suspend fun getPairsByOrderId(orderId: Int): List<PairEntity> {
        return pairRepository.getPairsByOrderId(orderId)
    }
}
