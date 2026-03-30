package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.Dao;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

import java.util.List;

public class CarServiceImpl implements CarService {
    private final Dao<Car> carDao;

    public CarServiceImpl(Dao<Car> carDao) {
        this.carDao = carDao;
    }

    @Override
    public List<Car> findAll() {
        return carDao.findAll();
    }
}