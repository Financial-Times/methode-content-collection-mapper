package com.ft.methodecontentcollectionmapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class EomLinkedObject {
    private String uuid;
    private String type;
    private String attributes;
    private String workflowStatus;
    private String systemAttributes;

    private EomLinkedObject(@JsonProperty("uuid") String uuid, @JsonProperty("type") String type, @JsonProperty("attributes") String attributes,
            @JsonProperty("workflowStatus") String workflowStatus, @JsonProperty("systemAttributes") String systemAttributes) {
        this.uuid = uuid;
        this.type = type;
        this.attributes = attributes;
        this.workflowStatus = workflowStatus;
        this.systemAttributes = systemAttributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("uuid", uuid)
                          .add("type", type)
                          .add("attributes", attributes)
                          .add("workflowStatus", workflowStatus)
                          .add("systemAttributes", systemAttributes)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final EomLinkedObject that = (EomLinkedObject) o;
        return
            Objects.equal(getUuid(), that.getUuid()) &&
            Objects.equal(getType(), that.getType()) &&
            Objects.equal(getAttributes(), that.getAttributes()) &&
            Objects.equal(getWorkflowStatus(), that.getWorkflowStatus()) &&
            Objects.equal(getSystemAttributes(), that.getSystemAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid(), getType(), getAttributes(), getWorkflowStatus(), getSystemAttributes());
    }

    public static class Builder {
        private String uuid;
        private String type;
        private String attributes;
        private String workflowStatus;
        private String systemAttributes;

        public Builder withUuid(final String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(final String type) {
            this.type = type;
            return this;
        }

        public Builder withAttributes(final String attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withWorkflowStatus(final String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        public Builder withSystemAttributes(final String systemAttributes) {
            this.systemAttributes = systemAttributes;
            return this;
        }

        public EomLinkedObject build() {
            return new EomLinkedObject(uuid, type, attributes, workflowStatus, systemAttributes);
        }

    }

}
