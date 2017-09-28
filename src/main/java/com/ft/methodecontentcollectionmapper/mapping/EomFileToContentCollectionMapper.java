package com.ft.methodecontentcollectionmapper.mapping;

import com.ft.methodecontentcollectionmapper.exception.MethodeMissingFieldException;
import com.ft.methodecontentcollectionmapper.exception.TransformationException;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.ContentCollectionType;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import com.ft.methodecontentcollectionmapper.model.Item;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EomFileToContentCollectionMapper {

  private static final String XPATH_CONTAINER_WEB_TYPE = "ObjectMetadata/FTcom/DIFTcomWebType";
  private static final String CONTENT_PLACEHOLDER_SRC = "ContentPlaceholder";
  private static final Set<String> BLOG_CATEGORIES = ImmutableSet.of("blog", "webchat-live-blogs", "webchat-live-qa", "webchat-markets-live", "fastft");
  private static final String XPATH_LIST_ITEM_SOURCE = "ObjectMetadata/EditorialNotes/Sources/Source/SourceCode";
  private static final String XPATH_LIST_ITEM_TYPE = "ObjectMetadata/WiresIndexing/category";
  private static final String XPATH_GUID = "ObjectMetadata/WiresIndexing/serviceid";
  private static final String XPATH_POST_ID = "ObjectMetadata/WiresIndexing/ref_field";

  private BlogUuidResolver blogUuidResolver;

  public EomFileToContentCollectionMapper(final BlogUuidResolver blogUuidResolver) {
    this.blogUuidResolver = blogUuidResolver;
  }

  public ContentCollection mapPackage(EomFile eomFile, String transactionId, Date lastModified) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      Document attributesDocument = getDocumentBuilder()
          .parse(new InputSource(new StringReader(eomFile.getAttributes())));
      String webType = xpath.evaluate(XPATH_CONTAINER_WEB_TYPE, attributesDocument);

      ContentCollectionType contentCollectionType = ContentCollectionType.fromWebType(webType,
          eomFile.getUuid());

      return new ContentCollection.Builder().withUuid(eomFile.getUuid())
              .withItems(extractItems(eomFile.getLinkedObjects(), transactionId))
              .withPublishReference(transactionId)
              .withLastModified(lastModified).withType(contentCollectionType).build();
    } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
      throw new TransformationException(e);
    }
  }

  private List<Item> extractItems(final List<EomLinkedObject> linkedObjects, final String tid) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
      if (linkedObjects == null) {
        return Collections.emptyList();
      }
      List<Item> items = new LinkedList<>();
      for (final EomLinkedObject linkedObject : linkedObjects) {
        if (isContentPlaceholder(linkedObject)) {
          items.add(resolveToBlogItem(linkedObject, tid));
        } else {
          items.add(new Item.Builder().withUuid(linkedObject.getUuid()).build());
        }
      }
      return items;
  }

  private Item resolveToBlogItem(final EomLinkedObject linkedObject, final String tid) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    final String itemAttributes = linkedObject.getAttributes();
    final Document attributesDocument = getDocumentBuilder().parse(new InputSource(new StringReader(itemAttributes)));
    final String referenceId = extractRefField(xpath, attributesDocument, UUID.fromString(linkedObject.getUuid()));
    final String guid = extractServiceId(xpath, attributesDocument, UUID.fromString(linkedObject.getUuid()));
    return new Item.Builder().withUuid(blogUuidResolver.resolveUuid(guid, referenceId, tid)).build();
  }

  private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory
        .setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    return documentBuilderFactory.newDocumentBuilder();
  }

  private boolean isContentPlaceholder(final EomLinkedObject linkedObject) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    final String itemAttributes = linkedObject.getAttributes();
    if (Strings.isNullOrEmpty(itemAttributes)) {
      return false;
    }
    final Document attributesDocument = getDocumentBuilder().parse(new InputSource(new StringReader(itemAttributes)));
    final String listItemSrc = extractSource(xpath, attributesDocument);
    return CONTENT_PLACEHOLDER_SRC.equals(listItemSrc) &&
            BLOG_CATEGORIES.contains(extractListItemWiredIndexType(xpath, attributesDocument));
  }

  private String extractSource(XPath xPath, Document attributesDocument) throws XPathExpressionException {
    return xPath.evaluate(XPATH_LIST_ITEM_SOURCE, attributesDocument);
  }

  private String extractListItemWiredIndexType(XPath xPath, Document attributesDocument) throws XPathExpressionException {
    return xPath.evaluate(XPATH_LIST_ITEM_TYPE, attributesDocument);
  }

  private String extractServiceId(XPath xPath, Document attributesDocument, UUID uuid) throws XPathExpressionException {
    final String serviceId = xPath.evaluate(XPATH_GUID, attributesDocument);
    if (Strings.isNullOrEmpty(serviceId)) {
      throw new MethodeMissingFieldException(uuid, "serviceid", "List");
    }
    return serviceId;
  }

  private String extractRefField(XPath xPath, Document attributesDocument, UUID uuid) throws XPathExpressionException {
    final String refField = xPath.evaluate(XPATH_POST_ID, attributesDocument);
    if (Strings.isNullOrEmpty(refField)) {
      throw new MethodeMissingFieldException(uuid, "ref_field", "List");
    }
    return refField;
  }
}
