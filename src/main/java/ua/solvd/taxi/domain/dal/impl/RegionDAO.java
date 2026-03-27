package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Region;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegionDAO implements DAO<Long, Region> {

    @Override
    public Region save(Region region) {
        String sql = "INSERT INTO region (name, multiplier) VALUES (?, ?)";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, region.getName());
                    preparedStatement.setBigDecimal(2, region.getMultiplier());
                    preparedStatement.executeUpdate();
                    return region;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving region.", e);
        }
    }

    @Override
    public Optional<Region> findById(Long id) {
        String sql = "SELECT regions.name, regions.multiplier FROM region AS regions WHERE regions.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToRegion(resultSet));
                        }
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding region by id.", e);
        }
    }

    @Override
    public List<Region> findAll() {
        String sql = "SELECT regions.name, regions.multiplier FROM region AS regions";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<Region> regionList = new ArrayList<>();
                        while (resultSet.next()) {
                            regionList.add(mapRowToRegion(resultSet));
                        }
                        return regionList;
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all regions.", e);
        }
    }

    @Override
    public boolean update(Long id, Region region) {
        String sql = "UPDATE region SET name = ?, multiplier = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, region.getName());
                    preparedStatement.setBigDecimal(2, region.getMultiplier());
                    preparedStatement.setLong(3, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating region.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM region WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting region.", e);
        }
    }

    private Region mapRowToRegion(ResultSet resultSet) {
        try {
            return new Region(
                    resultSet.getString("name"),
                    resultSet.getBigDecimal("multiplier")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping region.", e);
        }
    }
}