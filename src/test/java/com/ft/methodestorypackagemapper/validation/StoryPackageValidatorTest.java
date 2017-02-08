package com.ft.methodestorypackagemapper.validation;

import java.util.UUID;

import javax.validation.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.model.EomFile;

@RunWith(MockitoJUnitRunner.class)
public class StoryPackageValidatorTest {
    private StoryPackageValidator storyPackageValidator;

    @Before
    public void setUp() {
        storyPackageValidator = new StoryPackageValidator();
    }

    @Test(expected = ValidationException.class)
    public void thatUuidIsInvalid() {
        EomFile eomFile = new EomFile("abc", null, null, null, null, null, null, null);
        storyPackageValidator.validate(eomFile);
    }

    @Test(expected = UnsupportedTypeException.class)
    public void thatContentTypeIsInvalid() {
        EomFile eomFile = new EomFile(UUID.randomUUID().toString(), "typeABC", null, null, null, null, null, null);
        storyPackageValidator.validate(eomFile);
    }

}
