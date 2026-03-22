package ua.solvd.taxi.domain.service;

import ua.solvd.taxi.domain.model.impl.Driver;

import java.util.List;

public interface DriverService {
    List<Driver> findAvailableDrivers();

    Driver getAvailableDriver();
}