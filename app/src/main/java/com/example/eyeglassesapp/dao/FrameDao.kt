package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.entities.FrameEntity

@Dao
interface FrameDao {
    @Insert
    suspend fun insertFrame(frame: FrameEntity): Long

    @Update
    suspend fun updateFrame(frame: FrameEntity)

    @Delete
    suspend fun deleteFrame(frame: FrameEntity)

    @Query("SELECT * FROM frames WHERE frame_id = :frameId")
    suspend fun getFrameById(frameId: Int): FrameEntity

    @Query("SELECT * FROM frames")
    suspend fun getAllFrames(): List<FrameEntity>



    //transaction to get all frames with images
    //se foloseste transaction pentru ca e o cerere join pe doua tabele
    @Transaction
    @Query("SELECT * FROM frames")
    fun getAllFramesWithImages(): LiveData<List<FrameWithImages>>

    //transaction to get all frames with images and price less than a given price
    @Transaction
    @Query("SELECT * FROM frames WHERE price < :price ")
    fun getAllFramesWithImagesLessThanPrice(price: Double): List<FrameWithImages>

    @Transaction
    @Query("DELETE FROM frames WHERE frame_id = :frameId")
    suspend fun deleteFrameById(frameId: Int)

    //transaction to get all frames with images that are sunglasses or eyeglasses:
    @Transaction
    @Query("SELECT * FROM frames WHERE category = :category")
    fun getFramesByCategory(category: String) : List<FrameWithImages>

    //transaction yo get all frames with images that are of a specific gender and category
    @Transaction
    @Query("SELECT * FROM frames WHERE category = :category AND  gender = :gender")
    fun getFramesByGender(category: String, gender : String) : List<FrameWithImages>


    //frame with images for a specific frame id
    @Transaction
    @Query("SELECT * FROM frames WHERE frame_id = :frameId")
    fun getFrameWithImagesById(frameId: Int): LiveData<FrameWithImages>

    @Query("SELECT COUNT(*) FROM frames")
    suspend fun getTotalFrameCount() : Int


    //folosit pentru statisticile adminului
    @Query("SELECT COUNT(*) FROM frames WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int
}
