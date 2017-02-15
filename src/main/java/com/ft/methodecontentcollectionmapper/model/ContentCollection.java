package com.ft.methodecontentcollectionmapper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class ContentCollection {
    private String uuid;
    private List<Item> items;
    private Date lastModified;
    private String publishReference;

    private ContentCollectionType type;

    private ContentCollection(@JsonProperty("uuid") String uuid, @JsonProperty("items") List<Item> items,
            @JsonProperty("publishReference") String publishReference,
            @JsonProperty("lastModified") Date lastModified,
            @JsonProperty("type") ContentCollectionType type) {
        this.uuid = uuid;
        this.items = items;
        this.publishReference = publishReference;
        this.lastModified = lastModified;
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public List<Item> getItems() {
        return items;
    }

    public String getPublishReference() {
        return publishReference;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @JsonIgnore
    public ContentCollectionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ContentCollection that = (ContentCollection) o;

        return Objects.equal(this.uuid, that.uuid) && Objects.equal(this.lastModified, that.lastModified)
                && Objects.equal(this.items, that.items) && Objects.equal(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, lastModified, items, publishReference, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("uuid", uuid).add("lastModified", lastModified)
            .add("items", items).add("type", type).toString();
    }

    public static class Builder {
        private String uuid;
        private Date lastModified;
        private List<Item> items;
        private String publishReference;

        private ContentCollectionType type;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withLastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder withItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder withPublishReference(String transactionId) {
            this.publishReference = transactionId;
            return this;
        }

        public Builder withType(ContentCollectionType type) {
            this.type = type;
            return this;
        }

        public ContentCollection build() {
            return new ContentCollection(uuid, items, publishReference, lastModified, type);
        }

    }
}
