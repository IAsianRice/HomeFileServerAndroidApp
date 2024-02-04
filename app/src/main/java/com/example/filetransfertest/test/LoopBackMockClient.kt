package com.example.filetransfertest.test

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.filetransfertest.byteArrayToLong
import com.example.filetransfertest.longToByteArray
import com.example.filetransfertest.readUTF8FromBufferUntilDelimiter
import java.io.BufferedInputStream
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

class LoopBackMockClient(private val serverAddress: String, private val serverPort: Int) {
    var privateKey = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCvZOCdp5G/eR6O\n" +
            "LeQ8gv1q4N9+F1Bbaw0gHDC3PQdRfCLfKywKoPecH16Gi5j1ywx2ShgKzLNeyj1c\n" +
            "5XRZNAhCZdqo4XT1hlwri1MVGvYnt59hvIngJb2z2g3/GL2w7nwur5X0i8+m12uu\n" +
            "YiSPom/Ft/nCWwtze/ofD7yaIiHWrnXMkhIiAE4V+mT21hsRbWnX3Tge7egpDC/u\n" +
            "ZwBCggH5vh5st6nIOoVmuucjCMoKBrjWjuezzDnNtP2spqKFLTIQtuaGQoB0qtEu\n" +
            "EYGk1lZJY49YbkdX7ZLTYSph6bx0wrWCHgs0ROu//+F5HktSSsHN3lrnQq1OSdfB\n" +
            "hsj3LIr3AgMBAAECggEBAKdjrQKJNA36t/CVV6dwlA52gpVCBszYg/RLMysNsXhQ\n" +
            "8q7t8uBJ80YqofniLJ8Xo9MYr41PAzjJ5npL+hhQEjncHyzhzs+r5Tchhq8zxgzt\n" +
            "r8yFNUiYfE2zI3x7zj2XvSiJJqH29LyUBw6sV5xFOEsIY/LbFTin73PJfwPS41yQ\n" +
            "GpMCZbyOL+1BmtYrX8526Mnc/AKJZL+usQoLU4+Etbhy0U/fYdMv3TNNlrbBS6CV\n" +
            "LuiVDTRNXGYPrY7jSAHBnrmp/fAN7Vk/ohdL0XJEXcfVhy05TCvwtEbm0KAlgFZq\n" +
            "G/CN0ocxfaUSwDFN2wwAr32lilBNpRdZSMvJ8ZQylJECgYEA31oKLDlOhO+PCInR\n" +
            "Wx3RCof/mRQ+rw2vQko3WVtep2njxZYjBHVcha6JdBBzBtwF6INdzz/inBriMi3d\n" +
            "/ddnX/JbxibRSSCj1jTaQkJyoiVGL5e9c91V7DnFuW2sj+fF2xeXbKLfr1qJGUJH\n" +
            "51ILG0b/TvvH/i24C0zBNrMv4hUCgYEAyQg7Y49NzvbipY1V/ucEkxwS7+FU1AnW\n" +
            "0IM8cKi+VXDiF4kt7e5UDBmgqgHcbBTYJ/cIw5uYigvewZRoXUbHQFh9ZQMUmCF5\n" +
            "j+nb+qePzSPlhFlfo9siWmHMhrmNnZlx5yrKc8BK9rTyvLRaUpLYG7D74LLTA2O7\n" +
            "4dVhLl8dV9sCgYBTPbJ24vDBmKq9KugMxmMHQsJe4e2uvjHoSdKxCd2QFWXfWOI6\n" +
            "IidWZ0MxL4Q/NTc6feMzf4LMXTSaMQAAQAx19NSBWCw9zd1h0xbZBPzGM5Ah4rKo\n" +
            "AQCp5qorPpXGmEHkMrmrslQdfWeFM2+q5afgBajXXf0eXRmvlG6aHVQmNQKBgQCD\n" +
            "v1dciiAOERK8OIIxSalf17g5aU00oTYc9MT5eD00tYQWtJpbIiQGwgzfHdUdKBP5\n" +
            "LRabSy+O2PSlnONArhALBXM4bepjQRzvye2WuZWudWYHdKMPOZ+r6AgoCxbaQCO7\n" +
            "wfloHo+CW1fxf6jqEL58d2K7Abb0s7n/6N2FjGQonQKBgE4WSJq0hc41DEmVileb\n" +
            "XPq8M4jAvYnKXCB3TOiyXBccSLpUXQJ+/6OeYMKXUesS+X4fm3XoXwIMAal6F7f7\n" +
            "HoOFsWN4tm5JAlIMcTy5IpSv9ZWehAqPXlwdwf0CnNyBtGURQ44FVi6miQjr7Su2\n" +
            "hwsmRx5Ld/MZ4Tk61xwZKqih\n" +
            "-----END PRIVATE KEY-----"
    val publicKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr2TgnaeRv3keji3kPIL9\n" +
            "auDffhdQW2sNIBwwtz0HUXwi3yssCqD3nB9ehouY9csMdkoYCsyzXso9XOV0WTQI\n" +
            "QmXaqOF09YZcK4tTFRr2J7efYbyJ4CW9s9oN/xi9sO58Lq+V9IvPptdrrmIkj6Jv\n" +
            "xbf5wlsLc3v6Hw+8miIh1q51zJISIgBOFfpk9tYbEW1p1904Hu3oKQwv7mcAQoIB\n" +
            "+b4ebLepyDqFZrrnIwjKCga41o7ns8w5zbT9rKaihS0yELbmhkKAdKrRLhGBpNZW\n" +
            "SWOPWG5HV+2S02EqYem8dMK1gh4LNETrv//heR5LUkrBzd5a50KtTknXwYbI9yyK\n" +
            "9wIDAQAB\n" +
            "-----END PUBLIC KEY-----"
    var symmetricKey: SecretKeySpec? = null
    val symmetricKeyEncryptCipher: Cipher = Cipher.getInstance("AES")
    val symmetricKeyDecryptCipher: Cipher = Cipher.getInstance("AES")
    var clientPrivateKey: RSAPrivateKey? = null
    val clientPrivateKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    var serverPublicKey: RSAPublicKey? = null
    var serverPublicKeyCipher: Cipher? = null
    var reader: BufferedInputStream? = null
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(): String {
        println("Sending Message Inner")
        clientPrivateKey = parsePrivateKey(privateKey) as RSAPrivateKey
        clientPrivateKeyCipher.init(Cipher.DECRYPT_MODE, clientPrivateKey)
        val socket = Socket(serverAddress, serverPort)
        reader = BufferedInputStream(socket.getInputStream())

        serverPublicKey = parsePublicKey(readUTF8FromBufferUntilDelimiter(reader!!, 0x03)) as RSAPublicKey
        println("Got Key")

        serverPublicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        serverPublicKeyCipher!!.init(Cipher.ENCRYPT_MODE, serverPublicKey)
        println("Sending My Key")
        // Send message to the server
        val writer = socket.getOutputStream()
        val msg = sendAsymmetricEncryptedFormattedMessage(publicKey.toByteArray(Charsets.UTF_8))
        println(msg.toString(Charsets.UTF_8))
        println(publicKey)
        writer.write(msg)
        writer.flush()

        symmetricKey = parseSymmetricKey(parseHeader(socket, readHeader(socket)).toString(Charsets.UTF_8))
        symmetricKeyEncryptCipher.init(Cipher.ENCRYPT_MODE, symmetricKey)
        symmetricKeyDecryptCipher.init(Cipher.DECRYPT_MODE, symmetricKey)
        writer.write(sendSymmetricEncryptedFormattedMessage("HEYEHYEHEYEHYEHEYHEYHEY".toByteArray(Charsets.UTF_8)))
        writer.flush()

        // Close the connections
        writer.close()
        socket.close()
        println("Client Ending")

        return "HEYEHYEHEYEHYEHEYHEYHEY"
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

    fun readHeader(socket: Socket): ByteArray {
        println("Reading Header! Client")
        val chunkSize = ((clientPrivateKey!!.modulus!!.bitLength()) / 8)
        val buffer = ByteArray(chunkSize)
        println("Reading Header 1 Client")
        var bytesRead = 0
        while (bytesRead != chunkSize)
        {
            bytesRead = reader!!.read(buffer)
        }
        println("Reading Header 2 Client")
        println(buffer.toString(Charsets.UTF_8))

        val headerSize = clientPrivateKeyCipher.doFinal(buffer)
        val segments = byteArrayToLong(headerSize.sliceArray(0..7), ByteOrder.LITTLE_ENDIAN)

        println("Parsing Header Segments: ${segments} Client")
        println("Reading Header 3 Client")
        var header = byteArrayOf()
        for (i in 1..segments) {
            reader!!.read(buffer)
            header = header.plus(clientPrivateKeyCipher.doFinal(buffer))
        }
        println("Reading Header 4")

        println("Finished Header!")
        return header
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseHeader(socket: Socket, header: ByteArray): ByteArray {

        println("Parsing Header!")
        var encryptionMethod: Byte = header[0]
        var bodySegments: Long = byteArrayToLong(header.sliceArray(1..8), ByteOrder.LITTLE_ENDIAN)

        val chunkSize = ((clientPrivateKey!!.modulus!!.bitLength()) / 8)
        val buffer = ByteArray(chunkSize)

        println("Parsing Header body Segments: ${bodySegments}")
        var body = byteArrayOf()
        if (encryptionMethod == 0x01.toByte()) {
            for (i in 1..bodySegments) {
                reader!!.read(buffer)
                body = body.plus(clientPrivateKeyCipher.doFinal(buffer))
            }
        }
        else if (encryptionMethod == 0x02.toByte()) {
            while (!body.contains(0x04)) {
                reader!!.read(buffer)
                body = body.plus(buffer)
            }
            body.copyOf(body.indexOf(0x04))
            body = symmetricKeyDecryptCipher.doFinal(Base64.getDecoder().decode(body))
        }


        println("Finished Parsing Header!")
        println(body.toString(Charsets.UTF_8))
        return body
    }

    fun sendAsymmetricEncryptedFormattedMessage(data: ByteArray): ByteArray {
        println("Staring formatted Message")
        val chunkSize = ((serverPublicKey!!.modulus!!.bitLength()) / 8) - 11
        var completeMessage = byteArrayOf()
        var headerChunks: Long = 1
        var messageHeader = byteArrayOf()
        var bodyChunks: Long = 0
        var messageBody = byteArrayOf()
        for (i in data.indices step chunkSize) {
            val endIndex = Math.min(i + chunkSize, data.size)
            val chunk = data.sliceArray(i until endIndex)
            val encryptedChunk = serverPublicKeyCipher!!.doFinal(chunk)
            messageBody = messageBody.plus(encryptedChunk)
            bodyChunks += 1
        }
        println("Finished Body")
        messageHeader = serverPublicKeyCipher!!.doFinal(messageHeader.plus(0x01).plus(longToByteArray(bodyChunks, ByteOrder.LITTLE_ENDIAN)))
        println("Finished Header")
        completeMessage = serverPublicKeyCipher!!.doFinal(completeMessage.plus(longToByteArray(headerChunks, ByteOrder.LITTLE_ENDIAN)))
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
        messageHeader = serverPublicKeyCipher!!.doFinal(messageHeader.plus(0x02).plus(longToByteArray(bodyChunks, ByteOrder.LITTLE_ENDIAN)))
        println("Finished Header")
        completeMessage = serverPublicKeyCipher!!.doFinal(completeMessage.plus(longToByteArray(headerChunks, ByteOrder.LITTLE_ENDIAN)))
            .plus(messageHeader)
            .plus(messageBody)
            .plus(0x04)
        println(messageBody.toString(Charsets.UTF_8))
        println("Finished ALL!")
        return completeMessage
    }

}