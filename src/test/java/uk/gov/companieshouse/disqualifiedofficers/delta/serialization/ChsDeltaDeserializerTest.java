package uk.gov.companieshouse.disqualifiedofficers.delta.serialization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ChsDeltaDeserializerTest {

    @Mock
    private Logger logger;
    private final ChsDeltaDeserializer deserializer = new ChsDeltaDeserializer();

    @Test
    void When_deserialize_Expect_ValidChsDeltaObject() {
        ChsDelta chsDelta = new ChsDelta("data", 1, "context_id",false);
        byte[] data = encodedData(chsDelta);

        ChsDelta deserializedObject = deserializer.deserialize("", data);

        assertThat(deserializedObject).isEqualTo(chsDelta);
    }

    @Test
    void When_deserializeFails_throwsNonRetryableError() {
        byte[] data = "Invalid message".getBytes();
        assertThrows(NonRetryableErrorException.class, () -> deserializer.deserialize("", data));
    }

    private byte[] encodedData(ChsDelta chsDelta){
        ChsDeltaSerializer serializer = new ChsDeltaSerializer(this.logger);
        byte[] serialisedData = serializer.serialize("", chsDelta);
        serializer.close();
        return serialisedData;
    }
}