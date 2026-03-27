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
@XmlType(propOrder = {"id", "name"})
public class Role extends Entity {

    @XmlElement(name = "name")
    private String name;

    public Role(String name) {
        super();
        this.name = name;
    }

    public Role(UUID id, String name) {
        super(id);
        this.name = name;
    }

    @XmlElement(name = "id")
    @Override
    public UUID getId() {
        return super.getId();
    }
}