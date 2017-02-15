package com.ft.methodecontentcollectionmapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Item {
    private String uuid;

    private Item(@JsonProperty("uuid") String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Item that = (Item) o;

        return Objects.equal(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("uuid", uuid).toString();
    }

    public static class Builder {
        private String uuid;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Item build() {
            return new Item(uuid);
        }
    }
}
