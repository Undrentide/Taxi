package ua.solvd.taxi.domain.model.impl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"id", "firstName", "lastName", "phone", "role"})
public class User extends Entity {
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;

    public User(String firstName, String lastName, String phone, Role role) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
    }

    public User(UUID id, String firstName, String lastName, String phone, Role role) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
    }

    @XmlElement(name = "id")
    @Override
    public UUID getId() {
        return super.getId();
    }
}