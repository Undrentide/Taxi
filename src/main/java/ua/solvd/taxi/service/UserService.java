package ua.solvd.taxi.service;

import ua.solvd.taxi.model.impl.User;

public interface UserService {
    void registerUser(User user);

    User findUserByPhone(String phone);
}