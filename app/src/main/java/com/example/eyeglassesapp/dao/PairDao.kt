package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eyeglassesapp.entities.PairEntity

@Dao
interface PairDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPair(pair: PairEntity): Long

    @Update
    suspend fun updatePair(pair: PairEntity)

    @Delete
    suspend fun deletePair(pair: PairEntity)

    @Query("SELECT * FROM pairs")
    suspend fun getAllPairs(): List<PairEntity>

    @Query("SELECT * FROM pairs WHERE pair_id = :pairId")
    suspend fun getPairById(pairId: Long): PairEntity?

    @Query("SELECT * FROM pairs WHERE frame_id = :frameId AND lens_id = :lensId AND right_diopter = :rightDiopter AND left_diopter = :leftDiopter LIMIT 1")
    suspend fun getPairByDetails(frameId: Int, lensId: Int, rightDiopter: Double, leftDiopter: Double): PairEntity?

    //after the order is made and order id is generated
    @Query("UPDATE pairs SET order_id = :orderId, quantity = :quantity WHERE pair_id = :pairId")
    suspend fun updatePairWithOrder(pairId: Long, orderId: Long, quantity: Int)

    //all pairs in an order
    @Query("SELECT * FROM pairs WHERE order_id = :orderId")
    suspend fun getPairsByOrderId(orderId: Int): List<PairEntity>

}