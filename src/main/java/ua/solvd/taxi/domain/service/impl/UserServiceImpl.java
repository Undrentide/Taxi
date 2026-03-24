package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.UserDAO;
import ua.solvd.taxi.domain.exception.ServiceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

import java.sql.SQLException;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void save(User user) {
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