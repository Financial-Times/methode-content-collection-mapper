package com.ft.methodecontentcollectionmapper.client;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.ft.methodecontentcollectionmapper.exception.TransientUuidResolverException;
import com.ft.methodecontentcollectionmapper.exception.UuidResolverException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class DocumentStoreApiClientTest {
	private static final String VALID_UUID = "38b81198-f18e-11e8-911c-a20996806a68";
	private static final String DOC_STORE_CONTENT_ENDPOINT = "http://document-store-api:8080/content/";
	
	private Client mockedJerseyClient;
	private WebResource mockedResource;
	private WebResource.Builder mockedBuilder;
	
	@Before
	public void setUp() {
		mockedJerseyClient = mock(Client.class);
		mockedResource = mock(WebResource.class);
		mockedBuilder = mock(WebResource.Builder.class);
	}

    @Test
    public void testResolveUUIDWhenOk() throws Exception {
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
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
    public void testResolveUUIDThrowWhen404() throws Exception {
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
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
    public void testResolveUUIDThrowWhen503() throws Exception {
        when(
                mockedJerseyClient.resource(
                        URI.create("http://document-store-api:8080/content-query?" +
                                "identifierAuthority=http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-2&" +
                                "identifierValue=http%3A%2F%2Fblogs.ft.com%2Fthe-world%2F%3Fp%3D1234")
                )
        ).thenReturn(mockedResource);
        when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
        when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);
        ClientResponse mockedResponse = mock(ClientResponse.class);
        when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
        doNothing().when(mockedResponse).close();
        when(mockedResponse.getStatus()).thenReturn(503);
        DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");

        documentStoreClient.resolveUUID("http://api.ft.com/system/FT-LABS-WP-1-2", "http://blogs.ft.com/the-world/?p=1234", "tid_1");
    }
	
	@Test
	public void canResolveUUIDWhenResponse200() {
		String uriString = String.format("%s%s", DOC_STORE_CONTENT_ENDPOINT, VALID_UUID);
		when(mockedJerseyClient.resource(URI.create(uriString))).thenReturn(mockedResource);
		when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
		when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);

		ClientResponse mockedResponse = mock(ClientResponse.class);
		when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
		doNothing().when(mockedResponse).close();
		when(mockedResponse.getStatus()).thenReturn(200);

		DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");
		boolean canResolveUUID = documentStoreClient.canResolveUUID(VALID_UUID, "tid_1");
		
		assertEquals(true, canResolveUUID);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void canResolveUUIDWhenIllegalArgumentException() {
		DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");
		boolean canResolveUUID = documentStoreClient.canResolveUUID("uuid", "tid_1");
		
		assertEquals(false, canResolveUUID);
	}
	
	@Test(expected = UuidResolverException.class)
	public void canResolveUUIDWhenUuidResolverException() {
		String uriString = String.format("%s%s", DOC_STORE_CONTENT_ENDPOINT, VALID_UUID);
		when(mockedJerseyClient.resource(URI.create(uriString))).thenReturn(mockedResource);
		when(mockedResource.getRequestBuilder()).thenReturn(mockedBuilder);
		when(mockedBuilder.header(TRANSACTION_ID_HEADER, "tid_1")).thenReturn(mockedBuilder);

		ClientResponse mockedResponse = mock(ClientResponse.class);
		when(mockedBuilder.get(ClientResponse.class)).thenReturn(mockedResponse);
		doNothing().when(mockedResponse).close();
		when(mockedResponse.getStatus()).thenReturn(503);
		
		DocumentStoreApiClient documentStoreClient = new DocumentStoreApiClient(mockedJerseyClient, "document-store-api", 8080, "");
		boolean canResolveUUID = documentStoreClient.canResolveUUID(VALID_UUID, "tid_1");
		
		assertEquals(false, canResolveUUID);
	}
}

