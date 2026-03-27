package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Driver extends Entity {
    private final User user;
    private final Car car;
    private final DriverStatus driverStatus;
    private final BigDecimal rating;

    public Driver(UUID id, User user, Car car, DriverStatus driverStatus, BigDecimal rating) {
        super(id);
        this.user = user;
        this.car = car;
        this.driverStatus = driverStatus;
        this.rating = rating;
    }
}