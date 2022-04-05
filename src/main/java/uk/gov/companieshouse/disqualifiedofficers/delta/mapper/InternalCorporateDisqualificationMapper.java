package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;


@Mapper(componentModel = "spring", uses = {DisqualificationMapper.class, 
        PermissionToActMapper.class})
public interface InternalCorporateDisqualificationMapper {

    @Mapping(target = "internalData.officerDisqId", source = "officerDisqId")
    @Mapping(target = "internalData.officerDetailId", source = "officerDetailId")
    @Mapping(target = "internalData.officerIdRaw", source = "officerId")
    @Mapping(target = "externalData.companyNumber", source = "registeredNumber")
    @Mapping(target = "externalData.personNumber", source = "externalNumber")
    @Mapping(target = "externalData.countryOfRegistration", source = "registeredLocation")
    @Mapping(target = "externalData.name", source = "surname")
    @Mapping(target = "externalData.disqualifications", source = "disqualifications")
    @Mapping(target = "externalData.permissionsToAct", source = "exemptions")
    InternalCorporateDisqualificationApi disqualificationDeltaToApi(
            DisqualificationOfficer disqualificationOfficer);

    /**
    * Invoked at the end of the auto-generated mapping methods.
    * @param target        the target object
    * @param sourceCase    the source object
    */
    @AfterMapping
    default void mapLinksAndId(@MappingTarget InternalCorporateDisqualificationApi target,
                                  DisqualificationOfficer sourceCase) {
        
        String encodedOfficerId = MapperUtils.encode(sourceCase.getOfficerId());
        String link = String.format("/disqualifiedofficer/corporate/%s", encodedOfficerId);
        CorporateDisqualificationApi externalTarget = target.getExternalData();
        InternalDisqualificationApiInternalData internalData = target.getInternalData();
        externalTarget.setLinks(link);
        internalData.setOfficerId(encodedOfficerId);
    }

}
