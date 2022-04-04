package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;


@Mapper(componentModel = "spring", uses = {DisqualificationMapper.class, 
        PermissionToActMapper.class})
public interface InternalCorporateDisqualificationMapper {

    @Mapping(target = "externalData.officerDisqId", source = "officerDisqId")
    @Mapping(target = "externalData.companyNumber", source = "registeredNumber")
    @Mapping(target = "externalData.officerDetailId", source = "officerDetailId")
    @Mapping(target = "externalData.countryOfRegistration", source = "registeredLocation")
    @Mapping(target = "externalData.name", source = "surname")
    @Mapping(target = "externalData.disqualifications", source = "disqualifications")
    @Mapping(target = "externalData.permissionsToAct", source = "exemptions")
    InternalCorporateDisqualificationApi disqualificationDeltaToApi(
            DisqualificationOfficer disqualificationOfficer);

}
