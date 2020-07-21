package com.ft.methodecontentcollectionmapper.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.message.consumer.config.HealthcheckConfiguration;
import com.ft.methodecontentcollectionmapper.configuration.UppServiceConfiguration;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import org.junit.Test;

public class DocStoreHealthCheckTest {

  @Test
  public void checkAdvancedOk() throws Exception {
    HealthcheckConfiguration mockHCConfiguration = mock(HealthcheckConfiguration.class);
    UppServiceConfiguration mockUppServiceConfiguration = mock(UppServiceConfiguration.class);
    Client mockedClient = mock(Client.class);
    EndpointConfiguration mockedEndpointConfiguration = mock(EndpointConfiguration.class);
    when(mockUppServiceConfiguration.getEndpointConfiguration())
        .thenReturn(mockedEndpointConfiguration);
    when(mockedEndpointConfiguration.getHost()).thenReturn("document-store-api");
    when(mockedEndpointConfiguration.getPort()).thenReturn(8080);
    WebResource mockedWebResource = mock(WebResource.class);
    when(mockedClient.resource(URI.create("http://document-store-api:8080/__gtg")))
        .thenReturn(mockedWebResource);
    WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
    when(mockedWebResource.getRequestBuilder()).thenReturn(mockedBuilder);
    when(mockedBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(mockedBuilder);
    when(mockedBuilder.header(anyString(), anyObject())).thenReturn(mockedBuilder);
    ClientResponse mockedClientResponse = mock(ClientResponse.class);
    when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedClientResponse);
    when(mockedClientResponse.getStatus()).thenReturn(200);
    DocStoreHealthCheck hc =
        new DocStoreHealthCheck(mockHCConfiguration, mockUppServiceConfiguration, mockedClient);

    AdvancedResult actual = hc.checkAdvanced();

    assertTrue(actual.asResult().isHealthy());
  }

  @Test
  public void checkAdvancedBrokenLink() throws Exception {
    HealthcheckConfiguration hcConfiguration =
        new HealthcheckConfiguration("name", 2, "impact", "tech summary", "panic");
    UppServiceConfiguration mockUppServiceConfiguration = mock(UppServiceConfiguration.class);
    Client mockedClient = mock(Client.class);
    EndpointConfiguration mockedEndpointConfiguration = mock(EndpointConfiguration.class);
    when(mockUppServiceConfiguration.getEndpointConfiguration())
        .thenReturn(mockedEndpointConfiguration);
    when(mockedEndpointConfiguration.getHost()).thenReturn("document-store-api");
    when(mockedEndpointConfiguration.getPort()).thenReturn(8080);
    WebResource mockedWebResource = mock(WebResource.class);
    when(mockedClient.resource(URI.create("http://document-store-api:8080/__gtg")))
        .thenReturn(mockedWebResource);
    WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
    when(mockedWebResource.getRequestBuilder()).thenReturn(mockedBuilder);
    when(mockedBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(mockedBuilder);
    when(mockedBuilder.header(anyString(), anyObject())).thenReturn(mockedBuilder);
    doThrow(new RuntimeException("couldn't make the call"))
        .when(mockedBuilder)
        .get(ClientResponse.class);
    DocStoreHealthCheck hc =
        new DocStoreHealthCheck(hcConfiguration, mockUppServiceConfiguration, mockedClient);

    AdvancedResult actual = hc.checkAdvanced();

    assertFalse(actual.asResult().isHealthy());
    assertEquals("couldn't make the call", actual.checkOutput());
  }

  @Test
  public void checkAdvancedNotOkStatus() throws Exception {
    HealthcheckConfiguration hcConfiguration =
        new HealthcheckConfiguration("name", 2, "impact", "tech summary", "panic");
    UppServiceConfiguration mockUppServiceConfiguration = mock(UppServiceConfiguration.class);
    Client mockedClient = mock(Client.class);
    EndpointConfiguration mockedEndpointConfiguration = mock(EndpointConfiguration.class);
    when(mockUppServiceConfiguration.getEndpointConfiguration())
        .thenReturn(mockedEndpointConfiguration);
    when(mockedEndpointConfiguration.getHost()).thenReturn("document-store-api");
    when(mockedEndpointConfiguration.getPort()).thenReturn(8080);
    WebResource mockedWebResource = mock(WebResource.class);
    when(mockedClient.resource(URI.create("http://document-store-api:8080/__gtg")))
        .thenReturn(mockedWebResource);
    WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
    when(mockedWebResource.getRequestBuilder()).thenReturn(mockedBuilder);
    when(mockedBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(mockedBuilder);
    when(mockedBuilder.header(anyString(), anyObject())).thenReturn(mockedBuilder);
    ClientResponse mockedClientResponse = mock(ClientResponse.class);
    when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedClientResponse);
    when(mockedClientResponse.getStatus()).thenReturn(404);
    DocStoreHealthCheck hc =
        new DocStoreHealthCheck(hcConfiguration, mockUppServiceConfiguration, mockedClient);

    AdvancedResult actual = hc.checkAdvanced();

    assertFalse(actual.asResult().isHealthy());
    assertEquals("HTTP connection error: 404", actual.checkOutput());
  }
}
