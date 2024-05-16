package com.example.eyeglassesapp.repositories

import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.dao.CartElementDao
import com.example.eyeglassesapp.entities.CartElementEntity

class CartElementRepository(private val cartElementDao: CartElementDao) {

    suspend fun insertCartElement(cartElement: CartElementEntity): Long {
        return cartElementDao.insertCartElement(cartElement)
    }

    //get cart elements by user id
    fun getCartElementsByUserId(userId: Int): LiveData<List<CartElementEntity>> {
        return cartElementDao.getCartElementsByUserId(userId)
    }
    suspend fun getCartElementByPairAndUser(pairId: Long, userId: Int): CartElementEntity? {
        return cartElementDao.getCartElementByPairAndUser(pairId, userId)
    }

    suspend fun updateCartElement(cartElement: CartElementEntity) {
        cartElementDao.update(cartElement)
    }
    suspend fun deleteCartElement(cartElement: CartElementEntity) {
        cartElementDao.delete(cartElement)
    }
    suspend fun deleteAllCartElements() {
        cartElementDao.deleteAllCartElements()
    }
}