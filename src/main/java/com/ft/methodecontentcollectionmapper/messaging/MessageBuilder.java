package com.ft.methodecontentcollectionmapper.messaging;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.time.ZoneOffset.UTC;

import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.ContentCollectionType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodecontentcollectionmapper.exception.ContentCollectionMapperException;

public class MessageBuilder {
    private static final String CMS_CONTENT_PUBLISHED = "cms-content-published";
    private static final DateTimeFormatter RFC3339_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withResolverStyle(ResolverStyle.STRICT);

    private final String contentUriPrefix;
    private final SystemId systemId;
    private final ObjectMapper objectMapper;

    public MessageBuilder(String contentUriPrefix, String systemId, ObjectMapper objectMapper) {
        this.contentUriPrefix = contentUriPrefix;
        this.systemId = SystemId.systemIdFromCode(systemId);
        this.objectMapper = objectMapper;
    }

    public Message buildMessage(ContentCollection contentCollection) {
        String specificContentUriPrefix = contentUriPrefix;
        if (contentCollection.getType() == ContentCollectionType.STORY_PACKAGE_TYPE) {
          specificContentUriPrefix += "story-package";
        } else if (contentCollection.getType() == ContentCollectionType.CONTENT_PACKAGE_TYPE) {
          specificContentUriPrefix += "content-package";
        }
        UriBuilder contentUriBuilder = UriBuilder.fromUri(specificContentUriPrefix).path("{uuid}");

        MessageBody msgBody = new MessageBody(contentCollection, contentUriBuilder.build(contentCollection.getUuid()).toString(),
                RFC3339_FMT.format(OffsetDateTime.ofInstant(contentCollection.getLastModified().toInstant(), UTC)),
                contentCollection.getUuid());

        Message msg;
        try {
            msg = new Message.Builder().withMessageId(UUID.randomUUID()).withMessageType(CMS_CONTENT_PUBLISHED)
                    .withMessageTimestamp(new Date()).withOriginSystemId(systemId).withContentType("application/json")
                    .withMessageBody(objectMapper.writeValueAsString(msgBody)).build();

            msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, contentCollection.getPublishReference());
            msg = KeyedMessage.forMessageAndKey(msg, contentCollection.getUuid());
        } catch (JsonProcessingException e) {
            throw new ContentCollectionMapperException("unable to write JSON for message", e);
        }

        return msg;
    }

    public static class MessageBody {
        @JsonProperty("payload")
        public final ContentCollection payload;
        @JsonProperty("contentUri")
        public final String contentUri;
        @JsonProperty("lastModified")
        public final String lastModified;
        @JsonProperty("uuid")
        public final String uuid;

        MessageBody(@JsonProperty("payload") ContentCollection payload, @JsonProperty("contentUri") String contentUri,
                @JsonProperty("lastModified") String lastModified, @JsonProperty("uuid") String uuid) {
            this.contentUri = contentUri;
            this.payload = payload;
            this.lastModified = lastModified;
            this.uuid = uuid;
        }
    }

}
