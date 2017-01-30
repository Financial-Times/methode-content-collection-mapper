package com.ft.methodestorypackagemapper.messaging;

import java.io.IOException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodestorypackagemapper.exception.StoryPackageMapperException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.EomFileType;

public class NativeCmsPublicationEventsListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final MessageProducingStoryPackageMapper messageProducingStoryPackageMapper;
    private final ObjectMapper objectMapper;
    private final Predicate<Message> messageFilter;

    public NativeCmsPublicationEventsListener(MessageProducingStoryPackageMapper messageProducingStoryPackageMapper,
            ObjectMapper objectMapper, String systemCode) {
        this.messageProducingStoryPackageMapper = messageProducingStoryPackageMapper;
        this.objectMapper = objectMapper;
        this.messageFilter = systemIDFilter(systemCode).and(contentTypeFilter(objectMapper));
    }

    public boolean onMessage(Message message, String transactionId) {
        if (!messageFilter.test(message)) {
            LOGGER.info("Skip message");
            LOGGER.debug("Skip message {}", message);

            return true;
        }

        LOGGER.info("Process message");
        try {
            EomFile methodeContent = objectMapper.reader(EomFile.class).readValue(message.getMessageBody());
            messageProducingStoryPackageMapper.mapStoryPackage(methodeContent, transactionId,
                    message.getMessageTimestamp());
        } catch (UnsupportedTypeException e) {
            LOGGER.info("Skip message {}", message);
        } catch (IOException e) {
            throw new StoryPackageMapperException("Unable to map message", e);
        }

        return true;
    }

    private Predicate<Message> systemIDFilter(String systemCode) {
        return (Message msg) -> (SystemId.systemIdFromCode(systemCode).equals(msg.getOriginSystemId()));
    }

    private Predicate<Message> contentTypeFilter(ObjectMapper objectMapper) {
        return msg -> {
            EomFile eomFile = null;
            try {
                eomFile = objectMapper.reader(EomFile.class).readValue(msg.getMessageBody());
            } catch (IOException e) {
                LOGGER.warn("Message filter failure", e);
                return false;
            }
            return (EomFileType.EOMWebContainer.getTypeName().equals(eomFile.getType()));
        };
    }
}
