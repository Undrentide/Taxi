package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

@Getter
@RequiredArgsConstructor
public class Car extends Entity {
    private final String brand;
    private final String model;
    private final String licensePlate;
    private final String color;
    private final CarClass carClass;
}