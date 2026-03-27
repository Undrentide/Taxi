package ua.solvd.taxi.domain.service.otherimpl;

import ua.solvd.taxi.domain.dal.UserOtherDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;

public class UserServiceOtherImpl implements UserService {
    private final UserOtherDAO userOtherDAO;

    public UserServiceOtherImpl(UserOtherDAO userOtherDAO) {
        this.userOtherDAO = userOtherDAO;
    }

    @Override
    public void save(User user) {
        if (userOtherDAO.findUserByPhone(user.getPhone()).isPresent()) {
            throw new PersistenceException("User with this phone already exists.");
        }
        userOtherDAO.save(user);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userOtherDAO.findUserByPhone(phone)
                .orElseThrow(() -> new PersistenceException("User with this phone not found."));
    }

}