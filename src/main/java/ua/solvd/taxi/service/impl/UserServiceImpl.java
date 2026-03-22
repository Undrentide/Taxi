package ua.solvd.taxi.service.impl;

import ua.solvd.taxi.dal.impl.UserDAO;
import ua.solvd.taxi.exception.ServiceException;
import ua.solvd.taxi.model.impl.User;
import ua.solvd.taxi.service.UserService;

import java.sql.SQLException;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void registerUser(User user) {
        try {
            if (userDAO.findUserByPhone(user.getPhone()).isPresent()) {
                throw new ServiceException("User with this phone already exists.");
            }
            userDAO.save(user);
        } catch (SQLException e) {
            throw new ServiceException("Registration error occurred.", e);
        }
    }

    @Override
    public User findUserByPhone(String phone) {
        try {
            return userDAO.findUserByPhone(phone)
                    .orElseThrow(() -> new ServiceException("User with this phone not found."));
        } catch (SQLException e) {
            throw new ServiceException("Error occurred while searching for user by phone.", e);
        }
    }
}