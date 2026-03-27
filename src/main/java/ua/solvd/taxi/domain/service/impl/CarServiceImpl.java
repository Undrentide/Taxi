package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.CarDAO;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

import java.util.List;

public class CarServiceImpl implements CarService {
    private final CarDAO carDAO;

    public CarServiceImpl(CarDAO carDAO) {
        this.carDAO = carDAO;
    }

    @Override
    public List<Car> findAll() {
        return carDAO.findAll();
    }
}