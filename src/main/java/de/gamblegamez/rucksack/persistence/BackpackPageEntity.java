package de.gamblegamez.rucksack.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "backpacks")
@IdClass(BackpackPageId.class)
public class BackpackPageEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @SuppressWarnings("unused")
    private UUID id;

    @Id
    @Column(name = "page", nullable = false, updatable = false)
    @SuppressWarnings("unused")
    private int page;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

    public BackpackPageEntity() {
    }

    public BackpackPageEntity(UUID id, int page, byte[] data) {
        this.id = id;
        this.page = page;
        this.data = data;
    }

    public static BackpackPageEntity from(UUID id, int page, byte[] data) {
        return new BackpackPageEntity(id, page, data);
    }

    public byte[] toRawData() {
        return data;
    }
}

