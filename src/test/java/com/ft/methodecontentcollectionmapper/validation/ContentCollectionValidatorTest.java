package com.ft.methodecontentcollectionmapper.validation;

import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import java.util.Arrays;
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
    public void thatNullMainUuidThrowsException() {
        final EomFile eomFile = new EomFile.Builder().build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatInvalidMainUuidThrowsException() {
        final EomFile eomFile = new EomFile.Builder().withUuid("abc").build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatNullLinkedUuidThrowsException() throws Exception {
        final EomFile eomFile =
            new EomFile.Builder()
                .withUuid(UUID.randomUUID().toString())
                .withLinkedObjects(new EomLinkedObject.Builder().build())
                .build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatInvalidLinkedUuidThrowsException() throws Exception {
        final EomFile eomFile =
            new EomFile.Builder()
                .withUuid(UUID.randomUUID().toString())
                .withLinkedObjects(new EomLinkedObject.Builder().withUuid("abc").build())
                .build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatNullContentTypeThrowsException() {
        final EomFile eomFile =
            new EomFile.Builder()
                .withUuid(UUID.randomUUID().toString())
                .build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatInvalidContentTypeThrowsException() {
        final EomFile eomFile =
            new EomFile.Builder()
                .withUuid(UUID.randomUUID().toString())
                .withType("typeABC")
                .build();
        contentCollectionValidator.validate(eomFile);
    }

    @Test
    public void thatNoExceptionIsThrownWhenValid() throws Exception {
        final EomFile eomFile =
            new EomFile.Builder()
                .withUuid(UUID.randomUUID().toString())
                .withType("EOM::WebContainer")
                .withLinkedObjects(new EomLinkedObject.Builder().withUuid(UUID.randomUUID().toString()).build())
                .build();
        contentCollectionValidator.validate(eomFile);
    }

}