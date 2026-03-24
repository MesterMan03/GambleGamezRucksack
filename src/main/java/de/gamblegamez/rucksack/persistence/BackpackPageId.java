package de.gamblegamez.rucksack.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class BackpackPageId implements Serializable {
    private UUID id;
    private int page;

    @SuppressWarnings("unused")
    public BackpackPageId() {
        // empty no-arg constructor for Hibernate
    }

    public BackpackPageId(UUID id, int page) {
        this.id = id;
        this.page = page;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BackpackPageId that)) {
            return false;
        }
        return page == that.page && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, page);
    }
}

