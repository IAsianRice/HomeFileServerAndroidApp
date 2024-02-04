package com.example.filetransfertest

import android.util.Base64
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureCommunication (
                           var clientPublicKey: RSAPublicKey,
                           val clientPrivateKeyCipher: Cipher,
) {

    var symmetricKey: SecretKeySpec? = null
    lateinit var serverPublicKey: RSAPublicKey
    val symmetricKeyEncryptCipher: Cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val symmetricKeyDecryptCipher: Cipher = Cipher.getInstance("AES/CFB/NoPadding")
    private val serverPublicKeyCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    var isServerSymmetricKeySet: Boolean = false

    fun getClientPublicKeyPEM(): String {
        val encodedKey: String = Base64.encode(clientPublicKey.encoded, Base64.DEFAULT).toString(Charsets.UTF_8)
        return "-----BEGIN PUBLIC KEY-----\n$encodedKey-----END PUBLIC KEY-----\n"
    }

    fun getClientPublicKeyByteLength(): Int{
        return clientPublicKey.modulus.bitLength() / 8
    }

    fun getServerPublicKeyByteLength(): Int{
        return serverPublicKey.modulus.bitLength() / 8
    }

    // Cryptography
    fun decryptAsymmetricData(encryptedData: ByteArray): ByteArray {
        var data = byteArrayOf()

        val chunkSize = getClientPublicKeyByteLength()
        for (i in encryptedData.indices step chunkSize) {
            val endIndex = Math.min(i + chunkSize, encryptedData.size)
            val chunk = encryptedData.sliceArray(i until endIndex)
            data += clientPrivateKeyCipher.doFinal(chunk).asList()
        }

        return data
    }

    // Cryptography
    fun decryptSymmetricData(encryptedData: ByteArray): ByteArray {
        return symmetricKeyDecryptCipher.doFinal(encryptedData)
    }

    // Cryptography
    fun setServerPublicKey(data: String) {
        val publicKeyPEM = data.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\n","")
        val encoded = Base64.decode(publicKeyPEM, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        serverPublicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey
        serverPublicKeyCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey)
    }

    // Cryptography
    fun setSymmetricKey(data: ByteArray, iv: ByteArray) {
        symmetricKey = SecretKeySpec(Base64.decode(data, Base64.DEFAULT), "AES")
        symmetricKeyEncryptCipher.init(Cipher.ENCRYPT_MODE, symmetricKey, IvParameterSpec(iv))
        symmetricKeyDecryptCipher.init(Cipher.DECRYPT_MODE, symmetricKey, IvParameterSpec(iv))
    }

    fun encryptWithPublicKey(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // Use PKCS1Padding for RSA
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey)

        return cipher.doFinal(data)
    }

    // Cryptography
    fun constructAsymmetricMessage_Intermittent(data: ByteArray, transmissionId: Long = 0L, terminated: Boolean = false): ByteArray {
        var bodyChunks: Long = 0
        var messageBody = byteArrayOf()
        val chunkSize = getServerPublicKeyByteLength() - 11
        for (i in data.indices step chunkSize) {
            val endIndex = Math.min(i + chunkSize, data.size)
            val chunk = data.sliceArray(i until endIndex)
            val encryptedChunk = serverPublicKeyCipher.doFinal(chunk)
            messageBody = messageBody.plus(encryptedChunk)
            bodyChunks += 1
        }
        val encryptedMessageBody = symmetricKeyEncryptCipher.doFinal(messageBody)
        val header = serverPublicKeyCipher.doFinal(formatIntermittentMessageHeader(transmissionId, bodyChunks, EncryptionMethod.NONE, terminated = if (terminated) 1L else 0L))
        return header + encryptedMessageBody
    }
    // Cryptography
    fun constructAsymmetricMessage_Request(data: ByteArray, requestId: Long, messageType: Byte = Transmission.NORMAL, transmissionId: Long = 0L): ByteArray {
        var bodyChunks: Long = 0
        var messageBody = byteArrayOf()
        val chunkSize = ((serverPublicKey.modulus!!.bitLength()) / 8) - 11
        for (i in data.indices step chunkSize) {
            val endIndex = Math.min(i + chunkSize, data.size)
            val chunk = data.sliceArray(i until endIndex)
            val encryptedChunk = serverPublicKeyCipher.doFinal(chunk)
            messageBody = messageBody.plus(encryptedChunk)
            bodyChunks += 1
        }
        val encryptedMessageBody = symmetricKeyEncryptCipher.doFinal(messageBody)
        val header = serverPublicKeyCipher.doFinal(formatRequestMessageHeader(messageType, transmissionId, bodyChunks, EncryptionMethod.NONE, requestId))
        return header + encryptedMessageBody
    }
    // Cryptography
    fun constructSymmetricMessage_Intermittent(data: ByteArray, transmissionId: Long = 0L, terminated: Boolean = false): ByteArray {
        val encryptedMessageBody = symmetricKeyEncryptCipher.doFinal(data)
        val header = serverPublicKeyCipher.doFinal(formatIntermittentMessageHeader(transmissionId, encryptedMessageBody.size.toLong(), EncryptionMethod.NONE, terminated = if (terminated) 1L else 0L))
        return header + encryptedMessageBody
    }
    // Cryptography
    fun constructSymmetricMessage_Request(data: ByteArray, requestId: Long, messageType: Byte = Transmission.NORMAL, transmissionId: Long = 0L): ByteArray {
        val encryptedMessageBody = symmetricKeyEncryptCipher.doFinal(data)
        val header = serverPublicKeyCipher.doFinal(formatRequestMessageHeader(messageType, transmissionId, encryptedMessageBody.size.toLong(), EncryptionMethod.NONE, requestId))
        return header + encryptedMessageBody
    }
}