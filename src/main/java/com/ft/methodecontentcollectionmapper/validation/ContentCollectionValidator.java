package com.ft.methodecontentcollectionmapper.validation;

import com.ft.methodecontentcollectionmapper.model.EomLinkedObject;
import com.ft.uuidutils.UUIDValidation;
import java.util.List;

import javax.validation.ValidationException;

import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.model.EomFile;

public class ContentCollectionValidator {

    private static final String EOM_WEB_CONTAINER = "EOM::WebContainer";

    public void validate(EomFile eomContentCollection) throws ValidationException, UnsupportedTypeException {
        validateUuids(eomContentCollection.getUuid(), eomContentCollection.getLinkedObjects());
        validateContentType(eomContentCollection.getUuid(), eomContentCollection.getType());
    }

    private void validateUuids(final String mainUuid,
                               final List<EomLinkedObject> linkedObjects) throws ValidationException {
        try {
            UUIDValidation.of(mainUuid);
            if (linkedObjects != null) {
                linkedObjects.forEach(linkedObject -> UUIDValidation.of(linkedObject.getUuid()));
            }
        } catch (final IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void validateContentType(String uuid, String type) {
        if (!EOM_WEB_CONTAINER.equals(type)) {
            throw new UnsupportedTypeException(uuid, type, EOM_WEB_CONTAINER);
        }
    }

}
