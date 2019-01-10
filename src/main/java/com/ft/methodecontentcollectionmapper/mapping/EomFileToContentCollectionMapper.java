package com.ft.methodecontentcollectionmapper.mapping;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

import com.ft.methodecontentcollectionmapper.client.DocumentStoreApiClient;
import com.ft.methodecontentcollectionmapper.exception.ContentCollectionMapperException;
import com.ft.methodecontentcollectionmapper.exception.MethodeMissingFieldException;
import com.ft.methodecontentcollectionmapper.exception.TransformationException;
import com.ft.methodecontentcollectionmapper.exception.UuidResolverException;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.ContentCollectionType;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import com.ft.methodecontentcollectionmapper.model.Item;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class EomFileToContentCollectionMapper {

	private static final String XPATH_CONTAINER_WEB_TYPE = "ObjectMetadata/FTcom/DIFTcomWebType";
	private static final String CONTENT_PLACEHOLDER_SRC = "ContentPlaceholder";
	private static final Set<String> BLOG_CATEGORIES = ImmutableSet.of("blog", "webchat-live-blogs", "webchat-live-qa",
			"webchat-markets-live", "fastft");
	private static final String XPATH_LIST_ITEM_SOURCE = "ObjectMetadata/EditorialNotes/Sources/Source/SourceCode";
	private static final String XPATH_PLACEHOLDER_ORIGINAL_UUID = "ObjectMetadata/EditorialNotes/OriginalUUID";
	private static final String XPATH_LIST_ITEM_TYPE = "ObjectMetadata/WiresIndexing/category";
	private static final String XPATH_GUID = "ObjectMetadata/WiresIndexing/serviceid";
	private static final String XPATH_POST_ID = "ObjectMetadata/WiresIndexing/ref_field";

	private final DocumentStoreApiClient client;
	private BlogUuidResolver blogUuidResolver;

	public EomFileToContentCollectionMapper(DocumentStoreApiClient client, BlogUuidResolver blogUuidResolver) {
		this.client = client;
		this.blogUuidResolver = blogUuidResolver;
	}

	public ContentCollection mapPackage(EomFile eomFile, String transactionId, Date lastModified) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			Document attributesDocument = getDocumentBuilder()
					.parse(new InputSource(new StringReader(eomFile.getAttributes())));
			String webType = xpath.evaluate(XPATH_CONTAINER_WEB_TYPE, attributesDocument);

			ContentCollectionType contentCollectionType = ContentCollectionType.fromWebType(webType, eomFile.getUuid());

			return new ContentCollection.Builder().withUuid(eomFile.getUuid())
					.withItems(extractItems(eomFile.getLinkedObjects(), transactionId))
					.withPublishReference(transactionId).withLastModified(lastModified).withType(contentCollectionType)
					.build();
		} catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
			throw new TransformationException(e);
		}
	}

	private List<Item> extractItems(final List<EomLinkedObject> linkedObjects, final String tid)
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		if (linkedObjects == null) {
			return Collections.emptyList();
		}

		List<Item> items = new LinkedList<>();
		for (final EomLinkedObject linkedObject : linkedObjects) {
			Document attributesDocument = getLinkedObjectAttributes(linkedObject);
			if (attributesDocument != null && isContentPlaceholder(attributesDocument)) {
				items.add(resolveContentPlaceholder(attributesDocument, linkedObject.getUuid(), tid));
			} else {
				items.add(new Item.Builder().withUuid(linkedObject.getUuid()).build());
			}
		}

		return items;
	}

	private Item resolveContentPlaceholder(Document attributesDocument, String linkedObjectUUID, String tid)
			throws XPathExpressionException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		String originalUUID = extractOriginalUUID(xpath, attributesDocument);

		if (Strings.isNullOrEmpty(originalUUID)) {
			String listItemWiredIndexType = extractListItemWiredIndexType(xpath, attributesDocument);

			// this is a CPH referencing a blog
			if (BLOG_CATEGORIES.contains(listItemWiredIndexType)) {
				return resolveToBlogItem(attributesDocument, linkedObjectUUID, tid);
			}
			// this is a CPH referencing an external content
			return new Item.Builder().withUuid(linkedObjectUUID).build();
		}

		try {
			if (!client.canResolveUUID(originalUUID, tid)) {
				throw new UuidResolverException(String.format(
						"Cannot resolve in DocStore originalUUID: [%s] for internal content placeholder: [%s]",
						originalUUID, linkedObjectUUID));
			}
			// this is a CPH referencing an internal content
			return new Item.Builder().withUuid(originalUUID).build();
		} catch (IllegalArgumentException | UuidResolverException e) {
			throw new ContentCollectionMapperException("Unable to map content package", e);
		}
	}

	private Item resolveToBlogItem(Document attributesDocument, String linkedObjectUUID, String tid)
			throws XPathExpressionException {
		UUID fromStringUUID = UUID.fromString(linkedObjectUUID);
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final String referenceId = extractWiredIndexField(XPATH_POST_ID, xpath, attributesDocument, fromStringUUID);
		final String serviceId = extractWiredIndexField(XPATH_GUID, xpath, attributesDocument, fromStringUUID);

		return new Item.Builder().withUuid(blogUuidResolver.resolveUuid(serviceId, referenceId, tid)).build();
	}

	private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		return documentBuilderFactory.newDocumentBuilder();
	}

	private boolean isContentPlaceholder(Document attributesDocument) throws XPathExpressionException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final String listItemSrc = extractSource(xpath, attributesDocument);

		return CONTENT_PLACEHOLDER_SRC.equals(listItemSrc);
	}

	private Document getLinkedObjectAttributes(final EomLinkedObject linkedObject)
			throws ParserConfigurationException, SAXException, IOException {
		final String itemAttributes = linkedObject.getAttributes();
		if (Strings.isNullOrEmpty(itemAttributes)) {
			return null;
		}
		return getDocumentBuilder().parse(new InputSource(new StringReader(itemAttributes)));
	}

	private String extractSource(XPath xPath, Document attributesDocument) throws XPathExpressionException {
		return xPath.evaluate(XPATH_LIST_ITEM_SOURCE, attributesDocument);
	}

	private String extractOriginalUUID(XPath xPath, Document attributesDocument) throws XPathExpressionException {
		return xPath.evaluate(XPATH_PLACEHOLDER_ORIGINAL_UUID, attributesDocument);
	}

	private String extractListItemWiredIndexType(XPath xPath, Document attributesDocument)
			throws XPathExpressionException {
		return xPath.evaluate(XPATH_LIST_ITEM_TYPE, attributesDocument);
	}

	private String extractWiredIndexField(String fieldName, XPath xPath, Document attributesDocument, UUID uuid)
			throws XPathExpressionException {
		final String extractedField = xPath.evaluate(fieldName, attributesDocument);
		if (Strings.isNullOrEmpty(extractedField)) {
			throw new MethodeMissingFieldException(uuid, fieldName);
		}
		return extractedField;
	}
}
