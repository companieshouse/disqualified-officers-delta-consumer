package uk.gov.companieshouse.disqualifiedofficers.delta.serialization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ChsDeltaSerializerTest {

    private ChsDeltaSerializer serializer = new ChsDeltaSerializer();

    @Test
    void When_deserialize_Expect_ValidChsDeltaObject() {
        ChsDelta chsDelta = new ChsDelta("data", 1, "context_id");
        byte[] data = { 0x08, 0x64, 0x61, 0x74, 0x61, 0x02, 0x14, 0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74, 0x5F, 0x69, 0x64 };

        byte[] serializedObject = serializer.serialize("", chsDelta);

        assertThat(serializedObject).isEqualTo(data);
    }

}