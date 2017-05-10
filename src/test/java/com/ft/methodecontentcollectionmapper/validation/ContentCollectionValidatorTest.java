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
        final EomFile eomFile = new EomFile(null, null, null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatInvalidMainUuidThrowsException() {
        final EomFile eomFile = new EomFile("abc", null, null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatNullLinkedUuidThrowsException() throws Exception {
        final EomFile eomFile =
            new EomFile(
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(new EomLinkedObject(null, null, null, null, null)));

        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = ValidationException.class)
    public void thatInvalidLinkedUuidThrowsException() throws Exception {
        final EomFile eomFile =
            new EomFile(
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(new EomLinkedObject("abc", null, null, null, null)));

        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatNullContentTypeThrowsException() {
        final EomFile eomFile = new EomFile(UUID.randomUUID().toString(), null, null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatInvalidContentTypeThrowsException() {
        final EomFile eomFile = new EomFile(UUID.randomUUID().toString(), "typeABC", null, null, null, null, null, null);
        contentCollectionValidator.validate(eomFile);
    }

    @Test
    public void thatNoExceptionIsThrownWhenValid() throws Exception {
        final EomFile eomFile =
            new EomFile(
                UUID.randomUUID().toString(),
                "EOM::WebContainer",
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(new EomLinkedObject(UUID.randomUUID().toString(), null, null, null, null)));

        contentCollectionValidator.validate(eomFile);
    }
}
