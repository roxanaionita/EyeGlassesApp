package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eyeglassesapp.entities.LensEntity

@Dao
interface LensDao {
    // Insert a new lens into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLens(lens: LensEntity): Long

    // Delete a lens from the database
    @Delete
    suspend fun deleteLens(lens: LensEntity)

    // Optional: Retrieve all lenses from the database
    @Query("SELECT * FROM lenses")
    suspend fun getAllLenses(): List<LensEntity>

    // Optional: Retrieve a single lens by its ID
    @Query("SELECT * FROM lenses WHERE lens_id = :lensId")
    suspend fun getLensById(lensId: Int): LensEntity?
}