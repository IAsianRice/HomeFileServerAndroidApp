package com.example.filetransfertest

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Transmission {
    val NORMAL: Byte = 0b00000000
    val STREAMED: Byte = 0b00000010
}

object EncryptionMethod {
    val ASYMMETRIC: Byte = 1
    val SYMMETRIC: Byte = 2
    val NONE: Byte = 3
}

fun formatRequestMessageHeader(
    messageType: Byte = Transmission.NORMAL,
    transmissionId: Long = 0,
    size: Long = 0,
    encryptionMethod: Byte = EncryptionMethod.NONE,
    requestId: Long = 0
): ByteArray {
    return ByteBuffer.allocate(29).order(ByteOrder.LITTLE_ENDIAN)
        .put(messageType)
        .putLong(transmissionId)
        .putLong(size)
        .put(encryptionMethod)
        .putLong(requestId)
        .array()
}

fun formatIntermittentMessageHeader(
    transmissionId: Long,
    size: Long,
    encryptionMethod: Byte = EncryptionMethod.NONE,
    terminated: Long
): ByteArray {
    return ByteBuffer.allocate(29).order(ByteOrder.LITTLE_ENDIAN)
        .put(0b00000001)
        .putLong(transmissionId)
        .putLong(size)
        .put(encryptionMethod)
        .putLong(terminated)
        .array()
}