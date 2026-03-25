package ua.solvd.taxi.domain.service;

import ua.solvd.taxi.domain.model.impl.User;

public interface UserService {
    void registerUser(User user);

    User findUserByPhone(String phone);
}