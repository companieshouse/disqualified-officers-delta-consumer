package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import java.util.HashMap;
import java.util.Map;

public enum DisqualificationType {

    CORPORATE("corporate"),
    NATURAL("natural");

    private static final Map<String, DisqualificationType> BY_OFFICER_TYPE = new HashMap<>();

    static {
        BY_OFFICER_TYPE.put("1", CORPORATE);
    }

    private final String type;

    DisqualificationType(String type) {
        this.type = type;
    }

    public static DisqualificationType getTypeFromCorporateInd(final String corporateInd) {
        return BY_OFFICER_TYPE.getOrDefault(corporateInd, NATURAL);
    }

    public String getTypeAsString() {
        return type;
    }
}
