package com.ft.methodestorypackagemapper.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;

@RunWith(MockitoJUnitRunner.class)
public class MapResourceTest {

    private static final UUID UUID = java.util.UUID.randomUUID();
    private MapResource resource;
    @Mock
    private EomFileStoryPackageMapper mockEomStoryPackageMapper;
    @Mock
    private HttpHeaders mockHttpHeaders;
    @Mock
    private EomFile eomfile;
    @Mock
    private StoryPackage mockStoryPackage;

    @Before
    public void setUp() {
        resource = new MapResource(mockEomStoryPackageMapper);
        List<String> headerlist = new ArrayList<>();
        headerlist.add("unit-test");
        when(mockHttpHeaders.getRequestHeader("X-Request-Id")).thenReturn(headerlist);
    }

    @Test
    public void shouldReturnStoryPackageIfMappingIsSuccessful() {
        when(mockEomStoryPackageMapper.mapStoryPackage(eq(eomfile), anyString(), Matchers.any()))
                .thenReturn(mockStoryPackage);
        StoryPackage storyPackage = resource.map(eomfile, mockHttpHeaders);

        assertThat("The returned list is not valid", storyPackage, equalTo(mockStoryPackage));
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
