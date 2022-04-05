package uk.gov.companieshouse.disqualifiedofficers.delta.transformer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.InternalCorporateDisqualificationMapper;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.InternalNaturalDisqualificationMapper;

@Component
public class DisqualifiedOfficersApiTransformer {

    private final InternalCorporateDisqualificationMapper corporateMapper;
    private final InternalNaturalDisqualificationMapper naturalMapper;

    /**
     * Constructor for the transformer.
     * @param corporateMapper returns the corporate disqualification api object.
     * @param naturalMapper returns the natural disqualification api object.
     */
    @Autowired
    public DisqualifiedOfficersApiTransformer(
            InternalCorporateDisqualificationMapper corporateMapper, 
            InternalNaturalDisqualificationMapper naturalMapper
    ) {
        this.naturalMapper = naturalMapper;
        this.corporateMapper = corporateMapper;
    }

    /**
    * Maps the natural disqualification delta to an api object.
    * @param disqualificationDelta the CHIPS delta
    * @return the InternalNaturalDisqualificationApi object
    */
    public InternalNaturalDisqualificationApi transformNaturalDisqualification(
            DisqualificationDelta disqualificationDelta) {
        DisqualificationOfficer officer = disqualificationDelta.getDisqualifiedOfficer().get(0);
        InternalNaturalDisqualificationApi apiObject = naturalMapper
                        .disqualificationDeltaToApi(officer);
        return parseNaturalDeltaAt(apiObject, disqualificationDelta);
    }

    /**
    * Maps the corporate disqualification delta to an api object.
    * @param disqualificationDelta the CHIPS delta
    * @return the InternalCorporateDisqualificationApi object
    */
    public InternalCorporateDisqualificationApi transformCorporateDisqualification(
            DisqualificationDelta disqualificationDelta) {
        DisqualificationOfficer officer = disqualificationDelta.getDisqualifiedOfficer().get(0);
        InternalCorporateDisqualificationApi abiObject = corporateMapper
                        .disqualificationDeltaToApi(officer);
        return parseCorporateDeltaAt(abiObject, disqualificationDelta);
    }

    /**
     * Maps the delta at which is not passed into the mapper.
     * @param apiObject the InternalNaturalDisqualificationApi object from the mapper.
     * @param disqualificationDelta the delta from CHIPS containing the delta_at
     * @return the original api object with the delta_at parsed.
     */
    private InternalNaturalDisqualificationApi parseNaturalDeltaAt(
            InternalNaturalDisqualificationApi apiObject,
            DisqualificationDelta disqualificationDelta) {
        InternalDisqualificationApiInternalData internalData = apiObject.getInternalData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                .withZone(ZoneId.of("Z"));
        ZonedDateTime datetime = ZonedDateTime.parse(disqualificationDelta.getDeltaAt(), formatter);
        internalData.setDeltaAt(datetime.toOffsetDateTime());
        
        apiObject.setInternalData(internalData);
        return apiObject;
    }

    /**
    * Maps the delta at which is not passed into the mapper.
    * @param apiObject the InternalCorporateDisqualificationApi object from the mapper.
    * @param disqualificationDelta the delta from CHIPS containing the delta_at
    * @return the original api object with the delta_at parsed.
    */
    private InternalCorporateDisqualificationApi parseCorporateDeltaAt(
            InternalCorporateDisqualificationApi apiObject,
            DisqualificationDelta disqualificationDelta) {
        InternalDisqualificationApiInternalData internalData = apiObject.getInternalData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                .withZone(ZoneId.of("Z"));
        ZonedDateTime datetime = ZonedDateTime.parse(disqualificationDelta.getDeltaAt(), formatter);
        internalData.setDeltaAt(datetime.toOffsetDateTime());
        
        apiObject.setInternalData(internalData);
        return apiObject;
    }
}