package com.ft.methodecontentcollectionmapper.exception;

public class UnsupportedTypeException extends RuntimeException {
    private static final long serialVersionUID = -1859559690338255784L;
    private final String uuid;

    public UnsupportedTypeException(String uuid, String type, String expectedType) {
        super(String.format("[%s] not an %s.", type, expectedType));
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
