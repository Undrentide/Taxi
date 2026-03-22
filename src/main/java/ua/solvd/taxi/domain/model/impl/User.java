package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

@Getter
@RequiredArgsConstructor
public class User extends Entity {
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final Role role;
}