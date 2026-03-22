package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class DriverLocation extends Entity {
    private final Driver driver;
    private final double latitude;
    private final double longitude;
    private final Instant updatedAt;
}