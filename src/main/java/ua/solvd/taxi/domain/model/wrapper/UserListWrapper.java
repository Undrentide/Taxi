package ua.solvd.taxi.domain.model.wrapper;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import ua.solvd.taxi.domain.model.impl.User;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class UserListWrapper {
    @XmlElement(name = "user")
    private List<User> userList = new ArrayList<>();
}