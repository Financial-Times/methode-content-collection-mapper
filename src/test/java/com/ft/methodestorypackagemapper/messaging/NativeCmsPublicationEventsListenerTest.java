package com.ft.methodestorypackagemapper.messaging;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.validation.StoryPackageValidator;

@RunWith(MockitoJUnitRunner.class)
public class NativeCmsPublicationEventsListenerTest {
    private static final String UUID_STORY_PACKAGE = UUID.randomUUID().toString();
    private static final String EOM_WEB_CONTAINER = "EOM::WebContainer";
    private static final String SYSTEM_CODE = "foobar";
    private static final String TX_ID = "tid_foo";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MessageProducingStoryPackageMapper msgProducingStoryPackageMapper;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private MessageProducer producer;
    @Mock
    private UriBuilder contentUriBuilder;

    private StoryPackageValidator storyPackageValidator;
    private NativeCmsPublicationEventsListener listener;

    @Before
    public void setUp() {
        storyPackageValidator = new StoryPackageValidator();
        listener = new NativeCmsPublicationEventsListener(msgProducingStoryPackageMapper, objectMapper,
                storyPackageValidator, SYSTEM_CODE);
    }

    @Test
    public void thatMessageIsSuccessfullyMapped() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(
                objectMapper.writeValueAsString(buildStoryPackageEomFile(UUID_STORY_PACKAGE, EOM_WEB_CONTAINER)));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingStoryPackageMapper).mapStoryPackage(Matchers.any(), eq(TX_ID), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfUnexpectedSystemIDHeaderFound() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode("foobaz"));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(
                objectMapper.writeValueAsString(buildStoryPackageEomFile(UUID_STORY_PACKAGE, EOM_WEB_CONTAINER)));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingStoryPackageMapper, never()).mapStoryPackage(Matchers.any(), anyString(), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfNotSupportedUuidDetected() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());

        EomFile storyPackageEomFile = buildStoryPackageEomFile("abc", "foobaz");
        msg.setMessageBody(objectMapper.writeValueAsString(storyPackageEomFile));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingStoryPackageMapper, never()).mapStoryPackage(Matchers.any(), anyString(), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfNotSupportedContentTypeDetected() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(objectMapper.writeValueAsString(buildStoryPackageEomFile(UUID_STORY_PACKAGE, "foobaz")));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingStoryPackageMapper, never()).mapStoryPackage(Matchers.any(), anyString(), Matchers.any());
    }

    private EomFile buildStoryPackageEomFile(String uuid, String type) {
        return new EomFile(uuid, type, null, null, "Stories/Write", null, null, null);
    }
}