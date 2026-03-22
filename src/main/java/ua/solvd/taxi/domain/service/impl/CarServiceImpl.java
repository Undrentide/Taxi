package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.CarDAO;
import ua.solvd.taxi.domain.exception.ServiceException;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

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