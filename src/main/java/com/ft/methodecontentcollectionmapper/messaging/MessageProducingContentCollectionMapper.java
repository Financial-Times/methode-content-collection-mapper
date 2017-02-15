package com.ft.methodecontentcollectionmapper.messaging;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.methodecontentcollectionmapper.mapping.EomFileToContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import java.util.Collections;
import java.util.Date;

public class MessageProducingContentCollectionMapper {

  private final MessageBuilder messageBuilder;
  private final MessageProducer producer;
  private final EomFileToContentCollectionMapper eomFileToContentCollectionMapper;

  public MessageProducingContentCollectionMapper(MessageBuilder messageBuilder,
      MessageProducer producer, EomFileToContentCollectionMapper eomFileToContentCollectionMapper) {
    this.messageBuilder = messageBuilder;
    this.producer = producer;
    this.eomFileToContentCollectionMapper = eomFileToContentCollectionMapper;
  }

  public void mapPackage(EomFile methodeList, String tid, Date lastModified) {
    ContentCollection contentCollection = eomFileToContentCollectionMapper
        .mapPackage(methodeList, tid, lastModified);
    producer.send(Collections.singletonList(messageBuilder.buildMessage(contentCollection)));
  }

}
