package com.ft.methodecontentcollectionmapper.messaging;

import java.io.IOException;
import java.util.function.Predicate;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodecontentcollectionmapper.exception.ContentCollectionMapperException;
import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.validation.ContentCollectionValidator;

public class NativeCmsPublicationEventsListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final MessageProducingContentCollectionMapper messageProducingContentCollectionMapper;
    private final ObjectMapper objectMapper;
    private final Predicate<Message> messageFilter;
    private final ContentCollectionValidator contentCollectionValidator;

    public NativeCmsPublicationEventsListener(MessageProducingContentCollectionMapper messageProducingContentCollectionMapper,
            ObjectMapper objectMapper, ContentCollectionValidator contentCollectionValidator, String systemCode) {
        this.messageProducingContentCollectionMapper = messageProducingContentCollectionMapper;
        this.objectMapper = objectMapper;
        this.contentCollectionValidator = contentCollectionValidator;
        this.messageFilter = systemIDFilter(systemCode);
    }

    public boolean onMessage(Message message, String transactionId) {
        if (!messageFilter.test(message)) {
            LOGGER.info("Skip message originated from [{}]", message.getOriginSystemId());
            return true;
        }

        LOGGER.info("Process message");
        handleMessage(message, transactionId);

        return true;
    }

    private void handleMessage(Message message, String transactionId) {
        try {
            EomFile methodeContent = objectMapper.reader(EomFile.class).readValue(message.getMessageBody());
            contentCollectionValidator.validate(methodeContent);
            messageProducingContentCollectionMapper.mapPackage(methodeContent, transactionId,
                    message.getMessageTimestamp());
        } catch (ValidationException | UnsupportedTypeException e) {
            LOGGER.info(e.getMessage());
        } catch (IOException e) {
            throw new ContentCollectionMapperException("Unable to map message", e);
        }
    }

    private Predicate<Message> systemIDFilter(String systemCode) {
        return (Message msg) -> (SystemId.systemIdFromCode(systemCode).equals(msg.getOriginSystemId()));
    }
}
