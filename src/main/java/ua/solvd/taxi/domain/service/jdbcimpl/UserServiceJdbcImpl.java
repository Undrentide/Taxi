package ua.solvd.taxi.domain.service.jdbcimpl;

import ua.solvd.taxi.domain.dal.jdbcimpl.UserJdbcDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

public class UserServiceJdbcImpl implements UserService {
    private final UserJdbcDao userDAO;

    public UserServiceJdbcImpl(UserJdbcDao userDAO) {
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