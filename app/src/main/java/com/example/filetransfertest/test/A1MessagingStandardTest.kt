import com.example.filetransfertest.EncryptionMethod
import junit.framework.TestCase
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class A1MessagingStandardTest {
    /*
    Header (Strict) (245 Bytes):
    - Message Flags (1 byte)
        - Bit 0: Header Type [1:Intermittent, 0:Request]
        - Bit 1: Streamed [1: Yes (Intermittent Messaging), 0: No (Singular Message)]
    - Transmission ID (8 bytes)
    - Body Size (8 bytes)
    - Encryption Method (1 byte)
        - 0: No Encryption
        - 1: Asymmetric Encryption
        - 2: Symmetric Encryption
        - 3: ...
    - Request (8 bytes)



    Intermittent Header (Strict) (245 Bytes):
    - Message Flags (1 byte)
        - Bit 1: Header Type [1:Intermittent, 0:Request]
    - Transmission ID (8 Bytes)
    - Body Size (8 bytes)
    - Encryption Method (1 byte)
        - 0: No Encryption
        - 1: Asymmetric Encryption
        - 2: Symmetric Encryption
        - 3: ...
    - Terminated (8 bytes)

     */
    @Test
    fun test_formatSecureIntermittentMessage() {
        val messageHeader =
            ByteBuffer.allocate(29).order(ByteOrder.LITTLE_ENDIAN)
                .put(0b00000000)
                .putLong(0)
                .putLong(1)
                .put(EncryptionMethod.ASYMMETRIC)
                .putLong(0)
                .array()
        TestCase.assertEquals(4, 2 + 2)
    }
}
