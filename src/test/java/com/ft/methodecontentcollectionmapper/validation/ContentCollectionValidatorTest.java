package com.ft.methodecontentcollectionmapper.validation;

import java.util.UUID;

import javax.validation.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.model.EomFile;

@RunWith(MockitoJUnitRunner.class)
public class ContentCollectionValidatorTest {
    private ContentCollectionValidator contentCollectionValidator;

    @Before
    public void setUp() {
        contentCollectionValidator = new ContentCollectionValidator();
    }

    @Test(expected = ValidationException.class)
    public void thatUuidIsInvalid() {
        EomFile eomFile = new EomFile("abc", null, null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatContentTypeIsInvalid() {
        EomFile eomFile = new EomFile(UUID.randomUUID().toString(), "typeABC", null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

}
