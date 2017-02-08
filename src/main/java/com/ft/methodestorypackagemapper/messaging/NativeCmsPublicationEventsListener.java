package com.ft.methodestorypackagemapper.messaging;

import java.io.IOException;
import java.util.function.Predicate;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodestorypackagemapper.exception.StoryPackageMapperException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.validation.StoryPackageValidator;

public class NativeCmsPublicationEventsListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final MessageProducingStoryPackageMapper messageProducingStoryPackageMapper;
    private final ObjectMapper objectMapper;
    private final Predicate<Message> messageFilter;
    private final StoryPackageValidator storyPackageValidator;

    public NativeCmsPublicationEventsListener(MessageProducingStoryPackageMapper messageProducingStoryPackageMapper,
            ObjectMapper objectMapper, StoryPackageValidator storyPackageValidator, String systemCode) {
        this.messageProducingStoryPackageMapper = messageProducingStoryPackageMapper;
        this.objectMapper = objectMapper;
        this.storyPackageValidator = storyPackageValidator;
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
            storyPackageValidator.validate(methodeContent);
            messageProducingStoryPackageMapper.mapStoryPackage(methodeContent, transactionId,
                    message.getMessageTimestamp());
        } catch (ValidationException | UnsupportedTypeException e) {
            LOGGER.info(e.getMessage());
        } catch (IOException e) {
            throw new StoryPackageMapperException("Unable to map message", e);
        }
    }

    private Predicate<Message> systemIDFilter(String systemCode) {
        return (Message msg) -> (SystemId.systemIdFromCode(systemCode).equals(msg.getOriginSystemId()));
    }
}
