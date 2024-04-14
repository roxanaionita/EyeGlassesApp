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
    suspend fun getPairById(pairId: Int): PairEntity?
}