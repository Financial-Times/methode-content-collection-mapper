package com.ft.methodestorypackagemapper.messaging;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodestorypackagemapper.exception.StoryPackageMapperException;
import com.ft.methodestorypackagemapper.model.StoryPackage;

@RunWith(MockitoJUnitRunner.class)
public class MessageBuilderTest {

    private static final UUID UUID = java.util.UUID.fromString("4a319c8a-7b8f-4bdb-bb5f-f7fe70872de2");
    private static final String SYSTEM_ID = "foobar";
    private static final String PUBLISH_REFERENCE = "junit";

    @Mock
    private UriBuilder contentUriBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private MessageBuilder messageBuilder;

    @Before
    public void setUp() {
        messageBuilder = new MessageBuilder(contentUriBuilder, SYSTEM_ID, objectMapper);

    }

    @Test
    public void thatMsgHeadersAreSet() throws JsonProcessingException {
        StoryPackage storyPackage = new StoryPackage.Builder().withUuid(UUID.toString()).withLastModified(new Date())
                .withPublishReference(PUBLISH_REFERENCE).build();
        when(contentUriBuilder.build(storyPackage.getUuid())).thenReturn(URI.create("foobar"));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("\"foo\":\"bar\"");

        Message msg = messageBuilder.buildMessage(storyPackage);

        assertThat(msg.getCustomMessageHeader("X-Request-Id"), equalTo(PUBLISH_REFERENCE));
        assertThat(msg.getOriginSystemId().toString(), containsString(SYSTEM_ID));
    }

    @Test
    public void thatMsgBodyIsCorrect() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UriBuilder contentUriBuilder = mock(UriBuilder.class);
        messageBuilder = new MessageBuilder(contentUriBuilder, SYSTEM_ID, objectMapper);

        String lastModified = "2016-11-02T07:59:24.715Z";
        Date lastModifiedDate = Date.from(Instant.parse(lastModified));
        StoryPackage list = new StoryPackage.Builder().withUuid(UUID.toString()).withLastModified(lastModifiedDate)
                .withPublishReference(PUBLISH_REFERENCE).build();

        URI contentUri = URI.create("foobar");
        when(contentUriBuilder.build(UUID.toString())).thenReturn(contentUri);

        Message msg = messageBuilder.buildMessage(list);

        Map<String, Object> msgContent = objectMapper.reader(Map.class).readValue(msg.getMessageBody());
        assertThat(msgContent.get("contentUri"), equalTo(contentUri.toString()));
        assertThat(msgContent.get("lastModified"), equalTo(lastModified));
        assertThat(msgContent.get("payload"), instanceOf(Map.class));
        assertThat(msgContent.get("uuid"), equalTo(UUID.toString()));
    }

    @Test(expected = StoryPackageMapperException.class)
    public void thatStoryPackageMapperExceptionIsThrownIfMarshallingToStringFails() throws JsonProcessingException {
        StoryPackage storyPackage = new StoryPackage.Builder().withUuid(UUID.toString()).withLastModified(new Date())
                .withPublishReference(PUBLISH_REFERENCE).build();
        when(contentUriBuilder.build(storyPackage.getUuid())).thenReturn(URI.create("foobar"));
        when(objectMapper.writeValueAsString(anyMap())).thenThrow(new JsonMappingException("oh-oh"));

        messageBuilder.buildMessage(storyPackage);
    }

}