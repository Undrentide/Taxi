package ua.solvd.taxi.service.impl;

import ua.solvd.taxi.dal.impl.DriverDAO;
import ua.solvd.taxi.exception.ServiceException;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.service.DriverService;

import java.sql.SQLException;
import java.util.List;

public class DriverServiceImpl implements DriverService {
    private final DriverDAO driverDAO = new DriverDAO();

    @Override
    public List<Driver> findAvailableDrivers() {
        try {
            return driverDAO.findAll().stream()
                    .filter(d -> "Available".equalsIgnoreCase(d.getDriverStatus().getName()))
                    .toList();
        } catch (SQLException e) {
            throw new ServiceException("Error finding active drivers.", e);
        }
    }

    @Override
    public Driver getAvailableDriver() {
        try {
            return driverDAO.findAvailableDriver()
                    .orElseThrow(() -> new ServiceException("No available drivers found at the moment."));
        } catch (SQLException e) {
            throw new ServiceException("Error occurred while searching for an available driver.", e);
        }
    }
}