package com.ft.methodecontentcollectionmapper.mapping;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.methodecontentcollectionmapper.client.DocumentStoreApiClient;
import com.ft.methodecontentcollectionmapper.exception.UuidResolverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 * Copied from methode-list-mapper.
 */
public class BlogUuidResolver {

    private static final Logger LOG = LoggerFactory.getLogger(BlogUuidResolver.class);

    private final DocumentStoreApiClient client;

    private final Map<String, String> brandMappings;
    private final String authorityPrefix;

    private final Timer timer;

    public BlogUuidResolver(MetricRegistry metrics, DocumentStoreApiClient client, String authorityPrefix, Map<String, String> brandMappings) {
        this.client = client;
        this.authorityPrefix = authorityPrefix;
        this.brandMappings = brandMappings;

        this.timer = metrics.timer(MetricRegistry.name(BlogUuidResolver.class, "resolve-uuid-timer"));
    }

    public String resolveUuid(String guid, String postId, String transactionId) {
        LOG.info("Resolving UUID for GUID [{}] and PostID [{}]", guid, postId);
        Timer.Context context = timer.time();
        try {
            URI guidUri = URI.create(guid);

            String mappingKey = guidUri.getHost() + guidUri.getPath();
            for (String key : brandMappings.keySet()) {
                if (mappingKey.contains(key)) {
                    return resolve(guidUri, postId, key, transactionId);
                }
            }

            throw new UuidResolverException("No brand mapping for Blog serviceId [" + guid + "] - mapping [" + mappingKey + "]!");
        } finally {
            context.stop();
        }
    }

    private String resolve(URI guidUri, String postId, String key, String transactionId) {
        String identifierAuthority = authorityPrefix + brandMappings.get(key);
        String identifierValue = guidUri.getScheme() + "://" + key + "/?p=" + postId; // Wordpress article transformer homogenises the identifierValue, so this is ok to do.

        return client.resolveUUID(identifierAuthority, identifierValue, transactionId);
    }
}
