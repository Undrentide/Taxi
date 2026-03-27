package ua.solvd.taxi.domain.service.jdbcimpl;

import ua.solvd.taxi.domain.dal.jdbcimpl.DriverJDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.service.DriverService;

import java.util.List;

public class DriverServiceJDBCImpl implements DriverService {
    private final DriverJDBCDAO driverJDBCDAO;

    public DriverServiceJDBCImpl(DriverJDBCDAO driverJDBCDAO) {
        this.driverJDBCDAO = driverJDBCDAO;
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverJDBCDAO.findAll().stream()
                .filter(d -> "Available".equalsIgnoreCase(d.getDriverStatus().getName()))
                .toList();
    }

    @Override
    public Driver getAvailableDriver() {
        return driverJDBCDAO.findAvailableDriver()
                .orElseThrow(() -> new PersistenceException("No available drivers found at the moment."));
    }
}