package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
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
    @Query("DELETE FROM lenses")
    suspend fun deleteAllLenses()

    // Optional: Retrieve all lenses from the database
    @Query("SELECT * FROM lenses")
    suspend fun getAllLenses(): List<LensEntity>

    // Optional: Retrieve a single lens by its ID
    @Query("SELECT * FROM lenses WHERE lens_id = :lensId")
    suspend fun getLensById(lensId: Int): LensEntity?


    //se gaseste pretul unei lentile in functie de caracteristicile ei
    @Query("SELECT price FROM lenses WHERE type = :lensType AND material = :material AND uv_filter = :isUVFilterSelected AND pc_filter = :isPCFilterSelected LIMIT 1")
    fun getPriceForLens(lensType: String, material: String, isUVFilterSelected: Boolean, isPCFilterSelected: Boolean): LiveData<Double?>


    //se gaseste lentila in functie de caracteristicile ei
    @Query("SELECT lens_id FROM lenses WHERE type = :lensType AND material = :material AND uv_filter = :uvFilter AND pc_filter = :pcFilter")
    fun getLensId(lensType: String, material: String, uvFilter: Boolean, pcFilter: Boolean): LiveData<Int?>

}