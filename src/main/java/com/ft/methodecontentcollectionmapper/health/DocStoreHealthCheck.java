package com.ft.methodecontentcollectionmapper.health;

import com.ft.message.consumer.config.HealthcheckConfiguration;
import com.ft.methodecontentcollectionmapper.configuration.UppServiceConfiguration;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class DocStoreHealthCheck extends AdvancedHealthCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocStoreHealthCheck.class);
    private static final String HTTP_ERROR_MESSAGE = "HTTP connection error: ";
    private static final String HOST_HEADER = "Host";

    private final Client client;
    private final URI contentReadHeathCheckUri;
    private final String hostHeaderValue;
    private final HealthcheckConfiguration healthcheckConfiguration;

    public DocStoreHealthCheck(HealthcheckConfiguration healthcheckConfiguration,
                               UppServiceConfiguration contentReadConfiguration,
                               Client client) {
        super(healthcheckConfiguration.getName());
        this.healthcheckConfiguration = healthcheckConfiguration;
        final String host = contentReadConfiguration.getEndpointConfiguration().getHost();
        final int port = contentReadConfiguration.getEndpointConfiguration().getPort();
        contentReadHeathCheckUri = UriBuilder.fromPath("/__gtg").scheme("http").host(host).port(port).build();
        hostHeaderValue = contentReadConfiguration.getHostHeader();
        this.client = client;
    }

    @Override
    protected AdvancedResult checkAdvanced() {
        ClientResponse clientResponse = null;
        try {
            WebResource.Builder webResourceBuilder = client.resource(contentReadHeathCheckUri).getRequestBuilder();
            if (hostHeaderValue != null && !hostHeaderValue.isEmpty()) {
                webResourceBuilder.header(HOST_HEADER, hostHeaderValue);
            }
            webResourceBuilder.accept(MediaType.APPLICATION_JSON_TYPE);
            clientResponse = webResourceBuilder.get(ClientResponse.class);
            LOGGER.info("Calling health check: {} {}",contentReadHeathCheckUri,hostHeaderValue);
            if (clientResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                LOGGER.warn(HTTP_ERROR_MESSAGE + clientResponse.getStatus());
                return AdvancedResult.error(this, HTTP_ERROR_MESSAGE + clientResponse.getStatus());
            }
            return AdvancedResult.healthy("OK");
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return AdvancedResult.error(this, e.getMessage());
        } finally {
            if(clientResponse != null){
                clientResponse.close();
            }
        }

    }

    @Override
    protected int severity() {
        return healthcheckConfiguration.getSeverity();
    }

    @Override
    protected String businessImpact() {
        return healthcheckConfiguration.getBusinessImpact();
    }

    @Override
    protected String technicalSummary() {
        return healthcheckConfiguration.getTechnicalSummary();
    }

    @Override
    protected String panicGuideUrl() {
        return healthcheckConfiguration.getPanicGuideUrl();
    }

}

