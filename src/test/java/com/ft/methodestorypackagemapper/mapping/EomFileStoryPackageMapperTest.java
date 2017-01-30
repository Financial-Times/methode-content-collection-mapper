package com.ft.methodestorypackagemapper.mapping;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.EomLinkedObject;
import com.ft.methodestorypackagemapper.model.StoryPackage;

@RunWith(MockitoJUnitRunner.class)
public class EomFileStoryPackageMapperTest {
    private static final String STORY_PACKAGE_UUID = "5519a61c-c684-11e6-9043-7e34c07b46ef";
    private static final String ITEM_UUID_1 = "5519a61c-c684-11e6-9043-7e34c07b46ef";
    private static final String ITEM_UUID_2 = "5519a61c-c684-11e6-9043-7e34c07b46ef";
    
    private static final String EOM_WEB_CONTAINER = "EOM::WebContainer";
    private static final String STORY_PACKAGE_TYPE = "editorsChoice";
    private static final String TRANSACTION_ID = "tid_ab!430d8ef";
    private static final Date LAST_MODIFIED = new Date();

    private static final String ATTRIBUTES_TEMPLATE = "<!DOCTYPE ObjectMetadata SYSTEM \"/SysConfig/Classify/FTDWC2/classify.dtd\">\n"
            + "<ObjectMetadata><FTcom><DIFTcomWebType>%s</DIFTcomWebType>\n"
            + "<autoFill/>\n<footwellDedupe/>\n<displayCode/>\n<searchAge/>\n<agingRule/>\n"
            + "<markDeleted>False</markDeleted>\n</FTcom>\n<OutputChannels><DIFTcom><DIFTcomWebID/>\n"
            + "</DIFTcom>\n</OutputChannels>\n</ObjectMetadata>";
    private static final String SYSTEM_ATTRIBUTES = "<props><productInfo><name>FTcom</name>\n<issueDate>20160709</issueDate>\n</productInfo>\n"
            + "<summary/>\n<archiveUuid>244424ae-fc23-11df-a389-00144feabdc0</archiveUuid>\n<workFolder>/FT/WorldNews</workFolder>\n"
            + "<subFolder>Asia</subFolder>\n<templateName>/FT/Library/Masters/DwcTemplates/Story Package.dwc</templateName>\n</props>";

    private EomFileStoryPackageMapper eomStoryPackageMapper;
    private EomFile eomFileStoryPackage;

    @Before
    public void setUp() {
        this.eomStoryPackageMapper = new EomFileStoryPackageMapper();
    }

    @Test(expected = UnsupportedTypeException.class)
    public void shouldThrowUnsupportedExceptionIfNotEditorsChoiceType() {
        mockStoryPackage(STORY_PACKAGE_UUID, "", null);
        eomStoryPackageMapper.mapStoryPackage(eomFileStoryPackage, TRANSACTION_ID, LAST_MODIFIED);

    }

    @Test
    public void shouldReturnStoryPackageWithNoItems() {
        mockStoryPackage(STORY_PACKAGE_UUID, STORY_PACKAGE_TYPE, new ArrayList<>());
        StoryPackage actualStoryPackage = eomStoryPackageMapper.mapStoryPackage(eomFileStoryPackage, TRANSACTION_ID,
                LAST_MODIFIED);

        assertThat(actualStoryPackage.getItems().size(), equalTo(0));
    }

    @Test
    public void shouldReturnAllItems() {
        List<EomLinkedObject> items = new ArrayList<>();
        items.add(new EomLinkedObject(ITEM_UUID_1, "Story", null, null, null));
        items.add(new EomLinkedObject(ITEM_UUID_2, "Story", null, null, null));
        
        mockStoryPackage(STORY_PACKAGE_UUID, STORY_PACKAGE_TYPE, items);
        StoryPackage actualStoryPackage = eomStoryPackageMapper.mapStoryPackage(eomFileStoryPackage, TRANSACTION_ID,
                LAST_MODIFIED);

        assertThat(actualStoryPackage.getItems().size(), equalTo(2));
    }

    private void mockStoryPackage(String uuid, String webType, List<EomLinkedObject> storyPackageItems) {
        eomFileStoryPackage = new EomFile(uuid, EOM_WEB_CONTAINER, null, String.format(ATTRIBUTES_TEMPLATE, webType),
                "", SYSTEM_ATTRIBUTES, null, storyPackageItems);
    }
}
