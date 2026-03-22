package ua.solvd.taxi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.service.DriverService;
import ua.solvd.taxi.domain.service.impl.DriverServiceImpl;

import java.util.List;

public class DriverController {
    private static final Logger logger = LogManager.getLogger(DriverController.class);
    private final DriverService driverService = new DriverServiceImpl();

    public List<Driver> findAvailableDrivers() {
        List<Driver> driverList = driverService.findAvailableDrivers();
        logger.info("Retrieved {} available drivers from the system", driverList.size());
        driverList.forEach(driver -> logger.info("Available: {}.", driver));
        return driverList;
    }

    public Driver findAvailableDriver() {
        Driver driver = driverService.getAvailableDriver();
        logger.info("Driver assigned: {} {} (Plate: {}).",
                driver.getUser().getFirstName(),
                driver.getUser().getLastName(),
                driver.getCar().getLicensePlate());
        return driver;
    }
}