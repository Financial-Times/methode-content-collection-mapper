package com.ft.methodecontentcollectionmapper.exception;

import java.util.UUID;

public abstract class MethodeContentInvalidException extends RuntimeException {
  private static final long serialVersionUID = -5091499042517940743L;
  private final UUID uuid;

  public MethodeContentInvalidException(UUID uuid, String message) {
    super(message);
    this.uuid = uuid;
  }

  public UUID getUuid() {
    return uuid;
  }
}
