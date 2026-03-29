package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.DriverDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.service.DriverService;

import java.util.List;

public class DriverServiceImpl implements DriverService {
    private final DriverDao driverDao;

    public DriverServiceImpl(DriverDao driverDao) {
        this.driverDao = driverDao;
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverDao.findAll().stream()
                .filter(d -> "Available".equalsIgnoreCase(d.getDriverStatus().getName()))
                .toList();
    }

    @Override
    public Driver getAvailableDriver() {
        return driverDao.findAvailableDriver()
                .orElseThrow(() -> new PersistenceException("No available drivers found at the moment."));
    }
}