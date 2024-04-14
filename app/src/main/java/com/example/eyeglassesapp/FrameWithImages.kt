package com.example.eyeglassesapp

import androidx.room.Embedded
import androidx.room.Relation
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.ImageEntity

//This data class uses the @Embedded annotation to include the FrameEntity and the @Relation annotation
//to specify the relationship to fetch ImageEntity objects associated with each frame.

data class FrameWithImages(
    @Embedded val frame: FrameEntity,
    @Relation(
        parentColumn = "frame_id",
        entityColumn = "frame_id"
    )
    val images: List<ImageEntity>
)