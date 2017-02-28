package com.ft.methodecontentcollectionmapper.mapping;

import com.ft.methodecontentcollectionmapper.exception.TransformationException;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.ContentCollectionType;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import com.ft.methodecontentcollectionmapper.model.Item;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EomFileToContentCollectionMapper {

  private static final String XPATH_CONTAINER_WEB_TYPE = "ObjectMetadata/FTcom/DIFTcomWebType";

  public ContentCollection mapPackage(EomFile eomFile, String transactionId, Date lastModified) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      Document attributesDocument = getDocumentBuilder()
          .parse(new InputSource(new StringReader(eomFile.getAttributes())));
      String webType = xpath.evaluate(XPATH_CONTAINER_WEB_TYPE, attributesDocument);

      ContentCollectionType contentCollectionType = ContentCollectionType.fromWebType(webType,
          eomFile.getUuid());

      return new ContentCollection.Builder().withUuid(eomFile.getUuid())
          .withItems(extractItems(eomFile.getLinkedObjects())).withPublishReference(transactionId)
          .withLastModified(lastModified).withType(contentCollectionType).build();
    } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
      throw new TransformationException(e);
    }
  }

  private List<Item> extractItems(List<EomLinkedObject> linkedObjects)
      throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
    List<Item> listItems = new ArrayList<>();

    for (EomLinkedObject linkedObject : linkedObjects) {
      UUID uuid = UUID.fromString(linkedObject.getUuid());
      Item item = new Item.Builder().withUuid(uuid.toString()).build();
      listItems.add(item);
    }

    return listItems;
  }

  private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory
        .setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    return documentBuilderFactory.newDocumentBuilder();
  }
}