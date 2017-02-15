package com.ft.methodecontentcollectionmapper.resources;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.methodecontentcollectionmapper.exception.TransformationException;
import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.mapping.EomFileToContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.messaging.MessageProducingContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.validation.ContentCollectionValidator;

@RunWith(MockitoJUnitRunner.class)
public class MethodeContentCollectionResourceTest {
    private static final String UUID = randomUUID().toString();
    private static final String MSG = "err";

    private MethodeContentCollectionResource resource;

    @Mock
    private MessageProducingContentCollectionMapper mockMsgProducingContentCollectionMapper;
    @Mock
    private EomFileToContentCollectionMapper mockEomContentCollectionMapper;
    @Mock
    private ContentCollectionValidator mockContentCollectionValidator;
    @Mock
    private HttpHeaders mockHttpHeaders;
    @Mock
    private EomFile eomfile;
    @Mock
    private ContentCollection mockContentCollection;

    @Before
    public void setUp() {
        resource = new MethodeContentCollectionResource(mockMsgProducingContentCollectionMapper, mockEomContentCollectionMapper,
            mockContentCollectionValidator);
        List<String> headerlist = new ArrayList<>();
        headerlist.add("unit-test");
        when(mockHttpHeaders.getRequestHeader("X-Request-Id")).thenReturn(headerlist);
    }

    @Test
    public void thatMappingIsSuccessful() {
        when(mockEomContentCollectionMapper.mapPackage(eq(eomfile), anyString(), Matchers.any()))
                .thenReturn(mockContentCollection);
        ContentCollection contentCollection = resource.map(eomfile, mockHttpHeaders);

        assertThat("The returned list is not valid", contentCollection, equalTo(mockContentCollection));
    }

    @Test
    public void thatMsgProducingContentCollectionMapperIsCalled() {
        resource.ingest(eomfile, mockHttpHeaders);

        verify(mockMsgProducingContentCollectionMapper).mapPackage(eq(eomfile), anyString(), any());
    }

    @Test
    public void thatForTransformationException500IsReturned() {
        exceptionIsThrownAndStatusCodeIsExpected(new TransformationException(new Exception(MSG)),
                HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void thatForUnsupportedTypeException422IsReturned() {
        exceptionIsThrownAndStatusCodeIsExpected(new UnsupportedTypeException(UUID, "", ""),
                HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    private <T extends Exception> void exceptionIsThrownAndStatusCodeIsExpected(T exception, int status) {
        try {
            when(mockEomContentCollectionMapper.mapPackage(eq(eomfile), anyString(), Matchers.any()))
                    .thenThrow(exception);
            resource.map(eomfile, mockHttpHeaders);
        } catch (WebApplicationException wae) {
            assertThat("oh-oh, unexpected status code", wae.getResponse().getStatus(), equalTo(status));
        }
    }
}
