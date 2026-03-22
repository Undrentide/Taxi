package ua.solvd.taxi.service;

import ua.solvd.taxi.model.impl.Car;

import java.util.List;

public interface CarService {
    List<Car> findAllCars();
}