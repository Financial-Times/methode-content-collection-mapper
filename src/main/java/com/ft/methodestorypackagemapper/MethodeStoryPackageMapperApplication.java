package com.ft.methodestorypackagemapper;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;

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
import com.ft.methodestorypackagemapper.configuration.ConsumerConfiguration;
import com.ft.methodestorypackagemapper.configuration.MethodeStoryPackageMapperConfiguration;
import com.ft.methodestorypackagemapper.configuration.ProducerConfiguration;
import com.ft.methodestorypackagemapper.health.CanConnectToMessageQueueProducerProxyHealthcheck;
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.messaging.MessageBuilder;
import com.ft.methodestorypackagemapper.messaging.MessageProducingStoryPackageMapper;
import com.ft.methodestorypackagemapper.messaging.NativeCmsPublicationEventsListener;
import com.ft.methodestorypackagemapper.resources.IngestResource;
import com.ft.methodestorypackagemapper.resources.MapResource;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.sun.jersey.api.client.Client;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MethodeStoryPackageMapperApplication extends Application<MethodeStoryPackageMapperConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeStoryPackageMapperApplication.class);

    public static void main(final String[] args) throws Exception {
        new MethodeStoryPackageMapperApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeStoryPackageMapperConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final MethodeStoryPackageMapperConfiguration configuration, final Environment environment) throws Exception {
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/map", "/ingest");

        environment.jersey().register(new VersionResource());
        environment.jersey().register(new BuildInfoResource());

        EomFileStoryPackageMapper eomStoryPackageMapper = new EomFileStoryPackageMapper();
        ConsumerConfiguration consumerConfig = configuration.getConsumerConfiguration();

        MessageProducingStoryPackageMapper msgProducingStoryPackageMapper = new MessageProducingStoryPackageMapper(
                getMessageBuilder(configuration, environment),
                configureMessageProducer(configuration.getProducerConfiguration(), environment), eomStoryPackageMapper);

        MessageListener listener = new NativeCmsPublicationEventsListener(msgProducingStoryPackageMapper,
                environment.getObjectMapper(), consumerConfig.getSystemCode());

        registerListener(environment, listener, consumerConfig, getConsumerClient(environment, consumerConfig));

        environment.jersey().register(new MapResource(eomStoryPackageMapper));
        environment.jersey().register(new IngestResource(msgProducingStoryPackageMapper));
        environment.jersey().register(new RuntimeExceptionMapper());

        LOGGER.info("running with configuration: {}", configuration);
    }

    private MessageBuilder getMessageBuilder(MethodeStoryPackageMapperConfiguration configuration, Environment environment) {
        return new MessageBuilder(UriBuilder.fromUri(configuration.getContentUriPrefix()).path("{uuid}"),
                configuration.getConsumerConfiguration().getSystemCode(), environment.getObjectMapper());
    }

    private Client getConsumerClient(Environment environment, ConsumerConfiguration config) {
        JerseyClientConfiguration jerseyConfig = config.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        return ResilientClientBuilder.in(environment).using(jerseyConfig).usingDNS().named("consumer-client").build();
    }

    private MessageProducer configureMessageProducer(ProducerConfiguration producerConfiguration, Environment environment) {
        JerseyClientConfiguration jerseyConfig = producerConfiguration.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        Client producerClient = ResilientClientBuilder.in(environment).using(jerseyConfig).usingDNS()
                .named("producer-client").build();

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
}
