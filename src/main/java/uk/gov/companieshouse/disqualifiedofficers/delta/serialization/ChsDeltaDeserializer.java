package uk.gov.companieshouse.disqualifiedofficers.delta.serialization;

import java.util.Arrays;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;

@Component
public class ChsDeltaDeserializer implements Deserializer<ChsDelta> {
    
    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader = new ReflectDatumReader<>(ChsDelta.class);
            return reader.read(null, decoder);
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Message data [" + Arrays.toString(data) + "] from topic [" + topic + "] "
                            + "cannot be deserialized", ex);
        }
    }
}