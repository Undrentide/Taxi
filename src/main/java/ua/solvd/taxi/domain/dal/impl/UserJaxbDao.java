package ua.solvd.taxi.domain.dal.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;
import ua.solvd.taxi.domain.dal.UserDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.model.wrapper.UserListWrapper;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserJaxbDao implements UserDao {
    private static final String FILE_PATH = "src/main/resources/user_jaxb.xml";
    private static final String XSD_PATH = "src/main/resources/user.xsd";
    private final Schema schema;
    private final JAXBContext jaxbContext;

    public UserJaxbDao() {
        try {
            this.jaxbContext = JAXBContext.newInstance(UserListWrapper.class);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            this.schema = schemaFactory.newSchema(new File(XSD_PATH));
        } catch (JAXBException | SAXException e) {
            throw new PersistenceException("Could not initialize JAXB context", e);
        }
    }

    private UserListWrapper loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return new UserListWrapper();
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            return (UserListWrapper) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new PersistenceException("Failed to unmarshal XML", e);
        }
    }

    private void saveData(UserListWrapper userListWrapper) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setSchema(schema);
            marshaller.marshal(userListWrapper, new File(FILE_PATH));
        } catch (JAXBException e) {
            throw new PersistenceException("Failed to marshal XML", e);
        }
    }

    @Override
    public User save(User user) {
        UserListWrapper userListWrapper = loadData();
        userListWrapper.getUserList().add(user);
        saveData(userListWrapper);
        return user;
    }

    public Optional<User> findUserByPhone(String phone) {
        return loadData().getUserList().stream()
                .filter(user -> user.getPhone().equals(phone))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return loadData().getUserList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return loadData().getUserList().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean update(User user) {
        UserListWrapper userListWrapper = loadData();
        List<User> userList = userListWrapper.getUserList();
        UUID targetId = user.getId();
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(targetId)) {
                userList.set(i, user);
                saveData(userListWrapper);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        UserListWrapper userListWrapper = loadData();
        boolean removed = userListWrapper.getUserList().removeIf(user -> user.getId().equals(id));
        if (removed) saveData(userListWrapper);
        return removed;
    }
}