package com.example.filetransfertest.test

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.filetransfertest.byteArrayToLong
import com.example.filetransfertest.longToByteArray
import java.io.BufferedInputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class LoopBackMockServer(private val port: Int) {

    private var serverSocket: ServerSocket? = null
    var isOn: Boolean = false
    var privateKey = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDy/VJFp4WZubZX\n" +
            "W3CtX8jMPYiAA/XurqYn7xIuM4pJVsscbN8ICbkdEBJcEsS+1Da4Pi0XjFn2HK3d\n" +
            "aRwMNri4ilVnD+imWoVi3SqZyZfIJ5TMnHsuTMQ7HRqpJxB/DtVlzWQ4koeiQLrn\n" +
            "+MobrkDmN6bQqRQzRUb1p5ummPlDj8plfY4RfX6iH8DF+0tJ21JO5lSINrEVtKrx\n" +
            "D3jJzH2aeJqt96ns70Vm/m0DYP5ivuy7D2t5V/P9DYup3iZX6Hv0kjeEfF4t1cJs\n" +
            "IaeaE1GOvN7n5gUMn3vVRZarWZERu18FY8IDYjD2BsMFVluZfkYSD2qnb8V3UECJ\n" +
            "0yq+Q11JAgMBAAECggEAQAYnS1schKi+Nr5NLiFy0v3TnYdtoXo7JYKEk8/YvAI2\n" +
            "ekLlcUS+lb3KWKBJXs6jrGAVmYLNoV9lIFW1ojI48cbhQcqbTlnDk40a0HKhn4du\n" +
            "WYtRmZ14hurgP3mfixrpd21q3s0dF89ecAjJmAmN6Br5xTkHrYqva8LB7F+aLgaN\n" +
            "qiR+fBlnUvbO4p4jzWk7FKEvMdEkBYUWyCRcjTm6jiOOhXuF66cMZ5vKPPI45ebZ\n" +
            "BJI0nzrcC0BzQ3z+uLIGl/tWZobH5mUF9pacHM9OuiXvaJS/9h6/VDmCM8LtBcM2\n" +
            "6qBVJKa7wPUoHpZDgqLVTTNyHwLQ4ngKRAcDvmDFEQKBgQD67JMYtaub/ZcytjtQ\n" +
            "XKGSx2HL02+70fDuMkuqnNoNmso/NEWzP9rSbObr14OTTkPFpZfskJt3RDAxM0l2\n" +
            "Mgd2hl6CPt9HlvibWU5CYptWsMdeOcMSoI6/3+0Tw3Y9ew4ZUDMVtPf7g8O2m6VC\n" +
            "7H596tiIo4PQZnUM9lrFFGujDwKBgQD356g15f3V4z3eRhz8l/7bBGaH1NqPK1D8\n" +
            "iME+w3oewlVHPQ9PssIjpyCrNFaKr2/XoT/ar1dU10dJNSso6vuJ/xqHhg5Bvhtp\n" +
            "rHb3GrdwM3XdcrcSSIEWScbzvFhBX4YfumtxdzclJK71lgNf0q3yuvrhgSZ9BdAK\n" +
            "xr8G/9IaJwKBgEY8/Lv49o2WwcEfQ6EuHMhCaXytteQkU0XJ7GZYu6z9PEnO7wfL\n" +
            "AwzOESHJVPph+RSVUt6UaABNpRPQoGqmfHJQEJpy0PtwJi0OJZrtkUAGAfi9QH6x\n" +
            "VOQe4XUbzLhO/tZVHcegvZ2494rhLLR8ELhcibpN0zL9yxwSxbvPCIY/AoGBAN8X\n" +
            "MM/4PjsZ+ZYqoHFL00SDrEU2bs0CXSo1Yzo5LY+6sIWgVEQTiRQOlZ6cRfkeMdbp\n" +
            "vI3u1vd5mCQwXbu/40dnNBebsv7EtuSdGJ39A/FLzhjP4CHq74gAMrMq3n1OGmQ5\n" +
            "HfGJKC4b2fsV2cmEzZcvXxBKajl+Xp8kBcqlswxnAoGAGMocez2XDuwgd4cq3oEW\n" +
            "UAbtfUpE9RBnMemBBfWz0mYxzaZfM0lKnKeqgAzuqYNd+A+ELCDWofgK7JRvLgxQ\n" +
            "MgUjneoSwNz96jsyAHI2B39aSAN/lYKrK9TiaTIfxFS3aXg28vxs2lInLlc94Nf/\n" +
            "WbhLLVDhi6iD4N/doKpG7u8=\n" +
            "-----END PRIVATE KEY-----"
    val publicKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8v1SRaeFmbm2V1twrV/I\n" +
            "zD2IgAP17q6mJ+8SLjOKSVbLHGzfCAm5HRASXBLEvtQ2uD4tF4xZ9hyt3WkcDDa4\n" +
            "uIpVZw/oplqFYt0qmcmXyCeUzJx7LkzEOx0aqScQfw7VZc1kOJKHokC65/jKG65A\n" +
            "5jem0KkUM0VG9aebppj5Q4/KZX2OEX1+oh/AxftLSdtSTuZUiDaxFbSq8Q94ycx9\n" +
            "mniarfep7O9FZv5tA2D+Yr7suw9reVfz/Q2Lqd4mV+h79JI3hHxeLdXCbCGnmhNR\n" +
            "jrze5+YFDJ971UWWq1mREbtfBWPCA2Iw9gbDBVZbmX5GEg9qp2/Fd1BAidMqvkNd\n" +
            "SQIDAQAB\n" +
            "-----END PUBLIC KEY-----"
    val symmetricKey = "pFqOWGe+xGI251/Msk9AGBaL31P4PHgN7MCw6bHNpiw="
    val symmetricKeyEncryptCipher: Cipher = Cipher.getInstance("AES")
    val symmetricKeyDecryptCipher: Cipher = Cipher.getInstance("AES")
    var serverPrivateKey: RSAPrivateKey? = null
    val serverPrivateKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    var clientPublicKey: RSAPublicKey? = null
    val clientPublicKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    var reader: BufferedInputStream? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startServer() {
        try {
            serverSocket = ServerSocket(port)
            serverPrivateKey = parsePrivateKey(privateKey) as RSAPrivateKey
            serverPrivateKeyCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey)
            println("Parse Symm")
            symmetricKeyEncryptCipher.init(Cipher.ENCRYPT_MODE, parseSymmetricKey(symmetricKey))
            symmetricKeyDecryptCipher.init(Cipher.DECRYPT_MODE, parseSymmetricKey(symmetricKey))
            println("LoopbackServer: Server started on port $port")
            //while (true) {
            isOn = true
            println("1")
            val clientSocket: Socket = serverSocket!!.accept()
            println("2")
            handleClient(clientSocket)
            //}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleClient(clientSocket: Socket) {

        reader = BufferedInputStream(clientSocket.getInputStream())
        println("Server: Send Pub Key")
        // Send Pub Key
        val writer = clientSocket.getOutputStream()
        writer.write(publicKey.encodeToByteArray().plus(0x03).plus("\n".toByteArray(Charsets.UTF_8)))
        writer.flush()

        println("Server: Receive Client Pub Key")
        // Receive Client Pub Key
        clientPublicKey = parsePublicKey(parseHeader(clientSocket, readHeader(clientSocket)).toString(Charsets.UTF_8)) as RSAPublicKey
        clientPublicKeyCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey)

        println("Server: Send Symmetric Key")
        // Send Symmetric Key
        writer.write(sendAsymmetricEncryptedFormattedMessage(symmetricKey.toByteArray(Charsets.UTF_8)))
        writer.flush()

        println(parseHeader(clientSocket, readHeader(clientSocket)).toString(Charsets.UTF_8))
        // Close the connections
        reader!!.close()
        writer.close()
        clientSocket.close()
        println("Connection Ending")
    }

    fun stopServer() {
        try {
            serverSocket?.close()
            println("LoopbackServer: Server stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun parsePublicKey(publicKeyPEM: String): PublicKey? {
        val publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\n","")
        val encoded = Base64.getDecoder().decode(publicKeyPEM)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseSymmetricKey(symmetricKey: String): SecretKeySpec {
        println("Parse Symm2")
        return SecretKeySpec(Base64.getDecoder().decode(symmetricKey), "AES")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parsePrivateKey(privateKeyPEM: String): PrivateKey {
        val privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("\n","")
        val encoded = Base64.getDecoder().decode(privateKeyPEM)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }

    fun readHeader(clientSocket: Socket): ByteArray {
        println("Reading Header!")
        val chunkSize = ((serverPrivateKey!!.modulus!!.bitLength()) / 8)
        val buffer = ByteArray(chunkSize)
        println("Reading Header 1")
        reader!!.read(buffer)
        println("Reading Header 2")

        val headerSize = serverPrivateKeyCipher.doFinal(buffer)
        val segments = byteArrayToLong(headerSize.sliceArray(0..7), ByteOrder.LITTLE_ENDIAN)

        println("Parsing Header Segments: ${segments}")
        println("Reading Header 3")
        var header = byteArrayOf()
        for (i in 1..segments) {
            reader!!.read(buffer)
            header = header.plus(serverPrivateKeyCipher.doFinal(buffer))
        }
        println("Reading Header 4")

        println("Finished Header!")
        return header
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseHeader(clientSocket: Socket, header: ByteArray): ByteArray {

        println("Parsing Header!")
        var encryptionMethod: Byte = header[0]
        var bodySegments: Long = byteArrayToLong(header.sliceArray(1..8), ByteOrder.LITTLE_ENDIAN)

        val chunkSize = ((serverPrivateKey!!.modulus!!.bitLength()) / 8)
        val buffer = ByteArray(chunkSize)

        println("Parsing Header body Segments: ${bodySegments}")
        var body = byteArrayOf()
        if (encryptionMethod == 0x01.toByte()) {
            for (i in 1..bodySegments) {
                reader!!.read(buffer)
                body = body.plus(serverPrivateKeyCipher.doFinal(buffer))
            }
        }
        else if (encryptionMethod == 0x02.toByte()) {
            while (!body.contains(0x04)) {
                reader!!.read(buffer)
                body = body.plus(buffer)
            }
            body = body.copyOf(body.indexOf(0x04))
            println(body.toString(Charsets.UTF_8))
            body = symmetricKeyDecryptCipher.doFinal(Base64.getDecoder().decode(body))
        }

        println("Finished Parsing Header!")
        println(body.toString(Charsets.UTF_8))
        return body
    }

    fun sendAsymmetricEncryptedFormattedMessage(data: ByteArray): ByteArray {
        println("Staring formatted Message")
        val chunkSize = ((clientPublicKey!!.modulus!!.bitLength()) / 8) - 11
        var completeMessage = byteArrayOf()
        var headerChunks: Long = 1
        var messageHeader = byteArrayOf()
        var bodyChunks: Long = 0
        var messageBody = byteArrayOf()
        for (i in data.indices step chunkSize) {
            val endIndex = Math.min(i + chunkSize, data.size)
            val chunk = data.sliceArray(i until endIndex)
            val encryptedChunk = clientPublicKeyCipher!!.doFinal(chunk)
            messageBody = messageBody.plus(encryptedChunk)
            bodyChunks += 1
        }
        println("Finished Body")
        messageHeader = clientPublicKeyCipher!!.doFinal(messageHeader.plus(0x01).plus(
            longToByteArray(bodyChunks, ByteOrder.LITTLE_ENDIAN)
        ))
        println("Finished Header")
        completeMessage = clientPublicKeyCipher!!.doFinal(completeMessage.plus(longToByteArray(headerChunks, ByteOrder.LITTLE_ENDIAN)))
            .plus(messageHeader)
            .plus(messageBody)
        println("Finished ALL!")
        return completeMessage
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendSymmetricEncryptedFormattedMessage(data: ByteArray): ByteArray {
        println("Staring formatted Message")
        var completeMessage = byteArrayOf()
        var headerChunks: Long = 1
        var messageHeader = byteArrayOf()
        var bodyChunks: Long = 0
        var messageBody = Base64.getEncoder().encodeToString(symmetricKeyEncryptCipher.doFinal(data)).encodeToByteArray()
        println("Finished Body")
        messageHeader = clientPublicKeyCipher!!.doFinal(messageHeader.plus(0x02).plus(longToByteArray(bodyChunks, ByteOrder.LITTLE_ENDIAN)))
        println("Finished Header")
        completeMessage = clientPublicKeyCipher!!.doFinal(completeMessage.plus(longToByteArray(headerChunks, ByteOrder.LITTLE_ENDIAN)))
            .plus(messageHeader)
            .plus(messageBody)
            .plus(0x04)
        println(messageBody.toString(Charsets.UTF_8))
        println("Finished ALL!")
        return completeMessage
    }

}