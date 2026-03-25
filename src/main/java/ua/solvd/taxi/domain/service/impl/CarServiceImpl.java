package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.CarDAOUtil;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

import java.util.List;

public class CarServiceImpl implements CarService {
    private final CarDAOUtil carDAO;

    public CarServiceImpl(CarDAOUtil carDAO) {
        this.carDAO = carDAO;
    }

    @Override
    public List<Car> findAll() {
        return carDAO.findAll();
    }
}