package com.ft.methodestorypackagemapper.model;

public enum EomFileType {
    EOMWebContainer("EOM::WebContainer");

    private final String typeName;

    EomFileType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
