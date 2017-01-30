package com.ft.methodestorypackagemapper.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.ft.methodestorypackagemapper.exception.TransformationException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.messaging.MessageProducingStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;

@RunWith(MockitoJUnitRunner.class)
public class IngestResourceTest {
    private static final String MSG = "err";
    private static final java.util.UUID UUID = java.util.UUID.randomUUID();

    private IngestResource resource;
    @Mock
    private MessageProducingStoryPackageMapper mockMsgProducingStoryPackageMapper;
    @Mock
    private HttpHeaders mockHttpHeaders;
    @Mock
    private EomFile eomfile;
    @Mock
    private StoryPackage storyPackage;

    @Before
    public void setUp() {
        resource = new IngestResource(mockMsgProducingStoryPackageMapper);
        List<String> headerlist = new ArrayList<>();
        headerlist.add("unit-test");
        when(mockHttpHeaders.getRequestHeader("X-Request-Id")).thenReturn(headerlist);
    }

    @Test
    public void thatMsgProducingStoryPackageMapperIsCalled() {
        resource.ingest(eomfile, mockHttpHeaders);

        verify(mockMsgProducingStoryPackageMapper).mapStoryPackage(eq(eomfile), anyString(), any());
    }

    @Test
    public void thatForUnsupportedTypeException422IsReturned() {
        exceptionIsThrownAndStatusCodeIsExpected(new UnsupportedTypeException(UUID, "", ""),
                HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void thatForTransformationException500IsReturned() {
        exceptionIsThrownAndStatusCodeIsExpected(new TransformationException(new Exception(MSG)),
                HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private <T extends Exception> void exceptionIsThrownAndStatusCodeIsExpected(T exception, int status) {
        try {
            doThrow(exception).when(mockMsgProducingStoryPackageMapper).mapStoryPackage(eq(eomfile), anyString(),
                    Matchers.any());
            resource.ingest(eomfile, mockHttpHeaders);
        } catch (WebApplicationException wae) {
            assertThat("oh-oh, unexpected status code", wae.getResponse().getStatus(), equalTo(status));
        }
    }
}