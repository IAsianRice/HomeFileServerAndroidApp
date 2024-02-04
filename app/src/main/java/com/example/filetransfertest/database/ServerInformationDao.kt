package com.example.filetransfertest.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ServerInformationDao {

    @Query("SELECT * FROM server_information")
    suspend fun getAllServerInformation(): List<ServerInformation>

    @Query("DELETE FROM server_information WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert
    suspend fun insert(serverInformation: ServerInformation)

    @Delete
    suspend fun delete(serverInformation: ServerInformation)
}