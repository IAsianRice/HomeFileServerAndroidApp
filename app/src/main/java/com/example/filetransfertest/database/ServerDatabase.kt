package com.example.filetransfertest.database
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ServerInformation::class], version = 1)
abstract class ServerDatabase : RoomDatabase() {
    abstract fun serverInformationDao(): ServerInformationDao
}