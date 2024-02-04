package com.example.filetransfertest.test
import com.example.filetransfertest.toBytes
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ServerConnectionTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun shifting_isCorrect() {
        assertEquals(4096.toBytes().contentToString(), byteArrayOf(0x00, 0x10, 0x00, 0x00).contentToString())
        assertEquals(4096.toBytes(8).contentToString(), byteArrayOf(0x00,0x10,0x00,0x00,0x00, 0x00, 0x00, 0x00).contentToString())
        assertEquals(4096.toBytes(bigEndian = true).contentToString(), byteArrayOf(0x00, 0x00, 0x10, 0x00).contentToString())
        assertEquals(4096.toBytes(8, bigEndian = true).contentToString(), byteArrayOf(0x00,0x00,0x00,0x00,0x00, 0x00, 0x10, 0x00).contentToString())
        assertEquals(4096.toBytes(3).contentToString(), byteArrayOf(0x00, 0x10, 0x00).contentToString())
        assertEquals(4096.toBytes(1).contentToString(), byteArrayOf(0x00).contentToString())
        assertEquals(4096.toBytes(3, bigEndian = true).contentToString(), byteArrayOf(0x00, 0x10, 0x00).contentToString())
        assertEquals(4096.toBytes(1, bigEndian = true).contentToString(), byteArrayOf(0x00).contentToString())
    }

    /*
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testSocketInteraction() {
        val serverConn = LoopBackMockServer(50007)
        val clientConn = LoopBackMockClient("127.0.0.1", 50007)
        val job = GlobalScope.launch {
            serverConn.startServer()
        }
        println("Running")
        val job2 = GlobalScope.launch {
            var triggered = false
            while (!triggered) {
                println("${serverConn.isOn}")
                if (serverConn.isOn)
                {
                    println("Sending Message")
                    clientConn.sendMessage()
                    triggered = true
                }
            }
        }

        /*val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPrivateKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        var rsaPublicKey: RSAPublicKey? = null
        rsaPrivateKeyCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        rsaPublicKey = keyPair.public as RSAPublicKey
        val serverStateFlow = MutableStateFlow<ServerState>(ServerState.Unresponsive)
        val serverMessageFlow = MutableStateFlow<String>("")
        val conn = ServerConnection(
            Socket(),
            rsaPublicKey,
            rsaPrivateKeyCipher,
            Cipher.getInstance("RSA/ECB/PKCS1Padding"),
            "Mock!",
            "127.0.0.1",
            50007,
            serverStateFlow.asStateFlow(),
            serverStateFlow,
            serverMessageFlow.asStateFlow(),
            serverMessageFlow)
        conn.establishAndManageServerConnection()
        assertEquals(null, conn.serverPublicKey)*/
        //serverConn.stopServer()

        runBlocking {
            job.join()
            println("job fin")
            job2.join()
            println("job2 fin")
        }
    }*/
}