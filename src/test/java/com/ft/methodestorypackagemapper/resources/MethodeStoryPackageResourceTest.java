package com.ft.methodestorypackagemapper.resources;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.messaging.MessageProducingStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;
import com.ft.methodestorypackagemapper.validation.StoryPackageValidator;

@RunWith(MockitoJUnitRunner.class)
public class MethodeStoryPackageResourceTest {
    private static final String UUID = randomUUID().toString();
    private static final String MSG = "err";

    private MethodeStoryPackageResource resource;

    @Mock
    private MessageProducingStoryPackageMapper mockMsgProducingStoryPackageMapper;
    @Mock
    private EomFileStoryPackageMapper mockEomStoryPackageMapper;
    @Mock
    private StoryPackageValidator mockStoryPackageValidator;
    @Mock
    private HttpHeaders mockHttpHeaders;
    @Mock
    private EomFile eomfile;
    @Mock
    private StoryPackage mockStoryPackage;

    @Before
    public void setUp() {
        resource = new MethodeStoryPackageResource(mockMsgProducingStoryPackageMapper, mockEomStoryPackageMapper,
                mockStoryPackageValidator);
        List<String> headerlist = new ArrayList<>();
        headerlist.add("unit-test");
        when(mockHttpHeaders.getRequestHeader("X-Request-Id")).thenReturn(headerlist);
    }

    @Test
    public void thatMappingIsSuccessful() {
        when(mockEomStoryPackageMapper.mapStoryPackage(eq(eomfile), anyString(), Matchers.any()))
                .thenReturn(mockStoryPackage);
        StoryPackage storyPackage = resource.map(eomfile, mockHttpHeaders);

        assertThat("The returned list is not valid", storyPackage, equalTo(mockStoryPackage));
    }

    @Test
    public void thatMsgProducingStoryPackageMapperIsCalled() {
        resource.ingest(eomfile, mockHttpHeaders);

        verify(mockMsgProducingStoryPackageMapper).mapStoryPackage(eq(eomfile), anyString(), any());
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
            when(mockEomStoryPackageMapper.mapStoryPackage(eq(eomfile), anyString(), Matchers.any()))
                    .thenThrow(exception);
            resource.map(eomfile, mockHttpHeaders);
        } catch (WebApplicationException wae) {
            assertThat("oh-oh, unexpected status code", wae.getResponse().getStatus(), equalTo(status));
        }
    }
}
