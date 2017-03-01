package com.ft.methodecontentcollectionmapper.validation;

import java.util.UUID;

import javax.validation.ValidationException;

import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.model.EomFile;

public class ContentCollectionValidator {
    private static final String EOMWebContainer = "EOM::WebContainer";

    public void validate(EomFile eomContentCollection) throws ValidationException, UnsupportedTypeException {
        validateUuid(eomContentCollection.getUuid());
        validateContentType(eomContentCollection.getUuid(), eomContentCollection.getType());
    }

    private void validateUuid(String uuid) throws ValidationException {
        try {
            final UUID parsedUuid = UUID.fromString(uuid);
            if (!parsedUuid.toString().equals(uuid.toLowerCase())) {
                throw new ValidationException(String.format("Invalid UUID: [%s], does not conform to RFC 4122", uuid));
            }
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new ValidationException(String.format("Invalid UUID: [%s], does not conform to RFC 4122", uuid));
        }
    }

    private void validateContentType(String uuid, String type) {
        if (!EOMWebContainer.equals(type)) {
            throw new UnsupportedTypeException(uuid, type, EOMWebContainer);
        }
    }

}
