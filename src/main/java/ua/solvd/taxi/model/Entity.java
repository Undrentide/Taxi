package ua.solvd.taxi.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Entity {
    private final UUID uuid;

    public Entity() {
        this.uuid = UUID.randomUUID();
    }
}