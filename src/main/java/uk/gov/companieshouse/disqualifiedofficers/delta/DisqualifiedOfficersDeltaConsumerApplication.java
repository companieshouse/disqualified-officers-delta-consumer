package uk.gov.companieshouse.disqualifiedofficers.delta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DisqualifiedOfficersDeltaConsumerApplication {

    public static final String NAMESPACE = "disqualified-officers-delta-consumer";

    public static void main(String[] args) {
        SpringApplication.run(DisqualifiedOfficersDeltaConsumerApplication.class, args);
    }

}
