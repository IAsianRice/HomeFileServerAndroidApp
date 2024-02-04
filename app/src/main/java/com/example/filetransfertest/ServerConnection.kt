package com.example.filetransfertest

import android.util.Log
import com.example.filetransfertest.database.ServerInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext

@Serializable
@Polymorphic
sealed class FileSystemItem {
}
@Serializable
@SerialName("Directory")
data class Directory(@SerialName("name") val name: String, @SerialName("type") val type: String, @SerialName("content") val content: List<FileSystemItem>): FileSystemItem()
@Serializable
@SerialName("File")
data class File(@SerialName("name") val name: String, @SerialName("type") val type: String, @SerialName("extension") val extension: String, @SerialName("size") val size: Int): FileSystemItem()


// Server States
sealed class ServerState {
    object Online : ServerState()
    object Offline : ServerState()
    object Unresponsive : ServerState()
    object Error : ServerState()
    object Connected : ServerState()
}

data class UserSession(
    val baseDirectoryStateFlow: StateFlow<Directory>,
    val _baseDirectoryStateFlow: MutableStateFlow<Directory>,
    val isAuthorized: Boolean,
    val authToken: String
)

class ServerConnection (
                        var secureCommunication: SecureCommunication,
                        var serverInformation: ServerInformation) : CoroutineScope {
    private lateinit var socket: Socket
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var connectionAttempts = 5
    var checkLocal = false
    var active = false
    var userSession: UserSession? = null
    val transmissionStreams: MutableMap<Long, DataReceiver> = mutableMapOf()
    private lateinit var socketBuffer: SocketBuffer
    val availableNumbers: MutableSet<Long> = (1L..99L).toMutableSet()
    val _stateFlow: MutableStateFlow<ServerState> = MutableStateFlow<ServerState>(ServerState.Unresponsive)
    val stateFlow: StateFlow<ServerState> = _stateFlow.asStateFlow()
    val _messageFlow: MutableStateFlow<String> = MutableStateFlow<String>("")
    val messageFlow: StateFlow<String> = _messageFlow.asStateFlow()
    /*
    var clientPrivateKey: RSAPrivateKey? = null
    val clientPrivateKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
     */


    /**
     * Establishes and manages the connection to the server.
     * Responsible for handling connection states, encryption setup, and continuous communication with the server.
     */
    // Server Connection
    fun connectToServer() {
        // Check if the connection is not already active
        if (!active) {
            active = true
            // Launch a coroutine for managing the server connection
            launch {
                // Continue managing the connection while active
                while (active)
                    if (shouldConnect()) {
                        if (checkLocal) {
                            checkLocal = false;
                            handshakeLocalServer()
                        }
                        else
                        {
                            checkLocal = true;
                            //handshakeGlobalServer()
                        }
                        // Delay before attempting to reconnect
                        delay(5000L)
                    } else {
                        Log.d("Server", "Listening to Server")
                        parseHeader(readHeader())
                    }
                }
            }
        }

    /**
     * Checks if the server needs to be connected based on the current state.
     */
    // Server Connection
    private fun shouldConnect(): Boolean {
        return stateFlow.value != ServerState.Online && stateFlow.value != ServerState.Connected && connectionAttempts > 0
    }

    /**
     * Connects to the server, handles initial setup, and reads the server's response.
     */
    // Server Connection
    private fun handshakeLocalServer() {
        try {
            // Connect to the Server
            //socket.connect(InetSocketAddress(local_ip, port), 5000)
            socket = Socket(serverInformation.local_ip, serverInformation.port.toInt())
            socketBuffer = SocketBuffer(socket)

            //Get the Server Public Key
            Log.d("Server", "Receiving Public Key")
            //var data = readUTF8FromBufferUntilDelimiter(reader, 0x04)
            secureCommunication.setServerPublicKey(socketBuffer.recv(delimiter = 0x04).toString(Charsets.UTF_8))

            // Send our Pub Key
            Log.d("Server", "Sending Our Public Key")
            sendAsymmetricEncryptedFormattedMessage(secureCommunication.getClientPublicKeyPEM().toByteArray(Charsets.UTF_8), 0)

            // Read Symmetric KEY!
            Log.d("Service ${serverInformation.title}", "Receiving Symmetric Key")
            val headerData = readHeader()
            parseSymmetricKey(headerData)

            // Update the server state to Online]
            _stateFlow.value = ServerState.Online

        } catch (e: Exception) {
            handleConnectionException(e)
            Log.d("Service ${serverInformation.title}", "Connection exception: ${e.message}")
        }
    }
/*
    private fun handshakeGlobalServer() {
        try {
            // Connect to the Server
            socket.connect(InetSocketAddress(global_ip, port), 5000)
            reader = BufferedInputStream(socket.getInputStream())

            //Get the Server Public Key
            Log.d("Server", "Receiving Public Key")
            var data = readUTF8FromBufferUntilDelimiter(reader, 0x04)
            secureCommunication.setServerPublicKey(data)

            // Send our Pub Key
            Log.d("Server", "Sending Our Public Key")
            Log.d("Service $name", secureCommunication.clientPublicKey.encoded.toString(Charsets.UTF_8))
            sendAsymmetricEncryptedFormattedMessage(convertToPem(secureCommunication.clientPublicKey).toByteArray(Charsets.UTF_8), 0)

            // Read Symmetric KEY!
            Log.d("Service $name", "Receiving Symmetric Key")
            val headerData = readHeader()
            Log.d("Service $name", headerData.decodeToString())
            Log.d("Service $name", "Receiving Symmetric Key 2")
            parseSymmetricKey(headerData)

            // Update the server state to Online]
            _stateFlow.value = ServerState.Online

        } catch (e: Exception) {
            handleConnectionException(e)
            Log.d("Service $name", "Connection exception: ${e.message}")
        }
    }*/


    /**
     * Reads continuously from the connected server.
     */
    // API HANDLER
    @OptIn(ExperimentalSerializationApi::class)
    private fun apiRequestHandler(request: Long, dataReceiver: DataReceiver) {
        if(request == 1L) // Dirdata
        {
            dataReceiver.startReceiverFunction { dr ->
                if (userSession != null)
                {
                    userSession!!._baseDirectoryStateFlow.value = Json.decodeFromString<Directory>(dr.getDataUntilTerminated().toString(Charsets.UTF_8))
                }
            }
        }
        else if(request == 2L)  // authorized
        {
            Log.d("Service ${serverInformation.title}", "Authorized!!!!")
            dataReceiver.startReceiverFunction { dr ->
                Log.d("Service ${serverInformation.title}", "readFromConnectedServer: Starting Request!!!!")
                if (userSession != null)
                {
                    val ss = MutableStateFlow<Directory>(Directory("None", "Directory", listOf()))
                    userSession = UserSession(ss.asStateFlow(), ss , true,"")
                    sendAsymmetricEncryptedFormattedMessage(api_send_command("getdir"), 4L)
                }
                Log.d("Service ${serverInformation.title}", "readFromConnectedServer: Finished Request!!!!")
            }
        }
        else if(request == 3L)  // unauthorized
        {
            /*dataReceiver.setReceiverFunction { contentData ->
                _messageFlow.value = "${messageFlow.value}\nIncorrect Credentials"
            }*/
        }
        else
        {
            /*dataReceiver.setReceiverFunction { contentData ->
                _messageFlow.value = "${messageFlow.value}\n${contentData.toString(Charsets.UTF_8)}"
            }*/
        }
    }


    // Server Connection
    /**
     * Handles exceptions during the connection process.
     */
    private fun handleConnectionException(exception: Exception) {
        connectionAttempts -= 1
        when (exception) {
            is UnknownHostException -> {
                //Log.d("Service ${name}", "Unknown host: ${exception.message}")
                _stateFlow.value = ServerState.Error
            }
            is ConnectException -> {
                //Log.d("Service ${name}", "Connection exception: ${exception.message}")
                _stateFlow.value = ServerState.Offline
            }
            is NoRouteToHostException -> {
                //Log.d("Service ${name}", "No route to host: ${exception.message}")
            }
            is SocketException -> {
                //Log.d("Service ${name}", "Socket exception: ${exception.message}")
                _stateFlow.value = ServerState.Offline
            }
            is IOException -> {
                //Log.d("Service ${name}", "IOException: ${exception.message}")
            }
            is IllegalArgumentException -> {
                //Log.d("Service ${name}", "IllegalArgumentException: ${exception.message}")
            }
        }
    }
    // Server Connection
    private fun readHeader(): ByteArray {
        return secureCommunication.decryptAsymmetricData(socketBuffer.recv(secureCommunication.getClientPublicKeyByteLength()))
    }


    // Server Connection
    private fun parseHeader(header: ByteArray) {
        try {
            val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

            val messageFlags = buffer.get().toInt() and 0xFF
            val transmissionId = buffer.long
            val bodySize = buffer.long
            val encryptionMethod = buffer.get().toInt() and 0xFF

            Log.d("Service ${serverInformation.title}","(parseHeader) transmissionId: $transmissionId")
            Log.d("Service ${serverInformation.title}","(parseHeader) bodySize: $bodySize")
            Log.d("Service ${serverInformation.title}","(parseHeader) messageFlags: ${Integer.toBinaryString(messageFlags).padStart(8, '0')}")
            Log.d("Service ${serverInformation.title}","(parseHeader) encryptionMethod: $encryptionMethod")

            val dataReceiver: DataReceiver
            if (messageFlags and 0b00000001 != 0) {  // Intermittent Message
                Log.d("Service ${serverInformation.title}","(parseHeader) Intermittent Message")
                val terminated = buffer.getLong()
                dataReceiver = transmissionStreams[transmissionId] ?: return
                if (terminated != 0L) {
                    dataReceiver.terminate()
                }
            } else {  // Request Message
                Log.d("Service ${serverInformation.title}","(parseHeader) Request Message")
                dataReceiver = DataReceiver()
                val request = buffer.long
                Log.d("Service ${serverInformation.title}","(parseHeader) requestID: $request")
                apiRequestHandler(request, dataReceiver)
            }

            when (encryptionMethod) {
                0x01 -> {  // Asymmetric Encryption
                    Log.d("Service ${serverInformation.title}","(parseHeader) Asymmetric Encryption")
                    val dataIn = getAsymmetricData(bodySize.toInt())
                    Log.d("Service ${serverInformation.title}","(parseHeader): ${dataIn.joinToString("") { byte -> byte.toInt().and(0xFF).toString(16).padStart(2, '0') }}")
                    dataReceiver.pushData(dataIn)
                }
                0x02 -> {  // Symmetric Encryption
                    Log.d("Service ${serverInformation.title}","(parseHeader) Symmetric Encryption")
                    val dataIn = getSymmetricData(bodySize.toInt())
                    Log.d("Service ${serverInformation.title}","(parseHeader): ${dataIn.joinToString("") { byte -> byte.toInt().and(0xFF).toString(16).padStart(2, '0') }}")
                    dataReceiver.pushData(dataIn)
                }
            }

            transmissionStreams[transmissionId] = dataReceiver

            if (messageFlags and 0b00000010 == 0) {  // Not Streamed
                Log.d("Service ${serverInformation.title}","(parseHeader) Not Streamed")
                dataReceiver.terminate()
            }

        } catch (e: Exception) {
            Log.d("Service ${serverInformation.title}","Unexpected (parseHeader) error: $e")
        }
    }

    // Cryptography
    private fun getAsymmetricData(bodySize: Int): ByteArray {
        return (secureCommunication.decryptAsymmetricData(socketBuffer.recv(secureCommunication.getClientPublicKeyByteLength() * bodySize)))
    }

    // Cryptography
    private fun getSymmetricData(bodySize: Int): ByteArray {
        return secureCommunication.decryptSymmetricData(socketBuffer.recv(bodySize))
    }

    // Cryptography
    private fun parseSymmetricKey(header: ByteArray) {
        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 1")
        Log.d("Service ${serverInformation.title}", header.decodeToString())
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 2")
        val messageFlags = buffer.get()
        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 3")
        val transmissionId = buffer.getLong()
        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 4")
        val bodySize = buffer.getLong()
        val symmetricKey = secureCommunication.decryptAsymmetricData(socketBuffer.recv(secureCommunication.getClientPublicKeyByteLength() * bodySize.toInt()))
        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 7")
        val iv = symmetricKey.copyOfRange(symmetricKey.indexOf(0x03) + 1, symmetricKey.size)
        Log.d("Service ${serverInformation.title}", "parseSymmetricKey Step: 8")
        secureCommunication.setSymmetricKey(symmetricKey, iv)
    }

    fun stopConnection() {
        active = false
    }
    // Server Connection
    private fun sendDataToServer(dataToSend: ByteArray) {
        launch {
            //Log.d("Service", dataToSend.toString(Charsets.UTF_8))
            socket.getOutputStream().write(dataToSend)
        }
    }
    // Server Connection
    fun cancel() {
        coroutineContext.cancel()
    }


    fun sendAsymmetricEncryptedFormattedMessage(data: BufferedReader, requestId: Long, maxStreamedSegments: Int = 1) {
        try {
            // Determine the chunk size based on the key size
            val chunkSize = secureCommunication.getServerPublicKeyByteLength() - 11
            var requestSent = false
            val transmissionId = getUniqueNumber()

            data.use { file ->
                while (true) {
                    val chunk = CharArray(chunkSize * maxStreamedSegments)
                    val bytesRead = file.read(chunk)
                    if (bytesRead == -1) {
                        sendDataToServer(secureCommunication.constructAsymmetricMessage_Intermittent(byteArrayOf(), terminated = true, transmissionId = transmissionId))
                        break  // Exit the loop if there is no more data
                    }
                    val messageBody = chunk.copyOf(bytesRead).toString().toByteArray()

                    if (requestSent) {
                        sendDataToServer(secureCommunication.constructAsymmetricMessage_Intermittent(messageBody, transmissionId))
                    } else {
                        sendDataToServer(secureCommunication.constructAsymmetricMessage_Request(messageBody, requestId, Transmission.STREAMED, transmissionId))
                    }
                }
            }

            releaseUniqueNumber(transmissionId)
        } catch (e: Exception) {
            Log.d("Service ${serverInformation.title}", "Unexpected (sendAsymmetricEncryptedFormattedMessage) error: $e")
        }
    }

    fun sendAsymmetricEncryptedFormattedMessage(data: ByteArray, requestId: Long) {
        try {
            sendDataToServer(secureCommunication.constructAsymmetricMessage_Request(data, requestId, Transmission.NORMAL))
        } catch (e: Exception) {
            Log.d("Service ${serverInformation.title}", "Unexpected (sendAsymmetricEncryptedFormattedMessage) error: $e")
        }
    }

    fun sendSymmetricEncryptedFormattedMessage(data: InputStream, requestId: Long, extraData: ByteArray = byteArrayOf(), maxDataSent: Int = 4096) {
        val transmissionId = getUniqueNumber()
        data.use { file ->
            val chunk = ByteArray(maxDataSent)
            var bytesRead = file.read(chunk)
            var messageBody = chunk.copyOf(bytesRead)
            sendDataToServer(secureCommunication.constructSymmetricMessage_Request(extraData + messageBody, requestId, Transmission.STREAMED, transmissionId))

            while (true) {
                bytesRead = file.read(chunk)
                messageBody = chunk.copyOf(bytesRead)
                if (bytesRead == -1) {
                    secureCommunication.constructSymmetricMessage_Intermittent(byteArrayOf(),transmissionId,terminated = true)
                    break  // Exit the loop if there is no more data
                }
                secureCommunication.constructSymmetricMessage_Intermittent(messageBody, transmissionId)
            }
        }
        releaseUniqueNumber(transmissionId)
    }

    fun sendSymmetricEncryptedFormattedMessage(data: ByteArray, requestId: Long) {
        sendDataToServer(secureCommunication.constructSymmetricMessage_Request(data, requestId, Transmission.NORMAL))
    }

    // Server Connection
    private fun getUniqueNumber(): Long {
        if (this.availableNumbers.isEmpty()) {
            throw IllegalArgumentException("No available unique numbers in the pool")
        }
        return availableNumbers.first().also {
            availableNumbers.remove(it)
        }
    }

    // Server Connection
    private fun releaseUniqueNumber(number: Long) {
        if (number < 0 || number > 255) {
            throw IllegalArgumentException("Number must be in the range of 0-255")
        }
        availableNumbers.add(number)
    }
}