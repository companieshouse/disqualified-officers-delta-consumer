package uk.gov.companieshouse.disqualifiedofficers.delta.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;

@ExtendWith(MockitoExtension.class)
class ChsDeltaSerializerTest {

    private ChsDeltaSerializer serializer;

    @BeforeEach
    void init() {
        serializer = new ChsDeltaSerializer();
    }

    @Test
    void When_serialize_Expect_ValidByteArray() {
        ChsDelta chsDelta = new ChsDelta("data", 1, "context_id",false);
        byte[] data = { 0x08, 0x64, 0x61, 0x74, 0x61, 0x02, 0x14, 0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74, 0x5F, 0x69, 0x64, 0x0 };

        byte[] serializedObject = serializer.serialize("", chsDelta);

        assertThat(serializedObject).isEqualTo(data);
    }

    @Test
    void When_serialize_null_returns_null() {
        byte[] emptyArray = new byte[0];
        byte[] serialize = serializer.serialize("", null);
        assertThat(serialize).isEqualTo(emptyArray);
    }

    @Test
    void When_serialize_receivesBytes_returnsBytes() {
        byte[] byteExample = "Example bytes".getBytes();
        byte[] serialize = serializer.serialize("", byteExample);
        assertThat(serialize).isEqualTo(byteExample);
    }

    @Test
    void When_serialize_receives_blank_object_exception_thrown() {
        ChsDelta payload = new ChsDelta();
        assertThrows(NonRetryableErrorException.class, () -> serializer.serialize("",payload));
    }

}