package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.impl.Driver;

import java.util.Optional;

public interface DriverDao extends Dao<Driver> {
    Optional<Driver> findAvailableDriver();

    boolean updateStatusByPhone(String phone, String statusName);
}