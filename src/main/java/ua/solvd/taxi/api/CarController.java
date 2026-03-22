package ua.solvd.taxi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;
import ua.solvd.taxi.domain.service.impl.CarServiceImpl;

import java.util.List;

public class CarController {
    private static final Logger logger = LogManager.getLogger(CarController.class);
    private final CarService carService = new CarServiceImpl();

    public List<Car> findAllCars() {
        List<Car> carList = carService.findAllCars();
        logger.info("Retrieved {} vehicles from the fleet.", carList.size());
        carList.forEach(car -> logger.debug("Vehicle: {} | {} {}.", car.getLicensePlate(), car.getBrand(), car.getModel()));
        return carList;
    }
}