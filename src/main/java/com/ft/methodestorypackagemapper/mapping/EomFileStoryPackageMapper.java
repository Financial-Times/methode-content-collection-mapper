package com.ft.methodestorypackagemapper.mapping;

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

import com.ft.methodestorypackagemapper.exception.TransformationException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.EomLinkedObject;
import com.ft.methodestorypackagemapper.model.Item;
import com.ft.methodestorypackagemapper.model.StoryPackage;
import com.google.common.base.Strings;

public class EomFileStoryPackageMapper {
    private static final String XPATH_CONTAINER_WEB_TYPE = "ObjectMetadata/FTcom/DIFTcomWebType";

    public StoryPackage mapStoryPackage(EomFile eomFile, String transactionId, Date lastModified) {
        UUID uuid = UUID.fromString(eomFile.getUuid());

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Document attributesDocument = getDocumentBuilder()
                    .parse(new InputSource(new StringReader(eomFile.getAttributes())));
            verifyWebType(xpath, attributesDocument, uuid);

            StoryPackage.Builder builder = new StoryPackage.Builder();
            return builder.withUuid(uuid.toString()).withItems(extractItems(eomFile.getLinkedObjects(), transactionId))
                    .withPublishReference(transactionId).withLastModified(lastModified).build();
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new TransformationException(e);
        }
    }

    private List<Item> extractItems(List<EomLinkedObject> linkedObjects, String transactionId) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
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
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return documentBuilderFactory.newDocumentBuilder();
    }

    private void verifyWebType(XPath xpath, Document attributesDocument, UUID uuid) throws XPathExpressionException {
        final String webType = xpath.evaluate(XPATH_CONTAINER_WEB_TYPE, attributesDocument);
        boolean valid = false;
        if (!Strings.isNullOrEmpty(webType)) {
            valid = webType.startsWith("editorsChoice");
        }
        if (!valid) {
            throw new UnsupportedTypeException(uuid, webType, "editorsChoice");
        }
    }

}
