package ua.solvd.taxi.service.impl;

import ua.solvd.taxi.dal.impl.CarDAO;
import ua.solvd.taxi.exception.ServiceException;
import ua.solvd.taxi.model.impl.Car;
import ua.solvd.taxi.service.CarService;

import java.sql.SQLException;
import java.util.List;

public class CarServiceImpl implements CarService {
    private final CarDAO carDAO = new CarDAO();

    @Override
    public List<Car> findAllCars() {
        try {
            return carDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error loading car list occurred.", e);
        }
    }
}