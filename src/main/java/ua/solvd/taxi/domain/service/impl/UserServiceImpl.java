package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.UserDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void save(User user) {
        if (userDAO.findUserByPhone(user.getPhone()).isPresent()) {
            throw new PersistenceException("User with this phone already exists.");
        }
        userDAO.save(user);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userDAO.findUserByPhone(phone)
                .orElseThrow(() -> new PersistenceException("User with this phone not found."));
    }
}