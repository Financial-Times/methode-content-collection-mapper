package com.ft.methodestorypackagemapper.exception;

import java.util.UUID;

public class UnsupportedTypeException extends RuntimeException {
    private static final long serialVersionUID = -1859559690338255784L;
    private final UUID uuid;

    public UnsupportedTypeException(UUID uuid, String type, String expectedType) {
        super(String.format("[%s] not an %s.", type, expectedType));
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
