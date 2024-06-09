package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity

@Dao
interface FrameFaceShape_CrossDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossEntity: FrameFaceShape_CrossEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossEntities: List<FrameFaceShape_CrossEntity>)

    @Query("SELECT * FROM frame_face_shape_cross WHERE frame_id = :frameId")
    suspend fun getFaceShapesForFrame(frameId: Int): List<FrameFaceShape_CrossEntity>

    @Query("SELECT * FROM frame_face_shape_cross WHERE face_shape_id = :faceShapeId")
    suspend fun getFramesForFaceShape(faceShapeId: Int): List<FrameFaceShape_CrossEntity>

    @Query("""
        SELECT * FROM frames 
        WHERE frame_id IN (SELECT frame_id FROM frame_face_shape_cross WHERE face_shape_id = :faceShapeId)
        AND category = :category
    """)
    suspend fun getFramesWithImagesByFaceShapeIdAndCategory(faceShapeId: Int, category: String): List<FrameWithImages>

    @Query("""
        SELECT * FROM frames
        INNER JOIN frame_face_shape_cross ON frames.frame_id = frame_face_shape_cross.frame_id
        WHERE frame_face_shape_cross.face_shape_id = (SELECT face_shape_id FROM face_shapes WHERE face_shape = :faceShape)
    """)
    fun getFramesForFaceShape(faceShape: String): LiveData<List<FrameWithImages>>


}