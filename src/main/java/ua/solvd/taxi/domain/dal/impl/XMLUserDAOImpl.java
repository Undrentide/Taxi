package ua.solvd.taxi.domain.dal.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ua.solvd.taxi.domain.dal.UserDAO;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class XMLUserDAOImpl implements UserDAO {
    private static final Logger logger = LogManager.getLogger(XMLUserDAOImpl.class);
    private static final String FILE_PATH = "src/main/resources/user.xml";

    @Override
    public User save(User user) throws SQLException {
        try {
            Document document = getDocument();
            Element root = document.getDocumentElement();
            Element element = document.createElement("user");
            element.appendChild(createNode(document, "uuid", user.getUuid().toString()));
            element.appendChild(createNode(document, "first_name", user.getFirstName()));
            element.appendChild(createNode(document, "last_name", user.getLastName()));
            element.appendChild(createNode(document, "phone", user.getPhone()));
            element.appendChild(createNode(document, "role_name", user.getRole().getName()));
            root.appendChild(element);
            saveToFile(document);
            return user;
        } catch (Exception e) {
            throw new SQLException("XML Save error", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        logger.warn("findById(Long) is not supported for XML. Use business keys or UUID.");
        return Optional.empty();
    }

    public Optional<User> findByUuid(UUID uuid) throws SQLException {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            String targetUuid = uuid.toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlUuid = element.getElementsByTagName("uuid").item(0).getTextContent();
                if (xmlUuid.equals(targetUuid)) {
                    return Optional.of(mapNodeToUser(element));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new SQLException("Error searching user by UUID in XML", e);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            List<User> userList = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                userList.add(mapNodeToUser((Element) nodeList.item(i)));
            }
            return userList;
        } catch (Exception e) {
            throw new SQLException("XML List error", e);
        }
    }

    @Override
    public Optional<User> findUserByPhone(String phone) throws SQLException {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getElementsByTagName("phone").item(0).getTextContent().equals(phone)) {
                    return Optional.of(mapNodeToUser(element));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new SQLException("XML Read error", e);
        }
    }

    @Override
    public boolean update(Long id, User user) throws SQLException {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            String targetUuid = user.getUuid().toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getElementsByTagName("uuid").item(0).getTextContent().equals(targetUuid)) {
                    element.getElementsByTagName("first_name").item(0).setTextContent(user.getFirstName());
                    element.getElementsByTagName("last_name").item(0).setTextContent(user.getLastName());
                    element.getElementsByTagName("phone").item(0).setTextContent(user.getPhone());
                    element.getElementsByTagName("role_name").item(0).setTextContent(user.getRole().getName());
                    saveToFile(document);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new SQLException("XML Update error", e);
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        logger.error("Delete by Long ID is impossible in XML storage.");
        return false;
    }

    public boolean deleteByPhone(String phone) throws SQLException {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            boolean removed = false;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlPhone = element.getElementsByTagName("phone").item(0).getTextContent();
                if (xmlPhone.equals(phone)) {
                    element.getParentNode().removeChild(element);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                saveToFile(document);
                logger.info("User with phone {} deleted from XML.", phone);
            }
            return removed;
        } catch (Exception e) {
            throw new SQLException("Error deleting user from XML", e);
        }
    }

    private Document getDocument() throws Exception {
        File file = new File(FILE_PATH);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        if (file.exists() && file.length() > 0) return documentBuilder.parse(file);
        Document document = documentBuilder.newDocument();
        document.appendChild(document.createElement("users"));
        return document;
    }

    private void saveToFile(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(new File(FILE_PATH)));
    }

    private Node createNode(Document document, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        return element;
    }

    private User mapNodeToUser(Element element) {
        UUID uuid = UUID.fromString(element.getElementsByTagName("uuid").item(0).getTextContent());
        Role role = new Role(element.getElementsByTagName("role_name").item(0).getTextContent());
        return new User(
                uuid,
                element.getElementsByTagName("first_name").item(0).getTextContent(),
                element.getElementsByTagName("last_name").item(0).getTextContent(),
                element.getElementsByTagName("phone").item(0).getTextContent(),
                role
        );
    }
}
