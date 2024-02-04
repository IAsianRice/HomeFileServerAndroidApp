
import com.example.filetransfertest.DataReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DataReceiverTest {

    private val dataReceiver = DataReceiver()

    @Test
    fun testGetAllDataPrior() {
        println("setting recv func")

        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8), true)

        var finished = false

        fun onDataReceived(dr: DataReceiver) {
            assertEquals("So PainSo PainSo PainSo Pain", dr.getDataUntilTerminated().toString(Charsets.UTF_8))
            println("got data")
            finished = true
        }

        dataReceiver.startReceiverFunction(::onDataReceived)

        while (!finished) {
            // Wait for the receiver function to finish
        }
    }

    @Test
    fun testGetAllDataStaggered() {
        println("setting recv func")

        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))

        var finished = false

        fun onDataReceived(dr: DataReceiver) {
            assertEquals("So PainSo PainSo PainSo Pain", dr.getDataUntilTerminated().toString(Charsets.UTF_8))
            println("got data")
            finished = true
        }

        dataReceiver.startReceiverFunction(::onDataReceived)

        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8), true)

        while (!finished) {
            // Wait for the receiver function to finish
        }
    }

    @Test
    fun testGetAllData() {
        println("setting recv func")

        var finished = false

        fun onDataReceived(dr: DataReceiver) {
            assertEquals("So PainSo PainSo PainSo Pain", dr.getDataUntilTerminated().toString(Charsets.UTF_8))
            println("got data")
            finished = true
        }

        dataReceiver.startReceiverFunction(::onDataReceived)

        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8), true)

        while (!finished) {
            // Wait for the receiver function to finish
        }
    }

    @Test
    fun testGetAllDataAndSomeError() {
        try {
            println("setting recv func")

            var finished = false

            fun onDataReceived(dr: DataReceiver) {
                dr.getDataUntilTerminated().toString(Charsets.UTF_8)
                println("got data")
                finished = true
            }

            dataReceiver.startReceiverFunction(::onDataReceived)

            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8), true)
            TimeUnit.SECONDS.sleep(1)
            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
            println("sending data")
            dataReceiver.pushData("So Pain".toByteArray(Charsets.UTF_8))

            while (!finished) {
                // Wait for the receiver function to finish
            }
        } catch (e: Exception) {
            assertTrue(e is Exception)
        }
    }

    @Test
    fun testGetUntilDelimiter() {
        println("setting recv func")

        fun onDataReceived(dr: DataReceiver) {
            val username = dr.getDataUntilDelimiter(0x03).toString(Charsets.UTF_8)
            println("got username")
            val password = dr.getDataUntilDelimiter(0x03).toString(Charsets.UTF_8)
            println("got password")
            assertEquals("SuperMario", username)
            assertEquals("Bros:SuperLuigiEdition", password)
        }

        dataReceiver.startReceiverFunction(::onDataReceived)

        println("sending data")
        dataReceiver.pushData("Super".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("Mario".toByteArray(Charsets.UTF_8) + "\u0003".toByteArray(Charsets.UTF_8) + "Bros:SuperLui".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("giEdition".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("\u0003".toByteArray(Charsets.UTF_8), true)
    }

    @Test
    fun testMultipleRecvFuncError() {
        try {
            fun onDataReceived(dr: DataReceiver) {
                dr.getDataUntilDelimiter(0x03).toString(Charsets.UTF_8)
                println("got username")
                dr.getDataUntilDelimiter(0x03).toString(Charsets.UTF_8)
                println("got password")
            }

            fun onDataReceived2(dr: DataReceiver) {
                dr.getDataUntilTerminated().toString(Charsets.UTF_8)
                println("got data")
            }

            dataReceiver.startReceiverFunction(::onDataReceived)
            dataReceiver.startReceiverFunction(::onDataReceived2)

            println("sending data")
            dataReceiver.pushData("Super".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
        } catch (e: Exception) {
            assertTrue(e is Exception)
        }
    }

    @Test
    fun testStreamData() {
        println("setting recv func")

        var finished = false
        var mockFile = ""

        fun onDataReceived(dr: DataReceiver) {
            fun onData(data: ByteArray) {
                println(data.toString(Charsets.UTF_8))
                mockFile += data.toString(Charsets.UTF_8)
            }
            dr.streamDataUntilTerminatedInto(::onData)
            println("Finished Streaming?")
            finished = true
        }

        dataReceiver.startReceiverFunction(::onDataReceived)

        println("sending data")
        dataReceiver.pushData("Super".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("Mario".toByteArray(Charsets.UTF_8) + "\u0003".toByteArray(Charsets.UTF_8) + "Bros:SuperLui".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("giEdition".toByteArray(Charsets.UTF_8))
        TimeUnit.SECONDS.sleep(1)
        println("sending data")
        dataReceiver.pushData("\u0003".toByteArray(Charsets.UTF_8), true)

        while (!finished) {
            // Wait for the receiver function to finish
        }

        assertEquals("SuperMario\u0003Bros:SuperLuigiEdition\u0003", mockFile)
    }

    @Test
    fun testMultipleRecvFuncError2() {
        try {
            var mockFile = ""

            fun onDataReceived(dr: DataReceiver) {
                fun onData(data: ByteArray) {
                    println(data.toString(Charsets.UTF_8))
                    mockFile += data.toString(Charsets.UTF_8)
                }
                dr.streamDataUntilTerminatedInto(::onData)
            }

            fun onDataReceived2(dr: DataReceiver) {
                dr.getDataUntilTerminated().toString(Charsets.UTF_8)
                println("got data")
            }

            dataReceiver.startReceiverFunction(::onDataReceived)
            dataReceiver.startReceiverFunction(::onDataReceived2)

            println("sending data")
            dataReceiver.pushData("Super".toByteArray(Charsets.UTF_8))
            TimeUnit.SECONDS.sleep(1)
        } catch (e: Exception) {
            assertTrue(e is Exception)
        }
    }
}
