package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Region;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegionDAO extends AbstractDAO implements DAO<Long, Region> {

    @Override
    public Region save(Region region) throws SQLException {
        String sql = "INSERT INTO region (name, multiplier) VALUES (?, ?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region.getName());
                preparedStatement.setBigDecimal(2, region.getMultiplier());
                preparedStatement.executeUpdate();
                return region;
            }
        });
    }

    @Override
    public Optional<Region> findById(Long id) throws SQLException {
        String sql = "SELECT regions.name, regions.multiplier FROM region AS regions WHERE regions.id = ?";
        return execute(connection -> {
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
    }

    @Override
    public List<Region> findAll() throws SQLException {
        String sql = "SELECT regions.name, regions.multiplier FROM region AS regions";
        return execute(connection -> {
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
    }

    @Override
    public boolean update(Long id, Region region) throws SQLException {
        String sql = "UPDATE region SET name = ?, multiplier = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region.getName());
                preparedStatement.setBigDecimal(2, region.getMultiplier());
                preparedStatement.setLong(3, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM region WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private Region mapRowToRegion(ResultSet resultSet) throws SQLException {
        return new Region(
                resultSet.getString("name"),
                resultSet.getBigDecimal("multiplier")
        );
    }
}