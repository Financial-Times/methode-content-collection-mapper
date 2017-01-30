package com.ft.methodestorypackagemapper.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class MethodeStoryPackageMapperConfiguration extends Configuration {
    private final ConsumerConfiguration consumerConfiguration;
    private final ProducerConfiguration producerConfiguration;
    private final String contentUriPrefix;

    private MethodeStoryPackageMapperConfiguration(
            @NotNull @JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
            @NotNull @JsonProperty("producer") ProducerConfiguration producerConfiguration,
            @NotNull @JsonProperty("contentUriPrefix") String contentUriPrefix) {
        this.consumerConfiguration = consumerConfiguration;
        this.producerConfiguration = producerConfiguration;
        this.contentUriPrefix = contentUriPrefix;
    }

    @Valid
    public ConsumerConfiguration getConsumerConfiguration() {
        return consumerConfiguration;
    }

    @Valid
    public ProducerConfiguration getProducerConfiguration() {
        return producerConfiguration;
    }

    @Valid
    public String getContentUriPrefix() {
        return contentUriPrefix;
    }
}
