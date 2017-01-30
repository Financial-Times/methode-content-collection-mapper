package com.ft.methodestorypackagemapper.resources;

import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodestorypackagemapper.exception.TransformationException;
import com.ft.methodestorypackagemapper.exception.UnsupportedTypeException;
import com.ft.methodestorypackagemapper.messaging.MessageProducingStoryPackageMapper;
import com.ft.methodestorypackagemapper.model.EomFile;

@Path("/ingest")
public class IngestResource {
    private final MessageProducingStoryPackageMapper msgProducingStoryPackageMapper;

    public IngestResource(MessageProducingStoryPackageMapper messageProducingStoryPackageMapper) {
        this.msgProducingStoryPackageMapper = messageProducingStoryPackageMapper;
    }

    @POST
    @Timed
    public final void ingest(EomFile eomFile, @Context HttpHeaders httpHeaders) {
        String transactionID = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);

        try {
            msgProducingStoryPackageMapper.mapStoryPackage(eomFile, transactionID, new Date());
        } catch (UnsupportedTypeException e) {
            throw ClientError.status(422).reason(ErrorMessage.METHODE_CONTENT_TYPE_NOT_SUPPORTED).exception(e);
        } catch (TransformationException e) {
            throw ServerError.status(500).reason(ErrorMessage.UNEXPECTED_RUNTIME_EXCEPTION).exception(e);
        }
    }
    
    private enum ErrorMessage {
        METHODE_CONTENT_TYPE_NOT_SUPPORTED("Invalid request - resource not a list"),
        UNEXPECTED_RUNTIME_EXCEPTION("An expected error occured during mapping");
        
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
