package com.ft.methodecontentcollectionmapper;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import com.ft.methodecontentcollectionmapper.client.DocumentStoreApiClient;
import com.ft.methodecontentcollectionmapper.configuration.UppServiceConfiguration;
import com.ft.methodecontentcollectionmapper.health.DocStoreHealthCheck;
import com.ft.methodecontentcollectionmapper.mapping.BlogUuidResolver;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.jerseyhttpwrapper.ResilientClientBuilder;
import com.ft.message.consumer.MessageListener;
import com.ft.message.consumer.MessageQueueConsumerInitializer;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.QueueProxyProducer;
import com.ft.methodecontentcollectionmapper.configuration.ConsumerConfiguration;
import com.ft.methodecontentcollectionmapper.configuration.MethodeContentCollectionMapperConfiguration;
import com.ft.methodecontentcollectionmapper.configuration.ProducerConfiguration;
import com.ft.methodecontentcollectionmapper.health.CanConnectToMessageQueueProducerProxyHealthcheck;
import com.ft.methodecontentcollectionmapper.mapping.EomFileToContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.messaging.MessageBuilder;
import com.ft.methodecontentcollectionmapper.messaging.MessageProducingContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.messaging.NativeCmsPublicationEventsListener;
import com.ft.methodecontentcollectionmapper.resources.MethodeContentCollectionResource;
import com.ft.methodecontentcollectionmapper.validation.ContentCollectionValidator;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.platform.dropwizard.DefaultGoodToGoChecker;
import com.ft.platform.dropwizard.GoodToGoBundle;
import com.sun.jersey.api.client.Client;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MethodeContentCollectionMapperApplication extends Application<MethodeContentCollectionMapperConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeContentCollectionMapperApplication.class);

    public static void main(final String[] args) throws Exception {
        new MethodeContentCollectionMapperApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeContentCollectionMapperConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
        bootstrap.addBundle(new GoodToGoBundle(new DefaultGoodToGoChecker()));
    }

    @Override
    public void run(final MethodeContentCollectionMapperConfiguration configuration, final Environment environment) throws Exception {
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/map", "/ingest");

        environment.jersey().register(new VersionResource());
        environment.jersey().register(new BuildInfoResource());

        Client docStoreClient = createDocumentStoreClient(environment, configuration.getDocumentStoreApiConfiguration());
        DocumentStoreApiClient documentStoreApiClient = new DocumentStoreApiClient(
                docStoreClient,
                configuration.getDocumentStoreApiConfiguration().getEndpointConfiguration().getHost(),
                configuration.getDocumentStoreApiConfiguration().getEndpointConfiguration().getPort(),
                configuration.getDocumentStoreApiConfiguration().getHostHeader()
                );
        BlogUuidResolver blogUuidResolver = new BlogUuidResolver(
                environment.metrics(),
                documentStoreApiClient,
                configuration.getValidationConfiguration().getAuthorityPrefix(),
                configuration.getValidationConfiguration().getBrandIdMappings()
        );
        registerDocStoreHealthCheck(environment, configuration.getDocumentStoreApiConfiguration(), docStoreClient);

        EomFileToContentCollectionMapper eomContentCollectionMapper = new EomFileToContentCollectionMapper(blogUuidResolver);
        ConsumerConfiguration consumerConfig = configuration.getConsumerConfiguration();
        ContentCollectionValidator contentCollectionValidator = new ContentCollectionValidator();

        MessageProducingContentCollectionMapper msgProducingContentCollectionMapper = new MessageProducingContentCollectionMapper(
                getMessageBuilder(configuration, environment),
                configureMessageProducer(configuration.getProducerConfiguration(), environment), eomContentCollectionMapper);

        MessageListener listener = new NativeCmsPublicationEventsListener(msgProducingContentCollectionMapper,
                environment.getObjectMapper(), contentCollectionValidator, consumerConfig.getSystemCode());

        registerListener(environment, listener, consumerConfig, getConsumerClient(environment, consumerConfig));

        environment.jersey().register(new MethodeContentCollectionResource(msgProducingContentCollectionMapper,
                eomContentCollectionMapper, contentCollectionValidator));
        environment.jersey().register(new RuntimeExceptionMapper());

        LOGGER.info("running with configuration: {}", configuration);
    }

    private MessageBuilder getMessageBuilder(MethodeContentCollectionMapperConfiguration configuration, Environment environment) {
        return new MessageBuilder(configuration.getContentUriPrefix(),
                configuration.getConsumerConfiguration().getSystemCode(), environment.getObjectMapper());
    }

    private Client getConsumerClient(Environment environment, ConsumerConfiguration config) {
        JerseyClientConfiguration jerseyConfig = config.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        return ResilientClientBuilder.in(environment).using(jerseyConfig).usingDNS()
                .named("content-collection-consumer-client").build();
    }

    private Client createDocumentStoreClient(Environment environment, UppServiceConfiguration config) {
        JerseyClientConfiguration jerseyConfig = config.getEndpointConfiguration().getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);
        return ResilientClientBuilder.in(environment).using(jerseyConfig).usingDNS()
                .named("content-collection-document-store-api-client").build();
    }

    private MessageProducer configureMessageProducer(ProducerConfiguration producerConfiguration, Environment environment) {
        JerseyClientConfiguration jerseyConfig = producerConfiguration.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        Client producerClient = ResilientClientBuilder.in(environment).using(jerseyConfig).usingDNS()
                .named("content-collection-producer-client").build();

        final QueueProxyProducer.BuildNeeded queueProxyBuilder = QueueProxyProducer.builder()
                .withJerseyClient(producerClient)
                .withQueueProxyConfiguration(producerConfiguration.getMessageQueueProducerConfiguration());

        final QueueProxyProducer producer = queueProxyBuilder.build();

        registerProducerHealthCheck(environment, producerConfiguration, queueProxyBuilder);

        return producer;
    }

    private void registerProducerHealthCheck(Environment environment, ProducerConfiguration config, QueueProxyProducer.BuildNeeded queueProxyBuilder) {
        environment.healthChecks().register("KafkaProxyProducer", new CanConnectToMessageQueueProducerProxyHealthcheck(
                queueProxyBuilder.buildHealthcheck(), config.getHealthcheckConfiguration(), environment.metrics()));
    }

    protected void registerListener(Environment environment, MessageListener listener, ConsumerConfiguration config, Client consumerClient) {
        final MessageQueueConsumerInitializer messageQueueConsumerInitializer = new MessageQueueConsumerInitializer(
                config.getMessageQueueConsumerConfiguration(), listener, consumerClient);
        environment.lifecycle().manage(messageQueueConsumerInitializer);

        registerConsumerHealthCheck(environment, config, messageQueueConsumerInitializer);
    }

    private void registerConsumerHealthCheck(Environment environment, ConsumerConfiguration config, MessageQueueConsumerInitializer messageQueueConsumerInitializer) {
        environment.healthChecks().register("KafkaProxyConsumer", messageQueueConsumerInitializer
                .buildPassiveConsumerHealthcheck(config.getHealthcheckConfiguration(), environment.metrics()));
    }

    private void registerDocStoreHealthCheck(Environment environment, UppServiceConfiguration config, Client httpClient) {
        AdvancedHealthCheck healthCheck = new DocStoreHealthCheck(config.getHealthcheckConfiguration(), config, httpClient);
        environment.healthChecks().register(config.getHealthcheckConfiguration().getName(), healthCheck);
    }
}
