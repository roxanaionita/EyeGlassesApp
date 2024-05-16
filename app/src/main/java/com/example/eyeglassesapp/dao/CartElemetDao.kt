package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.eyeglassesapp.entities.CartElementEntity

@Dao
interface CartElementDao {
    @Insert
    suspend fun insertCartElement(cartElement: CartElementEntity): Long

    @Query("SELECT * FROM cart_elements WHERE user_id = :userId")
    fun getCartElementsByUserId(userId: Int): LiveData<List<CartElementEntity>>

    //folosita pentru a reincarca perechile adaugate de user in cos
    //daca acesta iese din aplicatie si intra iar
    @Query("SELECT * FROM cart_elements WHERE pair_id = :pairId AND user_id = :userId LIMIT 1")
    suspend fun getCartElementByPairAndUser(pairId: Long, userId: Int): CartElementEntity?

    @Update
    suspend fun update(cartElement: CartElementEntity)

    @Delete
    suspend fun delete(cartElement: CartElementEntity)

    @Query("DELETE FROM cart_elements")
    suspend fun deleteAllCartElements()
}