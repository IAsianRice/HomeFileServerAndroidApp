package com.example.filetransfertest
import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


data class ServerData (val name: String, val ip: String, val port: String)

interface Database {
    fun getWritableDatabase(): SQLiteDatabase?
    fun getServerDataList(): ArrayList<ServerData>
}

class ServerDatabase(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), Database {

    companion object {
        private const val DATABASE_NAME = "ServerDatabase"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables and define their structure here
        //db.execSQL("DROP TABLE IF EXISTS ServerTable")
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS ServerTable (
            name TEXT PRIMARY KEY,
            ip TEXT,
            port INTEGER
            )""".trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        // Your custom implementation here
        return super.getWritableDatabase()
    }

    @SuppressLint("Range")
    override fun getServerDataList(): ArrayList<ServerData> {
        // Initialize the data in the constructor
        val serverDatabase = ServerDatabase(context)
        val db = serverDatabase.readableDatabase
        val temp: ArrayList<ServerData> = ArrayList<ServerData>()
        val cursor = db.query("ServerTable", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val ip  = cursor.getString(cursor.getColumnIndex("ip"))
            val port = cursor.getString(cursor.getColumnIndex("port"))

            val myData = ServerData(name, ip, port)
            temp.add(myData)
        }
        cursor.close()
        db.close()
        return temp
    }
}

class MockDatabase : Database {
    override fun getWritableDatabase(): SQLiteDatabase? {
        // Your custom implementation here
        return null
    }

    override fun getServerDataList(): ArrayList<ServerData> {
        // Provide mock data for testing
        return arrayListOf(
            ServerData("MockServer1", "192.168.0.1", "8080"),
            ServerData("MockServer2", "192.168.0.2", "9090")
            // Add more mock data as needed
        )
    }
}