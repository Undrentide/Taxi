package ua.solvd.taxi.domain.service.jdbcimpl;

import ua.solvd.taxi.domain.dal.jdbcimpl.CarJDBCDAO;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

import java.util.List;

public class CarServiceJDBCImpl implements CarService {
    private final CarJDBCDAO carJDBCDAO;

    public CarServiceJDBCImpl(CarJDBCDAO carJDBCDAO) {
        this.carJDBCDAO = carJDBCDAO;
    }

    @Override
    public List<Car> findAll() {
        return carJDBCDAO.findAll();
    }
}