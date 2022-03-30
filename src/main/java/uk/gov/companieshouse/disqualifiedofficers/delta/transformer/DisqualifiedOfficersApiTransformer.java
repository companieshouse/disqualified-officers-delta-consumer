package uk.gov.companieshouse.disqualifiedofficers.delta.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;

@Component
public class DisqualifiedOfficersApiTransformer {

    public String transform(DisqualificationDelta disqualifiedOfficersDelta) {
        // TODO: Use mapStruct to transform json object to Open API generated object
        return disqualifiedOfficersDelta.toString();
    }
}