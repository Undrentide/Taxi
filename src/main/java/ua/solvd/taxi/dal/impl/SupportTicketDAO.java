package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.SupportTicket;
import ua.solvd.taxi.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupportTicketDAO extends AbstractDAO implements DAO<Long, SupportTicket> {

    @Override
    public SupportTicket save(SupportTicket supportTicket) throws SQLException {
        String findUserIdSql = "SELECT id FROM user WHERE phone = ?";
        String insertSql = "INSERT INTO support_ticket (user_id, subject, message, is_resolved) VALUES (?, ?, ?, ?)";
        return execute(connection -> {
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
    }

    @Override
    public Optional<SupportTicket> findById(Long id) throws SQLException {
        String sql = getBaseSelectQuery() + " WHERE tickets.id = ?";
        return execute(connection -> {
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
    }

    @Override
    public List<SupportTicket> findAll() throws SQLException {
        String sql = getBaseSelectQuery();
        return execute(connection -> {
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
    }

    @Override
    public boolean update(Long id, SupportTicket supportTicket) throws SQLException {
        String sql = "UPDATE support_ticket SET subject = ?, message = ?, is_resolved = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, supportTicket.getSubject());
                preparedStatement.setString(2, supportTicket.getMessage());
                preparedStatement.setBoolean(3, supportTicket.isResolved());
                preparedStatement.setLong(4, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM support_ticket WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private String getBaseSelectQuery() {
        return """
                 SELECT\s
                     tickets.subject, tickets.message, tickets.is_resolved,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name
                 FROM support_ticket AS tickets
                 JOIN user AS users ON tickets.user_id = users.id
                 JOIN role AS roles ON users.role_id = roles.id
                \s""";
    }

    private SupportTicket mapRowToSupportTicket(ResultSet resultSet) throws SQLException {
        Role role = new Role(resultSet.getString("role_name"));
        User user = new User(
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                role
        );
        return new SupportTicket(
                user,
                resultSet.getString("subject"),
                resultSet.getString("message"),
                resultSet.getBoolean("is_resolved")
        );
    }
}