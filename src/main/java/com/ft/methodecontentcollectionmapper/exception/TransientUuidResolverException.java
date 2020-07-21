package com.ft.methodecontentcollectionmapper.exception;

import java.net.URI;

public class TransientUuidResolverException extends UuidResolverException {
  private URI uri;
  private String identifierValue;

  public TransientUuidResolverException(String msg, URI uri, String identifierValue) {
    super(msg);
    this.uri = uri;
    this.identifierValue = identifierValue;
  }

  public URI getUri() {
    return uri;
  }

  public String getIdentifierValue() {
    return identifierValue;
  }
}
