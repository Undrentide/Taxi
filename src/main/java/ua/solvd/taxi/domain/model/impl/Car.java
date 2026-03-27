package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Car extends Entity {
    private final String brand;
    private final String model;
    private final String licensePlate;
    private final String color;
    private final CarClass carClass;

    public Car(UUID id, String brand, String model, String licensePlate, String color, CarClass carClass) {
        super(id);
        this.brand = brand;
        this.model = model;
        this.licensePlate = licensePlate;
        this.color = color;
        this.carClass = carClass;
    }
}