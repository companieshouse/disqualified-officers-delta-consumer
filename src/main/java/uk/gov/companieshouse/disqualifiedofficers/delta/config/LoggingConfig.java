package uk.gov.companieshouse.disqualifiedofficers.delta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
public class LoggingConfig {

    @Value("${logger.namespace}")
    private String loggerNamespace;

    private static Logger staticLogger;

    /**
     * Main application logger with component specific namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        Logger loggerBean = LoggerFactory.getLogger(loggerNamespace);
        staticLogger = loggerBean;
        return loggerBean;
    }


    public static Logger getLogger() {
        return staticLogger;
    }
}