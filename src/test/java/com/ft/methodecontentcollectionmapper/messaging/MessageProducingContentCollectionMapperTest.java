package com.ft.methodecontentcollectionmapper.messaging;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodecontentcollectionmapper.mapping.EomFileToContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageProducingContentCollectionMapperTest {
  @Mock private MessageBuilder messageBuilder;
  @Mock private MessageProducer producer;
  @Mock private EomFileToContentCollectionMapper eomContentCollectionMapper;

  private MessageProducingContentCollectionMapper msgProducingContentCollectionMapper;

  @Before
  public void setUp() {
    msgProducingContentCollectionMapper =
        new MessageProducingContentCollectionMapper(
            messageBuilder, producer, eomContentCollectionMapper);
  }

  @Test
  public void thatMessageIsCreatedFromMappedContentCollection() {
    Date lastModified = new Date();
    ContentCollection mappedContentCollection =
        new ContentCollection.Builder().withUuid(UUID.randomUUID().toString()).build();

    when(eomContentCollectionMapper.mapPackage(any(), eq("tid"), eq(lastModified)))
        .thenReturn(mappedContentCollection);

    msgProducingContentCollectionMapper.mapPackage(
        buildContentCollectionEomFile(), "tid", lastModified);

    verify(messageBuilder).buildMessage(mappedContentCollection);
  }

  @Test
  public void thatCreatedMessageIsSentToQueue() {
    ContentCollection mockedContentCollection = mock(ContentCollection.class);
    Message mockedMessage = mock(Message.class);
    when(eomContentCollectionMapper.mapPackage(any(), anyString(), any()))
        .thenReturn(mockedContentCollection);
    when(messageBuilder.buildMessage(mockedContentCollection)).thenReturn(mockedMessage);

    msgProducingContentCollectionMapper.mapPackage(
        buildContentCollectionEomFile(), "tid", new Date());

    verify(producer).send(Collections.singletonList(mockedMessage));
  }

  private EomFile buildContentCollectionEomFile() {
    return new EomFile.Builder()
        .withUuid(UUID.randomUUID().toString())
        .withType("EOM::WebContainer")
        .withWorkflowStatus("Stories/Write")
        .build();
  }
}
