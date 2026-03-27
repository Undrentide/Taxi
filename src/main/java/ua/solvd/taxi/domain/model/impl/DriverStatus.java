package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DriverStatus extends Entity {
    private final String name;

    public DriverStatus(UUID id, String name) {
        super(id);
        this.name = name;
    }
}