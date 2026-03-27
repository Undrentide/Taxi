package ua.solvd.taxi.domain.dal.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.solvd.taxi.domain.dal.UserDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserJacksonDAO implements UserDAO {
    private static final String FILE_PATH = "src/main/resources/user.json";
    private final ObjectMapper mapper;

    public UserJacksonDAO() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private List<User> loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new PersistenceException("Failed to load users from JSON", e);
        }
    }

    private void saveData(List<User> userList) {
        try {
            mapper.writeValue(new File(FILE_PATH), userList);
        } catch (IOException e) {
            throw new PersistenceException("Failed to save users to JSON", e);
        }
    }

    @Override
    public User save(User user) {
        List<User> userList = loadData();
        userList.add(user);
        saveData(userList);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return loadData().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return loadData();
    }

    public Optional<User> findUserByPhone(String phone) {
        return loadData().stream()
                .filter(user -> user.getPhone().equals(phone))
                .findFirst();
    }

    @Override
    public boolean update(User updatedUser) {
        List<User> userList = loadData();
        UUID targetId = updatedUser.getId();
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(targetId)) {
                userList.set(i, updatedUser);
                saveData(userList);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        List<User> userList = loadData();
        boolean removed = userList.removeIf(user -> user.getId().equals(id));
        if (removed) {
            saveData(userList);
        }
        return removed;
    }
}