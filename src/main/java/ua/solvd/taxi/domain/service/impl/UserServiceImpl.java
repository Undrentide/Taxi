package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.UserXMLDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserXMLDAO userXMLDAO = new UserXMLDAO();

    @Override
    public void save(User user) {
        if (userXMLDAO.findUserByPhone(user.getPhone()).isPresent()) {
            throw new PersistenceException("User with this phone already exists.");
        }
        userXMLDAO.save(user);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userXMLDAO.findUserByPhone(phone)
                .orElseThrow(() -> new PersistenceException("User with this phone not found."));
    }
}