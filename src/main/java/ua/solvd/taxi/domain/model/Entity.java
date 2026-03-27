package ua.solvd.taxi.domain.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Getter;

import java.util.UUID;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Entity {

    @XmlTransient
    private UUID uuid;

    protected Entity() {
        this.uuid = UUID.randomUUID();
    }

    protected Entity(UUID uuid) {
        this.uuid = uuid;
    }
}