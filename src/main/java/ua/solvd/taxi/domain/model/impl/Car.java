package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

@Getter
@RequiredArgsConstructor
public class Car extends Entity {
    private final String brand;
    private final String model;
    private final String licensePlate;
    private final String color;
    private final CarClass carClass;
}