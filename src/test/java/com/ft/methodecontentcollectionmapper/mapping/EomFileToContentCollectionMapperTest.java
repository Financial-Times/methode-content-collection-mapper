package com.ft.methodecontentcollectionmapper.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EomFileToContentCollectionMapperTest {

  private static final String CONTENT_COLLECTION_UUID = "5519a61c-c684-11e6-9043-7e34c07b46ef";
  private static final String ITEM_UUID_1 = "5519a61c-c684-11e6-9043-7e34c07b46ef";
  private static final String ITEM_UUID_2 = "5519a61c-c684-11e6-9043-7e34c07b46ef";

  private static final String EOM_WEB_CONTAINER = "EOM::WebContainer";
  private static final String STORY_PACKAGE_TYPE = "editorsChoice";
  private static final String CONTENT_PACKAGE_TYPE = "content-package";
  private static final String TRANSACTION_ID = "tid_ab!430d8ef";
  private static final Date LAST_MODIFIED = new Date();

  private static final String ATTRIBUTES_TEMPLATE =
      "<!DOCTYPE ObjectMetadata SYSTEM \"/SysConfig/Classify/FTDWC2/classify.dtd\">\n"
          + "<ObjectMetadata><FTcom><DIFTcomWebType>%s</DIFTcomWebType>\n"
          + "<autoFill/>\n<footwellDedupe/>\n<displayCode/>\n<searchAge/>\n<agingRule/>\n"
          + "<markDeleted>False</markDeleted>\n</FTcom>\n<OutputChannels><DIFTcom><DIFTcomWebID/>\n"
          + "</DIFTcom>\n</OutputChannels>\n</ObjectMetadata>";
  private static final String SYSTEM_ATTRIBUTES =
      "<props><productInfo><name>FTcom</name>\n<issueDate>20160709</issueDate>\n</productInfo>\n"
          + "<summary/>\n<archiveUuid>244424ae-fc23-11df-a389-00144feabdc0</archiveUuid>\n<workFolder>/FT/WorldNews</workFolder>\n"
          + "<subFolder>Asia</subFolder>\n<templateName>/FT/Library/Masters/DwcTemplates/Story ContentCollection.dwc</templateName>\n</props>";

  private EomFileToContentCollectionMapper eomContentCollectionMapper;
  private EomFile eomFileContentCollection;

  @Before
  public void setUp() {
    this.eomContentCollectionMapper = new EomFileToContentCollectionMapper();
  }

  @Test(expected = UnsupportedTypeException.class)
  public void shouldThrowUnsupportedTypeExceptionForNotContentCollectionTypes() {
    mockContentCollection(CONTENT_COLLECTION_UUID, "", null);
    eomContentCollectionMapper.mapPackage(eomFileContentCollection, TRANSACTION_ID, LAST_MODIFIED);
  }

  @Test
  public void shouldReturnStoryPackageWithNoItems() {
    mockContentCollection(CONTENT_COLLECTION_UUID, STORY_PACKAGE_TYPE, new ArrayList<>());
    ContentCollection actualStoryPackage = eomContentCollectionMapper
        .mapPackage(eomFileContentCollection, TRANSACTION_ID, LAST_MODIFIED);

    assertThat(actualStoryPackage.getItems().size(), equalTo(0));
  }

  @Test
  public void shouldReturnContentPackageWithNoItems() {
    mockContentCollection(CONTENT_COLLECTION_UUID, CONTENT_PACKAGE_TYPE, new ArrayList<>());
    ContentCollection actualContentPackage = eomContentCollectionMapper
        .mapPackage(eomFileContentCollection, TRANSACTION_ID, LAST_MODIFIED);

    assertThat(actualContentPackage.getItems().size(), equalTo(0));
  }

  @Test
  public void shouldNotThrowExceptionIfItemListIsNull() throws Exception {
    mockContentCollection(CONTENT_COLLECTION_UUID, STORY_PACKAGE_TYPE, null);
    final ContentCollection actualStoryPackage = eomContentCollectionMapper
        .mapPackage(eomFileContentCollection, TRANSACTION_ID, LAST_MODIFIED);

    assertThat(actualStoryPackage, is(notNullValue()));
    assertThat(actualStoryPackage.getItems(), is(notNullValue()));
    assertThat(actualStoryPackage.getItems().size(), is(0));
  }

  @Test
  public void shouldReturnAllItems() {
    List<EomLinkedObject> items = new ArrayList<>();
    items.add(new EomLinkedObject(ITEM_UUID_1, "Story", null, null, null));
    items.add(new EomLinkedObject(ITEM_UUID_2, "Story", null, null, null));

    mockContentCollection(CONTENT_COLLECTION_UUID, STORY_PACKAGE_TYPE, items);
    ContentCollection actualStoryPackage = eomContentCollectionMapper
        .mapPackage(eomFileContentCollection, TRANSACTION_ID, LAST_MODIFIED);

    assertThat(actualStoryPackage.getItems().size(), equalTo(2));
  }

  private void mockContentCollection(String uuid, String webType,
      List<EomLinkedObject> contentCollectionItems) {
    eomFileContentCollection = new EomFile(uuid, EOM_WEB_CONTAINER, null,
        String.format(ATTRIBUTES_TEMPLATE, webType), "", SYSTEM_ATTRIBUTES, null,
        contentCollectionItems);
  }
}
