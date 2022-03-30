package uk.gov.companieshouse.disqualifiedofficers.delta.transformer;

import org.junit.Test;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;

import static org.assertj.core.api.Assertions.assertThat;

public class DisqualifiedOfficersApiTransformerTest {
    private final DisqualifiedOfficersApiTransformer transformer = new DisqualifiedOfficersApiTransformer();

    @Test
    public void transformSuccessfully() {
        final DisqualificationDelta input = new DisqualificationDelta();
        assertThat(transformer.transform(input)).isEqualTo(input.toString());
    }

}