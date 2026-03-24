package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
public class User extends Entity {
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final Role role;

    public User(String firstName, String lastName, String phone, Role role) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
    }

    public User(UUID uuid, String firstName, String lastName, String phone, Role role) {
        super(uuid);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
    }
}