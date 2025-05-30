package uk.gov.companieshouse.disqualifiedofficers.delta.serialization;

import static uk.gov.companieshouse.disqualifiedofficers.delta.DisqualifiedOfficersDeltaConsumerApplication.NAMESPACE;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.logging.DataMapHolder;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.nio.charset.StandardCharsets;

@Component
public class ChsDeltaSerializer implements Serializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public byte[] serialize(String topic, Object payload) {
        LOGGER.trace("Payload serialised: " + payload, DataMapHolder.getLogMap());

        try {
            if (payload == null) {
                return null;
            }

            if (payload instanceof byte[]) {
                return (byte[]) payload;
            }

            if (payload instanceof ChsDelta chsDelta) {
                DatumWriter<ChsDelta> writer = new SpecificDatumWriter<>();
                EncoderFactory encoderFactory = EncoderFactory.get();

                AvroSerializer<ChsDelta> avroSerializer =
                        new AvroSerializer<>(writer, encoderFactory);

                return avroSerializer.toBinary(chsDelta);
            }

            return payload.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.error("Serialization exception while writing to byte array", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(ex);
        }
    }
}