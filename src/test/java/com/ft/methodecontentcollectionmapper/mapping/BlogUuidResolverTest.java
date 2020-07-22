package com.ft.methodecontentcollectionmapper.mapping;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.methodecontentcollectionmapper.client.DocumentStoreApiClient;
import com.ft.methodecontentcollectionmapper.exception.TransientUuidResolverException;
import com.ft.methodecontentcollectionmapper.exception.UuidResolverException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class BlogUuidResolverTest {

  private static Map<String, String> BRAND_MAPPINGS = new HashMap<>();

  static {
    BRAND_MAPPINGS.put("blogs.ft.com/the-world", "FT-LABS-WP-1-2");
  }

  @Test
  public void testGoodCase() {
    MetricRegistry mockedMetrics = mock(MetricRegistry.class);
    Timer mockedTimer = mock(Timer.class);
    when(mockedMetrics.timer(anyString())).thenReturn(mockedTimer);
    Timer.Context mockedTimerContext = mock(Timer.Context.class);
    when(mockedTimer.time()).thenReturn(mockedTimerContext);
    when(mockedTimerContext.stop()).thenReturn(0L);
    DocumentStoreApiClient dsApiClient = mock(DocumentStoreApiClient.class);
    when(dsApiClient.resolveUUID(
            "http://api.ft.com/system/FT-LABS-WP-1-2",
            "http://blogs.ft.com/the-world/?p=2193913",
            "tid_1"))
        .thenReturn("25966536-334f-42f0-8d57-a90bfc358c75");
    BlogUuidResolver resolver =
        new BlogUuidResolver(
            mockedMetrics, dsApiClient, "http://api.ft.com/system/", BRAND_MAPPINGS);
    String actualUuid =
        resolver.resolveUuid("http://blogs.ft.com/the-world?p=2193913", "2193913", "tid_1");
    assertEquals("25966536-334f-42f0-8d57-a90bfc358c75", actualUuid);
  }

  @Test(expected = UuidResolverException.class)
  public void testNoMappingFound() {
    MetricRegistry mockedMetrics = mock(MetricRegistry.class);
    Timer mockedTimer = mock(Timer.class);
    when(mockedMetrics.timer(anyString())).thenReturn(mockedTimer);
    Timer.Context mockedTimerContext = mock(Timer.Context.class);
    when(mockedTimer.time()).thenReturn(mockedTimerContext);
    when(mockedTimerContext.stop()).thenReturn(0L);
    BlogUuidResolver resolver =
        new BlogUuidResolver(mockedMetrics, null, "http://api.ft.com/system/", BRAND_MAPPINGS);
    resolver.resolveUuid("http://ftalphaville.ft.com?p=2193913", "2193913", "tid_1");
  }

  @Test(expected = UuidResolverException.class)
  public void testThrowIfClientThrows() {
    MetricRegistry mockedMetrics = mock(MetricRegistry.class);
    Timer mockedTimer = mock(Timer.class);
    when(mockedMetrics.timer(anyString())).thenReturn(mockedTimer);
    Timer.Context mockedTimerContext = mock(Timer.Context.class);
    when(mockedTimer.time()).thenReturn(mockedTimerContext);
    when(mockedTimerContext.stop()).thenReturn(0L);
    DocumentStoreApiClient dsApiClient = mock(DocumentStoreApiClient.class);
    when(dsApiClient.resolveUUID(
            "http://api.ft.com/system/FT-LABS-WP-1-2",
            "http://blogs.ft.com/the-world/?p=2193913",
            "tid_1"))
        .thenThrow(
            new UuidResolverException(
                "Couldn't find, could be a 404, could be a 503, could be a network error."));
    BlogUuidResolver resolver =
        new BlogUuidResolver(
            mockedMetrics, dsApiClient, "http://api.ft.com/system/", BRAND_MAPPINGS);
    resolver.resolveUuid("http://blogs.ft.com/the-world?p=2193913", "2193913", "tid_1");
  }

  @Test(expected = UuidResolverException.class)
  public void testThrowIfClientThrowsTransientUuidResolverException() {
    MetricRegistry mockedMetrics = mock(MetricRegistry.class);
    Timer mockedTimer = mock(Timer.class);
    when(mockedMetrics.timer(anyString())).thenReturn(mockedTimer);
    Timer.Context mockedTimerContext = mock(Timer.Context.class);
    when(mockedTimer.time()).thenReturn(mockedTimerContext);
    when(mockedTimerContext.stop()).thenReturn(0L);
    DocumentStoreApiClient dsApiClient = mock(DocumentStoreApiClient.class);
    when(dsApiClient.resolveUUID(
            "http://api.ft.com/system/FT-LABS-WP-1-2",
            "http://blogs.ft.com/the-world/?p=2193913",
            "tid_1"))
        .thenThrow(
            new TransientUuidResolverException(
                "Just some other type of exception",
                URI.create(
                    "document-store-api:8080/content-query?identifierAuthority=http://api.ft.com/system/FT-LABS-WP-1-2&identifierValue=2193913"),
                "2193913"));
    BlogUuidResolver resolver =
        new BlogUuidResolver(
            mockedMetrics, dsApiClient, "http://api.ft.com/system/", BRAND_MAPPINGS);
    resolver.resolveUuid("http://blogs.ft.com/the-world?p=2193913", "2193913", "tid_1");
  }
}
