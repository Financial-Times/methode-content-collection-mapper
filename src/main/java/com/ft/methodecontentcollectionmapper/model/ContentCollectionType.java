package com.ft.methodecontentcollectionmapper.model;

import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.google.common.collect.ImmutableMap;
import javax.xml.xpath.XPathExpressionException;

public enum ContentCollectionType {
  STORY_PACKAGE_TYPE("story-package"),
  CONTENT_PACKAGE_TYPE("content-package");

  private static ImmutableMap<String, ContentCollectionType> webTypeToContentCollectionTypeMap = ImmutableMap.<String, ContentCollectionType>builder()
      .put("editorsChoice", ContentCollectionType.STORY_PACKAGE_TYPE)
      .put("content-package", ContentCollectionType.CONTENT_PACKAGE_TYPE)
      .build();

  private String contentUriSuffix;

  private ContentCollectionType(String contentUriSuffix) {
    this.contentUriSuffix = contentUriSuffix;
  }

  public String getContentUriSuffix() {
    return contentUriSuffix;
  }

  public static ContentCollectionType fromWebType(String webType, String uuid)
      throws XPathExpressionException {
    ContentCollectionType contentCollectionType = webTypeToContentCollectionTypeMap.get(webType);
    if (contentCollectionType == null) {
      throw new UnsupportedTypeException(uuid, webType, formattedWebTypes());
    }

    return contentCollectionType;
  }

  private static String formattedWebTypes() {
    String formattedWebTypes = "";
    for (String validWebType : webTypeToContentCollectionTypeMap.keySet()) {
      formattedWebTypes += " or " + validWebType;
    }
    formattedWebTypes = formattedWebTypes.replaceFirst(" or ", "");
    return formattedWebTypes;
  }
}
