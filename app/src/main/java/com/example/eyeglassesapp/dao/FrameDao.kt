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


//    The @Transaction annotation is used because
//    this is a multi-table query, and Room requires
//    this annotation for queries that span multiple tables for consistency.
    @Transaction
    @Query("SELECT * FROM frames")
    fun getAllFramesWithImages(): LiveData<List<FrameWithImages>>

    @Transaction
    @Query("DELETE FROM frames WHERE frame_id = :frameId")
    suspend fun deleteFrameById(frameId: Int)
}
