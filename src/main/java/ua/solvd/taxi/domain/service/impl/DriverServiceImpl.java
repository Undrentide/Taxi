package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.DriverDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.service.DriverService;

import java.util.List;

public class DriverServiceImpl implements DriverService {
    private final DriverDAO driverDAO;

    public DriverServiceImpl(DriverDAO driverDAO) {
        this.driverDAO = driverDAO;
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverDAO.findAll().stream()
                .filter(d -> "Available".equalsIgnoreCase(d.getDriverStatus().getName()))
                .toList();
    }

    @Override
    public Driver getAvailableDriver() {
        return driverDAO.findAvailableDriver()
                .orElseThrow(() -> new PersistenceException("No available drivers found at the moment."));
    }
}