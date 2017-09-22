package com.ft.methodecontentcollectionmapper.client;

import com.ft.jerseyhttpwrapper.ResilientClientBuilder;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.jerseyhttpwrapper.continuation.ExponentialBackoffContinuationPolicy;
import com.ft.methodecontentcollectionmapper.configuration.UppServiceConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.setup.Environment;

import java.util.function.Function;

public abstract class UppServiceClient {

    protected final Client jerseyClient;
    protected final String apiHost;
    protected final int apiPort;
    protected final String apiPath;
    protected final String hostHeader;

    public UppServiceClient(final UppServiceConfiguration uppServiceConfiguration, final Environment environment){
        EndpointConfiguration endpointConfiguration = uppServiceConfiguration.getEndpointConfiguration();
        jerseyClient = ResilientClientBuilder.in(environment).using(endpointConfiguration).withContinuationPolicy(
                new ExponentialBackoffContinuationPolicy(
                        uppServiceConfiguration.getNumberOfConnectionAttempts(),
                        uppServiceConfiguration.getTimeoutMultiplier()
                )
        ).build();
        this.apiHost = endpointConfiguration.getHost();
        this.apiPort = endpointConfiguration.getPort();
        this.apiPath = endpointConfiguration.getPath();
        this.hostHeader = uppServiceConfiguration.getHostHeader();
    }

    public UppServiceClient(Client jerseyClient,
                            String apiHost,
                            int apiPort,
                            String apiPath,
                            String hostHeader) {
        this.jerseyClient = jerseyClient;
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.apiPath = apiPath;
        this.hostHeader = hostHeader;
    }

    public Client getJerseyClient() {
        return jerseyClient;
    }

    public String getApiHost() {
        return apiHost;
    }

    public int getApiPort() {
        return apiPort;
    }

    public String getHostHeader() {
        return hostHeader;
    }

    protected <R> R processResponse(final ClientResponse response, final Function<ClientResponse, R> processor) {
        try {
            return processor.apply(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
