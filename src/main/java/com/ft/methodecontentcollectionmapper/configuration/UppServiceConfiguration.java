package com.ft.methodecontentcollectionmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;

import javax.validation.Valid;

public class UppServiceConfiguration {
    private final EndpointConfiguration endpointConfiguration;
    private final int numberOfConnectionAttempts;
    private final int timeoutMultiplier;
    private final String hostHeader;

    public UppServiceConfiguration(@JsonProperty("endpointConfiguration") final EndpointConfiguration endpointConfiguration,
                                   @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts,
                                   @JsonProperty("timeoutMultiplier") int timeoutMultiplier,
                                   @JsonProperty("hostHeader") String hostHeader) {
        this.endpointConfiguration = endpointConfiguration;
        this.numberOfConnectionAttempts = numberOfConnectionAttempts;
        this.timeoutMultiplier = timeoutMultiplier;
        this.hostHeader = hostHeader;
    }

    @Valid
    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    public int getNumberOfConnectionAttempts() { return numberOfConnectionAttempts; }

    public int getTimeoutMultiplier() { return timeoutMultiplier; }

    public String getHostHeader() {
        return hostHeader;
    }
}
