package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "frame_face_shape_cross",
    primaryKeys = ["frame_id", "face_shape_id"],
    foreignKeys = [
        ForeignKey(
            entity = FrameEntity::class,
            //parent table - Frames table
            parentColumns = ["frame_id"],
            // child table - Cross table
            childColumns = ["frame_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FaceShapeEntity::class,
            // parent table - faceShapes table
            parentColumns = ["id"],
            // child table - cross table
            childColumns = ["face_shape_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["face_shape_id"])]
)
data class FrameFaceShape_CrossEntity(
    @ColumnInfo(name = "frame_id") val frame_id: Int,
    @ColumnInfo(name = "face_shape_id") val face_shape_id: Int
)
