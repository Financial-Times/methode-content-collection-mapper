package com.ft.methodecontentcollectionmapper.client;

import com.ft.methodecontentcollectionmapper.exception.TransientUuidResolverException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;

import java.net.URI;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentStoreApiClientTest {

    @Test
    public void testResolveUUIDWhenOk() throws Exception {
        Client mockedJerseyClient = mock(Client.class);
        WebResource mockedResource = mock(WebResource.class);
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
        WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
        when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);
        when(mockedBuilder.header("Host", "document-store-api")).thenReturn(mockedBuilder);
        ClientResponse mockedResponse = mock(ClientResponse.class);
        when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
        doNothing().when(mockedResponse).close();
        when(mockedResponse.getStatus()).thenReturn(301);
        when(mockedResponse.getLocation()).thenReturn(URI.create("http://api.ft.com/6c6d690a-95e4-449f-9d34-7f211f4e5b1e"));
        DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "document-store-api");

        String resolvedUuid = documentStoreClient.resolveUUID("http://api.ft.com/system/FT-LABS-WP-1-2", "http://blogs.ft.com/the-world/?p=1234", "tid_1");

        assertEquals("6c6d690a-95e4-449f-9d34-7f211f4e5b1e", resolvedUuid);
    }

    @Test(expected = TransientUuidResolverException.class)
    public void testThrowWhen404() throws Exception {
        Client mockedJerseyClient = mock(Client.class);
        WebResource mockedResource = mock(WebResource.class);
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
        WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
        when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);
        ClientResponse mockedResponse = mock(ClientResponse.class);
        when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
        doNothing().when(mockedResponse).close();
        when(mockedResponse.getStatus()).thenReturn(404);
        DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");

        documentStoreClient.resolveUUID("http://api.ft.com/system/FT-LABS-WP-1-2", "http://blogs.ft.com/the-world/?p=1234", "tid_1");
    }

    @Test(expected = TransientUuidResolverException.class)
    public void testThrowWhen503() throws Exception {
        Client mockedJerseyClient = mock(Client.class);
        WebResource mockedResource = mock(WebResource.class);
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
        WebResource.Builder mockedBuilder = mock(WebResource.Builder.class);
        when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);
        ClientResponse mockedResponse = mock(ClientResponse.class);
        when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
        doNothing().when(mockedResponse).close();
        when(mockedResponse.getStatus()).thenReturn(503);
        DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");

        documentStoreClient.resolveUUID("http://api.ft.com/system/FT-LABS-WP-1-2", "http://blogs.ft.com/the-world/?p=1234", "tid_1");
    }
}
