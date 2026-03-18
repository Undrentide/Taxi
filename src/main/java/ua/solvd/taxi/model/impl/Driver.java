package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class Driver extends Entity {
    private final User user;
    private final Car car;
    private final DriverStatus driverStatus;
    private final BigDecimal rating;
}