package com.example.filetransfertest

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class DataReceiver {
    private var terminated: Boolean = false
    private var recvFuncThread: Thread? = null
    private var buffer: ByteArray = byteArrayOf()
    private val stateUpdatedSemaphore = Semaphore(1)

    private var exceptionRaised: Boolean = false
    private var exceptionReason: String = ""

    private val lock = ReentrantLock()

    fun getDataUntilTerminated(): ByteArray {
        while (!terminated) {
            stateUpdatedSemaphore.acquire()
            if (exceptionRaised) {
                throw Exception(exceptionReason)
            }
        }

        val retVal = buffer.clone()
        buffer = byteArrayOf()
        return retVal
    }

    fun getDataUntilDelimiter(delimiter: Byte): ByteArray {
        while (!buffer.contains(delimiter)) {
            stateUpdatedSemaphore.acquire()
            if (exceptionRaised) {
                throw Exception(exceptionReason)
            }
        }

        val delimiterIndex = buffer.indexOf(delimiter)
        val retVal = buffer.copyOfRange(0, delimiterIndex)
        buffer = buffer.copyOfRange(delimiterIndex + 1, buffer.size)
        return retVal
    }

    fun streamDataUntilTerminatedInto(function: (ByteArray) -> Unit) {
        while (!terminated) {
            stateUpdatedSemaphore.acquire()
            function(getData())
            if (exceptionRaised) {
                throw Exception(exceptionReason)
            }
        }
    }

    fun pushData(data: ByteArray, finished: Boolean = false) {
        lock.lock()
        buffer += data
        if (terminated) {
            terminated = true
            exceptionReason = "Adding Data to an already terminated DR is undefined behaviour"
            exceptionRaised = true
            stateUpdatedSemaphore.release()  // state changed
            lock.unlock()
            throw Exception(exceptionReason)
        }

        if (finished) {
            terminate()
        } else {
            stateUpdatedSemaphore.release()  // state changed
            lock.unlock()
        }
    }

    fun startReceiverFunction(function: (DataReceiver) -> Unit) {
        lock.lock()
        if (recvFuncThread == null) {
            recvFuncThread = thread(start = true) {
                function(this)
            }
        } else {
            terminated = true
            exceptionReason = "2 Receiver Functions ran! (Only one can be ran)"
            exceptionRaised = true
            stateUpdatedSemaphore.release()  // state changed
            lock.unlock()
            recvFuncThread?.join()
            throw Exception(exceptionReason)
        }

        stateUpdatedSemaphore.release()  // state changed
        lock.unlock()
    }

    fun terminate() {
        lock.lock()
        terminated = true
        stateUpdatedSemaphore.release()  // state changed

        lock.unlock()
        recvFuncThread?.join()
    }

    private fun getData(): ByteArray {
        val retVal = buffer.clone()
        buffer = byteArrayOf()
        return retVal
    }
}
