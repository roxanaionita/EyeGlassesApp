package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pairs",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["order_id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FrameEntity::class,
            parentColumns = ["frame_id"],
            childColumns = ["frame_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LensEntity::class,
            parentColumns = ["lens_id"],
            childColumns = ["lens_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["frame_id"]),
        Index(value = ["lens_id"])
    ]
)
data class PairEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pair_id") val pairId: Int = 0,
    @ColumnInfo(name = "order_id") val orderId: Int,
    @ColumnInfo(name = "frame_id") val frameId: Int,
    @ColumnInfo(name = "lens_id") val lensId: Int,
    @ColumnInfo(name = "right_diopter") val rightDiopter: Double,
    @ColumnInfo(name = "left_diopter") val leftDiopter: Double,
    @ColumnInfo(name = "price") val price: Double
)