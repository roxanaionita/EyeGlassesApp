package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eyeglassesapp.entities.ImageEntity

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity)

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE frame_id = :frameId")
    suspend fun findImagesByFrameId(frameId: Int): List<ImageEntity>

    @Query("DELETE FROM images WHERE image_id = :imageId")
    suspend fun deleteImageById(imageId: Int)

    // Deletes all images associated with a specific frame
    @Query("DELETE FROM images WHERE frame_id = :frameId")
    suspend fun deleteImagesByFrameId(frameId: Int)

    @Query("SELECT * FROM images")
    fun getAllImages() : LiveData<List<ImageEntity>>

}
