package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;

@Mapper(componentModel = "spring", uses = {DisqualificationMapper.class, 
        PermissionToActMapper.class})
public interface InternalNaturalDisqualificationMapper {

    @Mapping(target = "externalData.dateOfBirth", source = "dateOfBirth", dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.officerDisqId", source = "officerDisqId")
    @Mapping(target = "externalData.personNumber", source = "externalNumber")
    @Mapping(target = "externalData.officerDetailId", source = "officerDetailId")
    @Mapping(target = "externalData.forename", source = "forename")
    @Mapping(target = "externalData.honours", source = "honours")
    @Mapping(target = "externalData.nationality", source = "nationality")
    @Mapping(target = "externalData.surname", source = "surname")
    @Mapping(target = "externalData.title", source = "title")
    @Mapping(target = "externalData.otherForenames", source = "middleName")
    @Mapping(target = "externalData.disqualifications", source = "disqualifications")
    @Mapping(target = "externalData.permissionsToAct", source = "exemptions")
    InternalNaturalDisqualificationApi disqualificationDeltaToApi(
            DisqualificationOfficer disqualificationOfficer);

}