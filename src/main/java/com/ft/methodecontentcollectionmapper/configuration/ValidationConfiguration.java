package com.ft.methodecontentcollectionmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class ValidationConfiguration {

    private String authorityPrefix;
    private Map<String, String> brandIdMappings;

    public ValidationConfiguration(
            @NotNull @JsonProperty("authorityPrefix") String authorityPrefix,
            @NotNull @JsonProperty("brandIdMappings") Map<String, String> brandIdMappings
    ) {
        this.authorityPrefix = authorityPrefix;
        this.brandIdMappings = brandIdMappings;
    }

    @Valid
    public String getAuthorityPrefix() {
        return authorityPrefix;
    }

    @Valid
    public Map<String, String> getBrandIdMappings() {
        return brandIdMappings;
    }
}
