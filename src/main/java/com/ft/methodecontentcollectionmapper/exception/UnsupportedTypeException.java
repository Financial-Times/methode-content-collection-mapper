package com.ft.methodecontentcollectionmapper.exception;

public class UnsupportedTypeException extends RuntimeException {
  private static final long serialVersionUID = -1859559690338255784L;
  private final String uuid;

  public UnsupportedTypeException(String uuid, String type, String expectedType) {
    super(
        String.format(
            "Expected content type: [%s], but was: [%s] for uuid %s.", expectedType, type, uuid));
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }
}
