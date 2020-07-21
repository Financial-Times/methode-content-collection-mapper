package com.ft.methodecontentcollectionmapper.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodecontentcollectionmapper.exception.TransformationException;
import com.ft.methodecontentcollectionmapper.exception.UnsupportedTypeException;
import com.ft.methodecontentcollectionmapper.mapping.EomFileToContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.messaging.MessageProducingContentCollectionMapper;
import com.ft.methodecontentcollectionmapper.model.ContentCollection;
import com.ft.methodecontentcollectionmapper.model.EomFile;
import com.ft.methodecontentcollectionmapper.validation.ContentCollectionValidator;
import java.util.Date;
import javax.validation.ValidationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/")
public class MethodeContentCollectionResource {
  private static final String CHARSET_UTF_8 = ";charset=utf-8";

  private final MessageProducingContentCollectionMapper msgProducingContentCollectionMapper;
  private final EomFileToContentCollectionMapper eomContentCollectionMapper;
  private final ContentCollectionValidator contentCollectionValidator;

  public MethodeContentCollectionResource(
      MessageProducingContentCollectionMapper msgProducingContentCollectionMapper,
      EomFileToContentCollectionMapper eomContentCollectionMapper,
      ContentCollectionValidator contentCollectionValidator) {
    this.msgProducingContentCollectionMapper = msgProducingContentCollectionMapper;
    this.eomContentCollectionMapper = eomContentCollectionMapper;
    this.contentCollectionValidator = contentCollectionValidator;
  }

  @POST
  @Timed
  @Path("/map")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final ContentCollection map(EomFile eomFile, @Context HttpHeaders httpHeaders) {
    String transactionID = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);

    try {
      contentCollectionValidator.validate(eomFile);
      return eomContentCollectionMapper.mapPackage(eomFile, transactionID, new Date());
    } catch (ValidationException e) {
      throw ClientError.status(422).reason(ErrorMessage.INVALID_UUID).exception(e);
    } catch (UnsupportedTypeException e) {
      throw ClientError.status(422)
          .reason(ErrorMessage.METHODE_CONTENT_TYPE_NOT_SUPPORTED)
          .exception(e);
    } catch (TransformationException e) {
      throw ServerError.status(500).reason(ErrorMessage.UNEXPECTED_RUNTIME_EXCEPTION).exception(e);
    }
  }

  @POST
  @Timed
  @Path("/ingest")
  public final void ingest(EomFile eomFile, @Context HttpHeaders httpHeaders) {
    String transactionID = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);

    try {
      contentCollectionValidator.validate(eomFile);
      msgProducingContentCollectionMapper.mapPackage(eomFile, transactionID, new Date());
    } catch (ValidationException e) {
      throw ClientError.status(422).reason(ErrorMessage.INVALID_UUID).exception(e);
    } catch (UnsupportedTypeException e) {
      throw ClientError.status(422)
          .reason(ErrorMessage.METHODE_CONTENT_TYPE_NOT_SUPPORTED)
          .exception(e);
    } catch (TransformationException e) {
      throw ServerError.status(500).reason(ErrorMessage.UNEXPECTED_RUNTIME_EXCEPTION).exception(e);
    }
  }

  private enum ErrorMessage {
    METHODE_CONTENT_TYPE_NOT_SUPPORTED("Invalid request - resource not a list"),
    UNEXPECTED_RUNTIME_EXCEPTION("An expected error occured during mapping"),
    INVALID_UUID("Invalid uuid");

    private final String text;

    ErrorMessage(String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
