package com.example.eyeglassesapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.eyeglassesapp.dao.FaceShapeDao
import com.example.eyeglassesapp.dao.FrameDao
import com.example.eyeglassesapp.dao.FrameFaceShape_CrossDao
import com.example.eyeglassesapp.dao.ImageDao
import com.example.eyeglassesapp.dao.LensDao
import com.example.eyeglassesapp.dao.OrderDao
import com.example.eyeglassesapp.dao.PairDao
import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import com.example.eyeglassesapp.entities.ImageEntity
import com.example.eyeglassesapp.entities.LensEntity
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.entities.PairEntity
import com.example.eyeglassesapp.entities.UserEntity

@Database(
    entities = [
        FaceShapeEntity::class,
        FrameEntity::class,
        FrameFaceShape_CrossEntity::class,
        ImageEntity::class,
        LensEntity::class,
        OrderEntity::class,
        PairEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converter::class) // Include this if you have any type converters
abstract class AppDatabase : RoomDatabase() {
    // Define DAOs for each entity
    abstract fun faceShapeDao(): FaceShapeDao
    abstract fun frameDao(): FrameDao
    abstract fun frameFaceShape_CrossDao(): FrameFaceShape_CrossDao
    abstract fun imageDao(): ImageDao
    abstract fun lensDao(): LensDao
    abstract fun orderDao(): OrderDao
    abstract fun pairDao(): PairDao
    abstract fun userDao(): UserDao

    // Singleton pattern to prevent multiple instances of the database opening at the same time
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eyeglasses_database"
                ).fallbackToDestructiveMigration() // Handle migrations as necessary
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}