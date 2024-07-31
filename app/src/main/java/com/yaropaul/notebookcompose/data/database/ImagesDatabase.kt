package com.yaropaul.notebookcompose.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yaropaul.notebookcompose.data.database.entity.ImageToDelete
import com.yaropaul.notebookcompose.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = true
)
abstract class ImagesDatabase: RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao
}