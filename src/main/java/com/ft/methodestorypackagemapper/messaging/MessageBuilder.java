package com.ft.methodestorypackagemapper.messaging;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.time.ZoneOffset.UTC;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodestorypackagemapper.exception.StoryPackageMapperException;
import com.ft.methodestorypackagemapper.model.StoryPackage;

public class MessageBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBuilder.class);
    private static final String CMS_CONTENT_PUBLISHED = "cms-content-published";
    private static final DateTimeFormatter RFC3339_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withResolverStyle(ResolverStyle.STRICT);

    private final UriBuilder contentUriBuilder;
    private final SystemId systemId;
    private final ObjectMapper objectMapper;

    public MessageBuilder(UriBuilder contentUriBuilder, String systemId, ObjectMapper objectMapper) {
        this.contentUriBuilder = contentUriBuilder;
        this.systemId = SystemId.systemIdFromCode(systemId);
        this.objectMapper = objectMapper;
    }

    Message buildMessage(StoryPackage storyPackage) {
        MessageBody msgBody = new MessageBody(storyPackage, contentUriBuilder.build(storyPackage.getUuid()).toString(),
                RFC3339_FMT.format(OffsetDateTime.ofInstant(storyPackage.getLastModified().toInstant(), UTC)),
                storyPackage.getUuid());

        Message msg;
        try {
            msg = new Message.Builder().withMessageId(UUID.randomUUID()).withMessageType(CMS_CONTENT_PUBLISHED)
                    .withMessageTimestamp(new Date()).withOriginSystemId(systemId).withContentType("application/json")
                    .withMessageBody(objectMapper.writeValueAsString(msgBody)).build();

            msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, storyPackage.getPublishReference());
            msg = KeyedMessage.forMessageAndKey(msg, storyPackage.getUuid());
        } catch (JsonProcessingException e) {
            throw new StoryPackageMapperException("unable to write JSON for message", e);
        }

        LOGGER.info("Message transformed: {}", msg.toStringFull());
        return msg;
    }

    public static class MessageBody {
        @JsonProperty("payload")
        public final StoryPackage payload;
        @JsonProperty("contentUri")
        public final String contentUri;
        @JsonProperty("lastModified")
        public final String lastModified;
        @JsonProperty("uuid")
        public final String uuid;

        MessageBody(@JsonProperty("payload") StoryPackage payload, @JsonProperty("contentUri") String contentUri,
                @JsonProperty("lastModified") String lastModified, @JsonProperty("uuid") String uuid) {
            this.contentUri = contentUri;
            this.payload = payload;
            this.lastModified = lastModified;
            this.uuid = uuid;
        }
    }

}
