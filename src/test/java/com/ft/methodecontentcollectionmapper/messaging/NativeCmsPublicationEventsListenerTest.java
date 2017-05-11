package com.ft.methodecontentcollectionmapper.messaging;

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
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.validation.ContentCollectionValidator;

@RunWith(MockitoJUnitRunner.class)
public class NativeCmsPublicationEventsListenerTest {
    private static final String UUID_CONTENT_COLLECTION = UUID.randomUUID().toString();
    private static final String EOM_WEB_CONTAINER = "EOM::WebContainer";
    private static final String SYSTEM_CODE = "foobar";
    private static final String TX_ID = "tid_foo";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MessageProducingContentCollectionMapper msgProducingContentCollectionMapper;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private MessageProducer producer;
    @Mock
    private UriBuilder contentUriBuilder;

    private ContentCollectionValidator contentCollectionValidator;
    private NativeCmsPublicationEventsListener listener;

    @Before
    public void setUp() {
        contentCollectionValidator = new ContentCollectionValidator();
        listener = new NativeCmsPublicationEventsListener(msgProducingContentCollectionMapper, objectMapper,
            contentCollectionValidator, SYSTEM_CODE);
    }

    @Test
    public void thatMessageIsSuccessfullyMapped() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(
                objectMapper.writeValueAsString(buildContentPackageEomFile(UUID_CONTENT_COLLECTION, EOM_WEB_CONTAINER)));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingContentCollectionMapper).mapPackage(Matchers.any(), eq(TX_ID), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfUnexpectedSystemIDHeaderFound() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode("foobaz"));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(
                objectMapper.writeValueAsString(buildContentPackageEomFile(UUID_CONTENT_COLLECTION, EOM_WEB_CONTAINER)));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingContentCollectionMapper, never()).mapPackage(Matchers.any(), anyString(), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfNotSupportedUuidDetected() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());

        EomFile contentCollectionEomFile = buildContentPackageEomFile("abc", "foobaz");
        msg.setMessageBody(objectMapper.writeValueAsString(contentCollectionEomFile));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingContentCollectionMapper, never()).mapPackage(Matchers.any(), anyString(), Matchers.any());
    }

    @Test
    public void thatMessageIsIgnoredIfNotSupportedContentTypeDetected() throws Exception {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        msg.setMessageTimestamp(new Date());
        msg.setMessageBody(objectMapper.writeValueAsString(buildContentPackageEomFile(UUID_CONTENT_COLLECTION, "foobaz")));

        listener.onMessage(msg, TX_ID);

        verify(msgProducingContentCollectionMapper, never()).mapPackage(Matchers.any(), anyString(), Matchers.any());
    }

    private EomFile buildContentPackageEomFile(final String uuid, final String type) {
        return new EomFile.Builder()
            .withUuid(uuid)
            .withType(type)
            .withWorkflowStatus("Stories/Write")
            .build();
    }
}