package com.example.filetransfertest.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_information")
data class ServerInformation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val local_ip: String,
    val internet_ip: String,
    val port: String,
)