package com.example.eyeglassesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = FaceShapeEntity::class,
            parentColumns = ["id"],
            childColumns = ["faceshape_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["faceshape_id"])]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val userId: Int = 0,
    @ColumnInfo(name = "firebase_uid") val firebaseUid: String,// pentru userii stocati in firebase
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "faceshape_id") val faceShapeId: Int?, // poate fi null
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "role") val role: String = "user",
    @ColumnInfo(name = "profile_picture_url") val profilePictureUrl: String? = null// URL-ul imaginii de profil, poate fi null inițial
)
