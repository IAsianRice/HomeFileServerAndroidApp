package com.example.filetransfertest.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.filetransfertest.SecureCommunication
import com.example.filetransfertest.ServerConnection
import com.example.filetransfertest.database.ServerInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher
import kotlin.coroutines.CoroutineContext


class SocketServerService : Service(), CoroutineScope {

    val SERVER_BROADCAST = "MCSASR"
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    // Binder
    private val binder: IBinder = SocketServiceBinder()

    // private key cipher
    private val rsaPrivateKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private lateinit var rsaPublicKey: RSAPublicKey
    private val _searchServerStateFlow = MutableStateFlow<MutableList<DiscoveredServerData>>(arrayListOf())
    var searchServerStateFlow = _searchServerStateFlow.asStateFlow()
    private val _connectionStateFlow = MutableStateFlow<MutableMap<Long,ServerConnection>>(mapOf<Long,ServerConnection>().toMutableMap())
    var connectionStateFlow = _connectionStateFlow.asStateFlow()

    var searchingForServers = false
    //val allConnections: MutableMap<String,ServerConnection> = mapOf<String,ServerConnection>().toMutableMap()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    // Start Command
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Service", "onStartCommand!")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val keyPair = keyPairGenerator.generateKeyPair()

        rsaPrivateKeyCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        rsaPublicKey = keyPair.public as RSAPublicKey
        Log.d("Service", "onCreate!")
    }

    override fun onDestroy() {
        super.onDestroy()
        for (conn in connectionStateFlow.value)  {
            conn.value.cancel()
        }
        Log.d("Service", "onDestroy!")
    }
    fun extractValues(input: String): List<String> {
        val regex = "\\[(.*?)\\]".toRegex()
        return regex.findAll(input).map { it.groupValues[1] }.toList()
    }
    fun searchForServers() {
        launch {
            Log.d("Service", "Searching for servers...")
            if (!searchingForServers) {
                searchingForServers = true
                val clientSocket = DatagramSocket(7001) // UDP socket for broadcasting
                clientSocket.broadcast = true
                while (searchingForServers) {
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)

                    Log.d("Service", "Waiting For Broadcast")
                    clientSocket.receive(packet)
                    //val serverAddress = packet.address
                    val serverPort = String(packet.data, 0, packet.length)
                    val serverData = extractValues(serverPort)
                    var discoveredServerData = DiscoveredServerData(serverData[0], serverData[1], serverData[2])

                    if (!searchServerStateFlow.value.contains(discoveredServerData)) {
                        // Update the mutable list in an immutable way
                        val currentList = ArrayList(_searchServerStateFlow.value)
                        currentList.add(discoveredServerData)

                        // Update the StateFlow with the new list
                        _searchServerStateFlow.value = currentList
                    }
                }
            }
        }
    }
    fun stopSearchForServers() {
        searchingForServers = false
    }

    // Socket Service Binder
    inner class SocketServiceBinder : Binder() {
        fun getService(): SocketServerService {
            return this@SocketServerService
        }
        fun addServerConnection(serverInformation: ServerInformation) {

            val conn = ServerConnection(
                SecureCommunication(rsaPublicKey, rsaPrivateKeyCipher),
                serverInformation)

            // Update the mutable list in an immutable way
            val currentList = _connectionStateFlow.value.toMutableMap()
            currentList[conn.serverInformation.id] = conn
            _connectionStateFlow.value = currentList
        }
        fun getAllConnectionsStateFlow(): MutableStateFlow<MutableMap<Long, ServerConnection>> {
            return _connectionStateFlow
        }
        fun getServerDiscoveryStateFlow(): MutableStateFlow<MutableList<DiscoveredServerData>> {
            return _searchServerStateFlow
        }
        fun startSearchForServers() {
            searchForServers()
        }
        fun stopSearchForServers() {
            stopSearchForServers()
        }
        fun addServerConnections(serverDataList: ArrayList<ServerInformation>) {
            for (serverData in serverDataList)
            {
                addServerConnection(serverData)
            }
        }
    }
    data class DiscoveredServerData(val localIP: String, val publicIP: String, val port: String)
}
