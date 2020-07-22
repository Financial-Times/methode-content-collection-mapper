package com.ft.methodecontentcollectionmapper.exception;

import java.util.UUID;

public class MethodeMissingFieldException extends MethodeContentInvalidException {
  private static final long serialVersionUID = 1957685706838057455L;

  public MethodeMissingFieldException(UUID uuid, String fieldName) {
    super(uuid, String.format("Content is missing field=%s uuid=%s", fieldName, uuid));
  }
}
