package com.ft.methodestorypackagemapper.messaging;

import java.util.Collections;
import java.util.Date;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;

public class MessageProducingStoryPackageMapper {
    private final MessageBuilder messageBuilder;
    private final MessageProducer producer;
    private final EomFileStoryPackageMapper eomFileStoryPackageMapper;

    public MessageProducingStoryPackageMapper(MessageBuilder messageBuilder, MessageProducer producer, EomFileStoryPackageMapper eomFileStoryPackageMapper) {
        this.messageBuilder = messageBuilder;
        this.producer = producer;
        this.eomFileStoryPackageMapper = eomFileStoryPackageMapper;
    }

    public void mapStoryPackage(EomFile methodeList, String tid, Date lastModified) {
        StoryPackage storyPackage = eomFileStoryPackageMapper.mapStoryPackage(methodeList, tid, lastModified);
        producer.send(Collections.singletonList(messageBuilder.buildMessage(storyPackage)));
    }

}
