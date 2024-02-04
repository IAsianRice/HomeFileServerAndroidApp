package com.example.filetransfertest

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.math.BigInteger
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

// Length in Bytes
fun intToLittleEndianByteArray(value: Int, length: Int): ByteArray {
    val byteArray = ByteArray(length)
    for (i: Int in 0 until length) {
        byteArray[i] = (value shr (8 * i) and 0xFF).toByte()
    }
    return byteArray
}

// Length in Bytes
fun littleEndianByteArrayToInt(byteArray: ByteArray): Int {
    var value = 0
    for (i: Int in byteArray.indices) {
        value = value or ((byteArray[i].toInt() and 0xFF) shl (8 * i))
    }

    return value
}

fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String? {
    val cursor = contentResolver.query(fileUri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1) {
            return it.getString(nameIndex)
        }
    }
    return null
}

fun readFileContents(fileUri: Uri, contentResolver: ContentResolver): Pair<ByteArray, Int> {
    val inputStream = contentResolver.openInputStream(fileUri)
    if (inputStream != null) {
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096) // Adjust buffer size as needed

        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        val fileContents = outputStream.toByteArray()
        val bytesReadCount = fileContents.size
        Log.d("File", "$bytesReadCount")
        Log.d("File", "$fileContents.")
        inputStream.close()
        outputStream.close()

        return Pair(fileContents, bytesReadCount)
    }
    return byteArrayOf() to 0
}

fun parsePublicKeyFromDER(derBytes: ByteArray): PublicKey {
    val keyFactory = KeyFactory.getInstance("RSA") // Replace with the appropriate algorithm if needed
    val publicKeySpec = X509EncodedKeySpec(derBytes)
    return keyFactory.generatePublic(publicKeySpec)
}
fun parsePublicKeyFromExpAndMod(publicExponent: String, modulus: String): PublicKey {
    // Remove PEM header and footer lines, and any line breaks

    // Decode the base64-encoded key content

    // Create an X.509 encoded public key spec
    val keyFactory = KeyFactory.getInstance("RSA") // Use the appropriate algorithm
    val publicKeySpec = RSAPublicKeySpec(
        BigInteger(modulus),
        BigInteger(publicExponent)
    )

    // Generate a PublicKey from the X.509 encoded key spec
    return keyFactory.generatePublic(publicKeySpec)
}
fun encryptWithPublicKey(publicKey: PublicKey, data: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // Use PKCS1Padding for RSA
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)

    return cipher.doFinal(data)
}

fun pingServer(serverIP: String, serverPort: Int, timeoutMillis: Int): Boolean {
    return try {
        val socket = Socket()
        socket.connect(InetSocketAddress(serverIP, serverPort), timeoutMillis)

        // Create an output stream for writing to the server
        val out = PrintWriter(socket.getOutputStream(), true)

        // Send a ping message (you can customize the message)
        out.println("Ping")

        // Create an input stream for reading from the server
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        // Read the server's response (if any)
        val response = reader.readLine()

        // Close the socket connection
        socket.close()

        // Check if the response matches the expected ping response
        response == "Pong"
    } catch (e: Exception) {
        // Handle exceptions, such as connection failure or timeouts
        false
    }
}

fun getFileNameAndExtensionFromFileUri(uri: Uri): Pair<String, String> {
    val file = File(uri.path.toString())
    val name = file.name
    val extension = name.substringAfterLast(".", "")
    return Pair(name, extension)
}

fun publicKeyToPem(internalPem: String): String {

    return """
        |-----BEGIN PUBLIC KEY-----
        |${internalPem.trimMargin()}
        |-----END PUBLIC KEY-----${'\n'}
    """.trimMargin()
}

fun encryptData(data: String, key: SecretKeySpec): ByteArray {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(data.toByteArray())
}

fun decryptData(ciphertext: ByteArray, key: SecretKeySpec): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, key)
    val decryptedBytes = cipher.doFinal(ciphertext)
    return String(decryptedBytes)
}

fun generateSymmetricKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(256) // Key size (e.g., 128, 192, or 256 bits)

    return keyGenerator.generateKey()
}

fun Int.toBytes(size: Int = 4, bigEndian: Boolean = false): ByteArray {
    require(size > 0) { "Size must be greater than 0" }

    val byteArray = ByteArray(size)
    for (i in 0 until if (4 < size) 4 else size) {
        val shift = i * 8
        byteArray[if (bigEndian) size - 1 - i else i] = ((this shr shift) and 0xFF).toByte()
    }
    return byteArray

}

fun byteArrayToLong(byteArray: ByteArray, byteOrder: ByteOrder): Long {
    val buffer = ByteBuffer.wrap(byteArray)
    buffer.order(byteOrder)
    return buffer.getLong()
}
fun longToByteArray(value: Long, byteOrder: ByteOrder): ByteArray {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.order(byteOrder)
    buffer.putLong(value)
    return buffer.array()
}

fun readUTF8FromBufferUntilDelimiter(buffer: BufferedInputStream, delimiter: Byte): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    var nextByte: Int

    // Read bytes from the buffer until the delimiter is encountered
    while (true) {
        nextByte = buffer.read()
        if (nextByte == -1 || nextByte == delimiter.toInt()) {
            break
        }
        byteArrayOutputStream.write(nextByte)
    }

    // Convert the collected bytes to a UTF-8 string
    return byteArrayOutputStream.toString("UTF-8")
}

fun byteArrayToByteArrayList(byteArray: ByteArray, delimiter: Byte): List<ByteArray> {
    val result = mutableListOf<ByteArray>()
    var startIndex = 0

    for (i in byteArray.indices) {
        if (byteArray[i] == delimiter) {
            // Found a delimiter, create a subarray
            val subarray = byteArray.copyOfRange(startIndex, i)
            result.add(subarray)
            startIndex = i + 1
        }
    }

    // Add the last subarray (after the last delimiter)
    val lastSubarray = byteArray.copyOfRange(startIndex, byteArray.size)
    result.add(lastSubarray)

    return result
}

fun convertToPem(publicKey: RSAPublicKey): String {
    val encodedKey:String = Base64.encode(publicKey.encoded, Base64.DEFAULT).toString(Charsets.UTF_8)

    return "-----BEGIN PUBLIC KEY-----\n$encodedKey-----END PUBLIC KEY-----\n"
}