package com.ft.methodecontentcollectionmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MethodeContentCollectionMapperConfiguration extends Configuration
    implements ConfigWithAppInfo {
  private final ConsumerConfiguration consumerConfiguration;
  private final ProducerConfiguration producerConfiguration;
  private final UppServiceConfiguration documentStoreApiConfiguration;
  private final ValidationConfiguration validationConfiguration;
  private final String contentUriPrefix;

  @JsonProperty private AppInfo appInfo = new AppInfo();

  private MethodeContentCollectionMapperConfiguration(
      @NotNull @JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
      @NotNull @JsonProperty("producer") ProducerConfiguration producerConfiguration,
      @NotNull @JsonProperty("contentUriPrefix") String contentUriPrefix,
      @NotNull @JsonProperty("documentStoreApi")
          UppServiceConfiguration documentStoreApiConfiguration,
      @NotNull @JsonProperty("validationConfiguration")
          ValidationConfiguration validationConfiguration) {
    this.consumerConfiguration = consumerConfiguration;
    this.producerConfiguration = producerConfiguration;
    this.contentUriPrefix = contentUriPrefix;
    this.documentStoreApiConfiguration = documentStoreApiConfiguration;
    this.validationConfiguration = validationConfiguration;
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
  public UppServiceConfiguration getDocumentStoreApiConfiguration() {
    return documentStoreApiConfiguration;
  }

  @Valid
  public ValidationConfiguration getValidationConfiguration() {
    return validationConfiguration;
  }

  @Valid
  public String getContentUriPrefix() {
    return contentUriPrefix;
  }

  @Override
  public AppInfo getAppInfo() {
    return appInfo;
  }
}
