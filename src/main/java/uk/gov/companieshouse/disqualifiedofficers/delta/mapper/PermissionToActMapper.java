package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import uk.gov.companieshouse.api.delta.Exemption;
import uk.gov.companieshouse.api.disqualification.PermissionToAct;


@Mapper(componentModel = "spring")
public interface PermissionToActMapper {

    @Mapping(target = "companyNames", source = "companyNames")
    @Mapping(target = "courtName", source = "courtName")
    @Mapping(target = "expiresOn", source = "expiresOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "grantedOn", source = "grantedOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "purpose", source = "purpose")
    PermissionToAct disqualificationDeltaToApi(
            Exemption exemptions);

}
