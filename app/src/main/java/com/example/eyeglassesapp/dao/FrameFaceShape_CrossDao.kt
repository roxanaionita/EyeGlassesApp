package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity

@Dao
interface FrameFaceShape_CrossDao {
    @Insert
    suspend fun insertFrameFaceShapeCrossRef(crossRef: FrameFaceShape_CrossEntity)

    @Query("SELECT frames.* FROM frames INNER JOIN frame_face_shape_cross ON frames.frame_id = frame_face_shape_cross.frame_id WHERE frame_face_shape_cross.face_shape_id = :faceShapeId")
    suspend fun getFramesForFaceShape(faceShapeId: Int): List<FrameEntity>

    @Query("SELECT face_shapes.* FROM face_shapes INNER JOIN frame_face_shape_cross ON face_shapes.id = frame_face_shape_cross.face_shape_id WHERE frame_face_shape_cross.frame_id = :frameId")
    suspend fun getFaceShapesForFrame(frameId: Int): List<FaceShapeEntity>

    // This could be useful for removing an association between a frame and a face shape
    @Query("DELETE FROM frame_face_shape_cross WHERE frame_id = :frameId AND face_shape_id = :faceShapeId")
    suspend fun deleteAssociation(frameId: Int, faceShapeId: Int)
}