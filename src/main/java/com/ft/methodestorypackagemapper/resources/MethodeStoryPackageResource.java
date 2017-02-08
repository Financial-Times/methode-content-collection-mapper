package com.ft.methodestorypackagemapper.resources;

import java.util.Date;

import javax.validation.ValidationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodestorypackagemapper.exception.TransformationException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.mapping.EomFileStoryPackageMapper;
import com.ft.methodestorypackagemapper.messaging.MessageProducingStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;
import com.ft.methodestorypackagemapper.model.StoryPackage;
import com.ft.methodestorypackagemapper.validation.StoryPackageValidator;

@Path("/")
public class MethodeStoryPackageResource {
    private static final String CHARSET_UTF_8 = ";charset=utf-8";

    private final MessageProducingStoryPackageMapper msgProducingStoryPackageMapper;
    private final EomFileStoryPackageMapper eomStoryPackageMapper;
    private final StoryPackageValidator storyPackageValidator;

    public MethodeStoryPackageResource(MessageProducingStoryPackageMapper msgProducingStoryPackageMapper,
            EomFileStoryPackageMapper eomStoryPackageMapper, StoryPackageValidator storyPackageValidator) {
        this.msgProducingStoryPackageMapper = msgProducingStoryPackageMapper;
        this.eomStoryPackageMapper = eomStoryPackageMapper;
        this.storyPackageValidator = storyPackageValidator;
    }

    @POST
    @Timed
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final StoryPackage map(EomFile eomFile, @Context HttpHeaders httpHeaders) {
        String transactionID = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);
        
        try {
            storyPackageValidator.validate(eomFile);
            return eomStoryPackageMapper.mapStoryPackage(eomFile, transactionID, new Date());
        } catch (ValidationException e) {
            throw ClientError.status(422).reason(ErrorMessage.INVALID_UUID).exception(e);
        } catch (UnsupportedTypeException e) {
            throw ClientError.status(422).reason(ErrorMessage.METHODE_CONTENT_TYPE_NOT_SUPPORTED).exception(e);
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
            storyPackageValidator.validate(eomFile);
            msgProducingStoryPackageMapper.mapStoryPackage(eomFile, transactionID, new Date());
        } catch (ValidationException e) {
            throw ClientError.status(422).reason(ErrorMessage.INVALID_UUID).exception(e);
        } catch (UnsupportedTypeException e) {
            throw ClientError.status(422).reason(ErrorMessage.METHODE_CONTENT_TYPE_NOT_SUPPORTED).exception(e);
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
