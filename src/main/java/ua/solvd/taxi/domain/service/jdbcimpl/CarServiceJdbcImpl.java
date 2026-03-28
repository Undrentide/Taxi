package ua.solvd.taxi.domain.service.jdbcimpl;

import ua.solvd.taxi.domain.dal.jdbcimpl.CarJdbcDao;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.service.CarService;

import java.util.List;

public class CarServiceJdbcImpl implements CarService {
    private final CarJdbcDao carJDBCDAO;

    public CarServiceJdbcImpl(CarJdbcDao carJDBCDAO) {
        this.carJDBCDAO = carJDBCDAO;
    }

    @Override
    public List<Car> findAll() {
        return carJDBCDAO.findAll();
    }
}