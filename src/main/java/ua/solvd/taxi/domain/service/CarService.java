package ua.solvd.taxi.domain.service;

import ua.solvd.taxi.domain.model.impl.Car;

import java.util.List;

public interface CarService {
    List<Car> findAllCars();
}