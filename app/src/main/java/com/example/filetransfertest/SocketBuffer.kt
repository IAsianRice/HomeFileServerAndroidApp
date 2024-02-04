package com.example.filetransfertest

import java.net.Socket

class SocketBuffer(private val socket: Socket) {
    private var intermediateBuffer: ByteArray = byteArrayOf()

    fun recv(numBytes: Int, delimiter: Byte? = null): ByteArray {
        try {
            val newDataNum = numBytes - intermediateBuffer.size
            var data: ByteArray

            if (newDataNum > 0) {
                val chunk = ByteArray(newDataNum)
                socket.getInputStream().read(chunk)
                data = recvAllFromIntermediateBuffer() + chunk
            } else {
                data = recvFromIntermediateBuffer(numBytes)
            }

            if (delimiter == null) {
                return data
            } else {
                val delimiterIndex = data.indexOf(delimiter)
                if (delimiterIndex != -1) {
                    intermediateBuffer += data.sliceArray(IntRange(delimiterIndex + 1, data.size - 1))
                    return data.sliceArray(IntRange(0, delimiterIndex))
                } else {
                    return data
                }
            }
        } catch (e: Exception) {
            println("Unexpected (recv) error: $e")
            return byteArrayOf()
        }
    }
    fun recv(delimiter: Byte): ByteArray {
        try {
            var nextByte: Int
            var data = recvAllFromIntermediateBuffer()

            val delimiterIndex = data.indexOf(delimiter)

            if (delimiterIndex != -1) {
                intermediateBuffer += data.sliceArray(IntRange(delimiterIndex + 1, data.size - 1))
                return data.sliceArray(IntRange(0, delimiterIndex))
            } else {
                // Read bytes from the buffer until the delimiter is encountered
                while (true) {
                    nextByte = socket.getInputStream().read()
                    if (nextByte == -1 || nextByte == delimiter.toInt()) {
                        break
                    }
                    data = data.plus(nextByte.toByte())
                }
                return data
            }
        } catch (e: Exception) {
            println("Unexpected (recv) error: $e")
            return byteArrayOf()
        }
    }


    private fun recvFromIntermediateBuffer(numBytes: Int): ByteArray {
        val data = intermediateBuffer.sliceArray(IntRange(0, numBytes - 1))
        intermediateBuffer = intermediateBuffer.sliceArray(IntRange(numBytes, intermediateBuffer.size - 1))
        return data
    }

    private fun recvAllFromIntermediateBuffer(): ByteArray {
        val data = intermediateBuffer
        intermediateBuffer = byteArrayOf()
        return data
    }
}