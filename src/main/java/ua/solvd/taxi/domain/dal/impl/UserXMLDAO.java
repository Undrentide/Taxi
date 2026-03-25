package ua.solvd.taxi.domain.dal.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.DataAccessException;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserXMLDAO implements DAO<UUID, User> {
    private static final String FILE_PATH = "src/main/resources/user.xml";

    @Override
    public User save(User user) {
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
            throw new PersistenceException("XML Save error", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            String targetUuid = id.toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlUuid = element.getElementsByTagName("uuid").item(0).getTextContent();
                if (xmlUuid.equals(targetUuid)) {
                    return Optional.of(mapNodeToUser(element));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new PersistenceException("Error searching user by UUID in XML", e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                userList.add(mapNodeToUser((Element) nodeList.item(i)));
            }
            return userList;
        } catch (Exception e) {
            throw new PersistenceException("XML List error", e);
        }
    }

    public Optional<User> findUserByPhone(String phone) {
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
            throw new PersistenceException("XML Read error", e);
        }
    }

    @Override
    public boolean update(UUID id, User user) {
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
            throw new PersistenceException("XML Update error", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        try {
            Document document = getDocument();
            NodeList nodeList = document.getElementsByTagName("user");
            boolean removed = false;
            String targetUuid = id.toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlUuid = element.getElementsByTagName("uuid").item(0).getTextContent();
                if (xmlUuid.equals(targetUuid)) {
                    element.getParentNode().removeChild(element);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                saveToFile(document);
            }
            return removed;
        } catch (Exception e) {
            throw new PersistenceException("Error deleting user from XML by UUID", e);
        }
    }

    private Document getDocument() {
        File file = new File(FILE_PATH);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new DataAccessException("Error creating document builder", e);
        }
        if (file.exists() && file.length() > 0) {
            try {
                return documentBuilder.parse(file);
            } catch (SAXException e) {
                throw new DataAccessException("Error parsing XML file", e);
            } catch (IOException e) {
                throw new DataAccessException("Error reading XML file", e);
            }
        }
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