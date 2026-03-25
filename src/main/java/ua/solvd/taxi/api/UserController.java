package ua.solvd.taxi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.UserService;
import ua.solvd.taxi.domain.service.impl.UserServiceImpl;

public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService = new UserServiceImpl();

    public void registerNewUser(String firstName, String lastName, String phone, Role role) {
        userService.save(new User(firstName, lastName, phone, role));
        logger.info("User with phone {} processed.", phone);
    }

    public User findUserByPhone(String phone) {
        User user = userService.findUserByPhone(phone);
        logger.info("User found: {} {}.", user.getFirstName(), user.getLastName());
        return user;
    }
}