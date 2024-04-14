package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "images",
    foreignKeys = [
        ForeignKey(
            entity = FrameEntity::class,
            parentColumns = ["frame_id"],
            childColumns = ["frame_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["frame_id"])]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "image_id") val image_id: Int = 0,
    @ColumnInfo(name = "frame_id") val frameId: Int,
    @ColumnInfo(name = "image_uri") val imageUri: String //path
)
