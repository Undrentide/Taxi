package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.UserDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void save(User user) {
        if (userDao.findUserByPhone(user.getPhone()).isPresent()) {
            throw new PersistenceException("User with this phone already exists.");
        }
        userDao.save(user);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userDao.findUserByPhone(phone)
                .orElseThrow(() -> new PersistenceException("User with this phone not found."));
    }
}