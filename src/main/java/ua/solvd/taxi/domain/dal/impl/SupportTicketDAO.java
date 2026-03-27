package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.SupportTicket;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupportTicketDAO implements DAO<Long, SupportTicket> {

    @Override
    public SupportTicket save(SupportTicket supportTicket) {
        String findUserIdSql = "SELECT id FROM user WHERE phone = ?";
        String insertSql = "INSERT INTO support_ticket (user_id, subject, message, is_resolved) VALUES (?, ?, ?, ?)";
        try {
            return DAOUtil.execute(connection -> {
                long userId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findUserIdSql)) {
                    preparedStatement.setString(1, supportTicket.getUser().getPhone());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            userId = resultSet.getLong("id");
                        } else {
                            throw new SQLException("User not found for support ticket: " + supportTicket.getUser().getPhone());
                        }
                    }
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setString(2, supportTicket.getSubject());
                    preparedStatement.setString(3, supportTicket.getMessage());
                    preparedStatement.setBoolean(4, supportTicket.isResolved());
                    preparedStatement.executeUpdate();
                    return supportTicket;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving support ticket.", e);
        }
    }

    @Override
    public Optional<SupportTicket> findById(Long id) {
        String sql = getBaseSelectQuery() + " WHERE tickets.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToSupportTicket(resultSet));
                        }
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding support ticket by id.", e);
        }
    }

    @Override
    public List<SupportTicket> findAll() {
        String sql = getBaseSelectQuery();
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<SupportTicket> supportTicketList = new ArrayList<>();
                        while (resultSet.next()) {
                            supportTicketList.add(mapRowToSupportTicket(resultSet));
                        }
                        return supportTicketList;
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all support tickets.", e);
        }
    }

    @Override
    public boolean update(Long id, SupportTicket supportTicket) {
        String sql = "UPDATE support_ticket SET subject = ?, message = ?, is_resolved = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, supportTicket.getSubject());
                    preparedStatement.setString(2, supportTicket.getMessage());
                    preparedStatement.setBoolean(3, supportTicket.isResolved());
                    preparedStatement.setLong(4, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating support ticket.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM support_ticket WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting support ticket.", e);
        }
    }

    private String getBaseSelectQuery() {
        return """
                 SELECT
                     tickets.subject, tickets.message, tickets.is_resolved,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name
                 FROM support_ticket AS tickets
                 INNER JOIN user AS users ON tickets.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                """;
    }

    private SupportTicket mapRowToSupportTicket(ResultSet resultSet) {
        Role role;
        try {
            role = new Role(resultSet.getString("role_name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping support ticket.", e);
        }
        User user;
        try {
            user = new User(
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("phone"),
                    role
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping support ticket.", e);
        }
        try {
            return new SupportTicket(
                    user,
                    resultSet.getString("subject"),
                    resultSet.getString("message"),
                    resultSet.getBoolean("is_resolved")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping support ticket.", e);
        }
    }
}