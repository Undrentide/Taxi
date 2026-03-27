package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Region extends Entity {
    private final String name;
    private final BigDecimal multiplier;

    public Region(UUID id, String name, BigDecimal multiplier) {
        super(id);
        this.multiplier = multiplier;
        this.name = name;
    }
}