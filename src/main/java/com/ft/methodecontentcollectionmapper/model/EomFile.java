package com.ft.methodecontentcollectionmapper.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EomFile {
    private final String uuid;
    private final String type;

    private final byte[] value;
    private final String attributes;
    private final String workflowStatus;
    private final String systemAttributes;
    private final String usageTickets;
    private final List<EomLinkedObject> linkedObjects;

    public EomFile(@JsonProperty("uuid") String uuid, @JsonProperty("type") String type, @JsonProperty("value") byte[] value, @JsonProperty("attributes") String attributes,
            @JsonProperty("workflowStatus") String workflowStatus, @JsonProperty("systemAttributes") String systemAttributes, @JsonProperty("usageTickets") String usageTickets,
            @JsonProperty("linkedObjects") List<EomLinkedObject> linkedObjects) {
        this.uuid = uuid;
        this.type = type;
        this.value = value;
        this.attributes = attributes;
        this.workflowStatus = workflowStatus;
        this.systemAttributes = systemAttributes;
        this.usageTickets = usageTickets;
        this.linkedObjects = linkedObjects;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public String getSystemAttributes() {
        return systemAttributes;
    }

    public String getUsageTickets() {
        return usageTickets;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<EomLinkedObject> getLinkedObjects() {
        return linkedObjects;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("uuid", uuid)
            .add("type", type)
            .add("value", value)
            .add("attributes", attributes)
            .add("workflowStatus", workflowStatus)
            .add("systemAttributes", systemAttributes)
            .add("usageTickets", usageTickets)
            .add("linkedObjects", linkedObjects)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EomFile eomFile = (EomFile) o;
        return
            Objects.equal(getUuid(), eomFile.getUuid()) &&
            Objects.equal(getType(), eomFile.getType()) &&
            Objects.equal(getValue(), eomFile.getValue()) &&
            Objects.equal(getAttributes(), eomFile.getAttributes()) &&
            Objects.equal(getWorkflowStatus(), eomFile.getWorkflowStatus()) &&
            Objects.equal(getSystemAttributes(), eomFile.getSystemAttributes()) &&
            Objects.equal(getUsageTickets(), eomFile.getUsageTickets()) &&
            Objects.equal(getLinkedObjects(), eomFile.getLinkedObjects());
    }

    @Override
    public int hashCode() {
        return Objects
            .hashCode(getUuid(), getType(), getValue(), getAttributes(), getWorkflowStatus(),
                getSystemAttributes(), getUsageTickets(), getLinkedObjects());
    }



}
