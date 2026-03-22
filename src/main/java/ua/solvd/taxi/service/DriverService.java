package ua.solvd.taxi.service;

import ua.solvd.taxi.model.impl.Driver;

import java.util.List;

public interface DriverService {
    List<Driver> findAvailableDrivers();

    Driver getAvailableDriver();
}