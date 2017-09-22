package com.ft.methodecontentcollectionmapper.client;

import com.ft.methodecontentcollectionmapper.configuration.UppServiceConfiguration;
import com.ft.methodecontentcollectionmapper.exception.TransientUuidResolverException;
import com.ft.methodecontentcollectionmapper.exception.UuidResolverException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import io.dropwizard.setup.Environment;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;

/**
 * Copied from methode-list-mapper.
 */
public class DocumentStoreApiClient extends UppServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentStoreApiClient.class);
    private static final String QUERY_PATH = "/content-query";
    private static final String LISTS_PATH = "/lists";

    private final URI queryEndpoint;
    private final URI listsEndpoint;

    public DocumentStoreApiClient(UppServiceConfiguration uppServiceConfiguration, Environment environment) {
        super(uppServiceConfiguration, environment);
        queryEndpoint = UriBuilder.fromPath(QUERY_PATH).scheme("http").host(apiHost).port(apiPort).build();
        listsEndpoint = UriBuilder.fromPath(LISTS_PATH).scheme("http").host(apiHost).port(apiPort).build();
        configureJersey();
    }

    public DocumentStoreApiClient(Client documentStoreJerseyClient, String docStoreHost, int docStorePort, String docStoreHostHeader) {
        super(documentStoreJerseyClient, docStoreHost, docStorePort, null, docStoreHostHeader);
        queryEndpoint = UriBuilder.fromPath(QUERY_PATH).scheme("http").host(apiHost).port(apiPort).build();
        listsEndpoint = UriBuilder.fromPath(LISTS_PATH).scheme("http").host(apiHost).port(apiPort).build();
        configureJersey();
    }

    private void configureJersey() {
        // Hack to force http client to stop handling redirects. This needs to be changed to the new 'DW way' when we upgrade from v0.7.1
        ClientHandler handler = jerseyClient.getHeadHandler();
        while (handler instanceof ClientFilter) {
            handler = ((ClientFilter) handler).getNext();
        }

        if (handler instanceof ApacheHttpClient4Handler) {
            LOG.info("Reconfiguring underlying http client to stop handling redirects.");
            ApacheHttpClient4Handler apacheHandler = (ApacheHttpClient4Handler) handler;
            apacheHandler.getHttpClient().getParams().setParameter("http.protocol.handle-redirects", false);
        }
    }

    public String resolveUUID(final String identifierAuthority, final String identifierValue, final String transactionId) {
        if (identifierAuthority == null || identifierValue == null) {
            throw new UuidResolverException("Neither the identifierAuthority nor identifierValue should be null!");
        }

        final URI queryUri;
        try {
            queryUri = UriBuilder.fromUri(queryEndpoint)
                    .queryParam("identifierAuthority", URLEncoder.encode(identifierAuthority, "UTF-8"))
                    .queryParam("identifierValue", URLEncoder.encode(identifierValue, "UTF-8"))
                    .build();
        } catch (final UnsupportedEncodingException ex) {
            LOG.error("Failed to encode query params!", ex);
            throw new UuidResolverException(ex);
        }

        LOG.info("Call to Document Store API: {}", queryUri);
        final ClientResponse response = jerseyClient.resource(queryUri)
                .header("Host", hostHeader)
                .header(TRANSACTION_ID_HEADER, transactionId)
                .get(ClientResponse.class);

        return processResponse(response, resp -> {
            if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
                throw new TransientUuidResolverException("Failed to find uuid in Document Store API! QueryURI: " + queryUri, queryUri, identifierValue);
            }

            final URI redirectUrl = resp.getLocation();
            if (redirectUrl == null) {
                LOG.error("DS API could not find the required resource! DS Query URL [{}], Response [{}]", queryUri.toString(), resp.toString());
                throw new UuidResolverException("DS API could not find the required resource! DS Query URL [" + queryUri.toString() + "], Response [" + resp.toString() + "]");
            }

            final String uuid = lastPath(redirectUrl.getPath());
            LOG.info("UUID for [{} / {}] is [{}].", identifierAuthority, identifierValue, uuid);

            return uuid;
        });
    }

    private String lastPath(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
