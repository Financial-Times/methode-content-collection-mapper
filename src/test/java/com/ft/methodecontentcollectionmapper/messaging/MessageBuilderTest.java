package com.ft.methodecontentcollectionmapper.messaging;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.ContentCollectionType;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
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
import com.ft.methodecontentcollectionmapper.exception.ContentCollectionMapperException;

@RunWith(MockitoJUnitRunner.class)
public class MessageBuilderTest {

    private static final UUID UUID = java.util.UUID.fromString("4a319c8a-7b8f-4bdb-bb5f-f7fe70872de2");
    private static final String SYSTEM_ID = "foobar";
    private static final String PUBLISH_REFERENCE = "junit";

    private String contentUriPrefix = "http://methode-content-collection-mapper.svc.ft.com/content-collection/";
    @Mock
    private ObjectMapper objectMapper;

    private MessageBuilder messageBuilder;

    @Before
    public void setUp() {
        messageBuilder = new MessageBuilder(contentUriPrefix, SYSTEM_ID, objectMapper);
    }

    @Test
    public void thatMsgHeadersAreSet() throws JsonProcessingException {
        ContentCollection contentCollection = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(new Date())
                .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.STORY_PACKAGE_TYPE).build();
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("\"foo\":\"bar\"");

        Message msg = messageBuilder.buildMessage(contentCollection);

        assertThat(msg.getCustomMessageHeader("X-Request-Id"), equalTo(PUBLISH_REFERENCE));
        assertThat(msg.getOriginSystemId().toString(), containsString(SYSTEM_ID));
    }

    @Test
    public void thatMsgBodyIsCorrect() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        messageBuilder = new MessageBuilder(contentUriPrefix, SYSTEM_ID, objectMapper);

        String lastModified = "2016-11-02T07:59:24.715Z";
        Date lastModifiedDate = Date.from(Instant.parse(lastModified));
        ContentCollection list = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(lastModifiedDate)
                .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.STORY_PACKAGE_TYPE).build();

        URI contentUri = URI.create(contentUriPrefix + "story-package/" + UUID.toString());

        Message msg = messageBuilder.buildMessage(list);

        Map<String, Object> msgContent = objectMapper.reader(Map.class).readValue(msg.getMessageBody());
        assertThat(msgContent.get("contentUri"), equalTo(contentUri.toString()));
        assertThat(msgContent.get("lastModified"), equalTo(lastModified));
        assertThat(msgContent.get("payload"), instanceOf(Map.class));
        assertThat(msgContent.get("uuid"), equalTo(UUID.toString()));
    }

    @Test(expected = ContentCollectionMapperException.class)
    public void thatContentCollectionMapperExceptionIsThrownIfMarshallingToStringFails() throws JsonProcessingException {
        ContentCollection contentCollection = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(new Date())
                .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.STORY_PACKAGE_TYPE).build();
        when(objectMapper.writeValueAsString(anyMap())).thenThrow(new JsonMappingException("oh-oh"));

        messageBuilder.buildMessage(contentCollection);
    }

    @Test
    public void thatContentUriIsCorrectForStoryPackage() throws IOException {
      ObjectMapper objectMapper = new ObjectMapper();
      messageBuilder = new MessageBuilder(contentUriPrefix, SYSTEM_ID, objectMapper);

      ContentCollection list = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(new Date())
          .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.STORY_PACKAGE_TYPE).build();

      Message msg = messageBuilder.buildMessage(list);

      URI expectedContentUri = URI.create(contentUriPrefix + "story-package/" + UUID.toString());
      Map<String, Object> msgContent = objectMapper.reader(Map.class).readValue(msg.getMessageBody());
      assertThat(msgContent.get("contentUri"), equalTo(expectedContentUri.toString()));
    }

    @Test
    public void thatContentUriIsCorrectForContentPackage() throws IOException {
      ObjectMapper objectMapper = new ObjectMapper();
      messageBuilder = new MessageBuilder(contentUriPrefix, SYSTEM_ID, objectMapper);

      ContentCollection list = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(new Date())
          .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.CONTENT_PACKAGE_TYPE).build();

      Message msg = messageBuilder.buildMessage(list);

      URI expectedContentUri = URI.create(contentUriPrefix + "content-package/" + UUID.toString());
      Map<String, Object> msgContent = objectMapper.reader(Map.class).readValue(msg.getMessageBody());
      assertThat(msgContent.get("contentUri"), equalTo(expectedContentUri.toString()));
    }

    @Test
    public void thatContentCollectionTypeIsIgnoredInResponseMessage() throws IOException {
      ObjectMapper objectMapper = new ObjectMapper();
      messageBuilder = new MessageBuilder(contentUriPrefix, SYSTEM_ID, objectMapper);

      ContentCollection list = new ContentCollection.Builder().withUuid(UUID.toString()).withLastModified(new Date())
          .withPublishReference(PUBLISH_REFERENCE).withType(ContentCollectionType.STORY_PACKAGE_TYPE).build();

      Message msg = messageBuilder.buildMessage(list);

      Map<String, Object> msgContent = objectMapper.reader(Map.class).readValue(msg.getMessageBody());
      assertThat(((LinkedHashMap)msgContent.get("payload")).get("type"), equalTo(null));
    }
}