package com.ft.methodestorypackagemapper.messaging;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;

@RunWith(MockitoJUnitRunner.class)
public class MessageProducingStoryPackageMapperTest {
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private MessageProducer producer;
    @Mock
    private EomFileStoryPackageMapper eomStoryPackageMapper;

    private MessageProducingStoryPackageMapper msgProducingStoryPackageMapper;

    @Before
    public void setUp() {
        msgProducingStoryPackageMapper = new MessageProducingStoryPackageMapper(messageBuilder, producer,
                eomStoryPackageMapper);
    }

    @Test
    public void thatMessageIsCreatedFromMappedStoryPackage() {
        Date lastModified = new Date();
        StoryPackage mappedStoryPackage = new StoryPackage.Builder().withUuid(UUID.randomUUID().toString()).build();

        when(eomStoryPackageMapper.mapStoryPackage(any(), eq("tid"), eq(lastModified))).thenReturn(mappedStoryPackage);

        msgProducingStoryPackageMapper.mapStoryPackage(buildStoryPackageEomFile(), "tid", lastModified);

        verify(messageBuilder).buildMessage(mappedStoryPackage);
    }
    
    @Test
    public void thatCreatedMessageIsSentToQueue() {
        StoryPackage mockedMapList = mock(StoryPackage.class);
        Message mockedMessage = mock(Message.class);
        when(eomStoryPackageMapper.mapStoryPackage(any(), anyString(), any())).thenReturn(mockedMapList);
        when(messageBuilder.buildMessage(mockedMapList)).thenReturn(mockedMessage);

        msgProducingStoryPackageMapper.mapStoryPackage(buildStoryPackageEomFile(), "tid", new Date());

        verify(producer).send(Collections.singletonList(mockedMessage));
    }

    private EomFile buildStoryPackageEomFile() {
        return new EomFile(UUID.randomUUID().toString(), "EOM::WebContainer", null, null, "Stories/Write", null, null,
                null);
    }
    
}
