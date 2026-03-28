package ua.solvd.taxi.domain.dal.otherimpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ua.solvd.taxi.domain.dal.UserOtherDao;
import ua.solvd.taxi.domain.exception.DataAccessException;
import ua.solvd.taxi.domain.exception.PersistenceException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserXmlDao implements UserOtherDao {
    private static final String FILE_PATH = "src/main/resources/user.xml";
    private final Document document;

    public UserXmlDao() {
        this.document = loadDocument();
    }

    @Override
    public User save(User user) {
        try {
            Element root = document.getDocumentElement();
            Element userElement = document.createElement("user");
            userElement.appendChild(createNode(document, "id", user.getId().toString()));
            userElement.appendChild(createNode(document, "first_name", user.getFirstName()));
            userElement.appendChild(createNode(document, "last_name", user.getLastName()));
            userElement.appendChild(createNode(document, "phone", user.getPhone()));
            Element roleElement = document.createElement("role");
            roleElement.appendChild(createNode(document, "id", user.getRole().getId().toString()));
            roleElement.appendChild(createNode(document, "name", user.getRole().getName()));
            userElement.appendChild(roleElement);
            root.appendChild(userElement);
            saveToFile(document);
            return user;
        } catch (Exception e) {
            throw new PersistenceException("XML Save error", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        try {
            NodeList nodeList = document.getElementsByTagName("user");
            String targetId = id.toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlId = element.getElementsByTagName("id").item(0).getTextContent();
                if (xmlId.equals(targetId)) {
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
    public boolean update(User user) {
        try {
            NodeList nodeList = document.getElementsByTagName("user");
            String targetId = user.getId().toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlId = element.getElementsByTagName("id").item(0).getTextContent();
                if (xmlId.equals(targetId)) {
                    element.getElementsByTagName("first_name").item(0).setTextContent(user.getFirstName());
                    element.getElementsByTagName("last_name").item(0).setTextContent(user.getLastName());
                    element.getElementsByTagName("phone").item(0).setTextContent(user.getPhone());
                    Element roleElement = (Element) element.getElementsByTagName("role").item(0);
                    roleElement.getElementsByTagName("id").item(0).setTextContent(user.getRole().getId().toString());
                    roleElement.getElementsByTagName("name").item(0).setTextContent(user.getRole().getName());
                    saveToFile(document);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new PersistenceException("XML Update error for user ID: " + user.getId(), e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        try {
            NodeList nodeList = document.getElementsByTagName("user");
            boolean removed = false;
            String targetId = id.toString();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String xmlUuid = element.getElementsByTagName("uuid").item(0).getTextContent();
                if (xmlUuid.equals(targetId)) {
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

    private Document loadDocument() {
        try {
            File file = new File(FILE_PATH);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            if (file.exists() && file.length() > 0) {
                return documentBuilder.parse(file);
            } else {
                Document document = documentBuilder.newDocument();
                document.appendChild(document.createElement("users"));
                return document;
            }
        } catch (Exception e) {
            throw new DataAccessException("Initial XML load failed", e);
        }
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
        UUID id = UUID.fromString(element.getElementsByTagName("id").item(0).getTextContent());
        Element roleNode = (Element) element.getElementsByTagName("role").item(0);
        UUID roleId = UUID.fromString(roleNode.getElementsByTagName("id").item(0).getTextContent());
        String roleName = roleNode.getElementsByTagName("name").item(0).getTextContent();
        Role role = new Role(roleId, roleName);
        return new User(id,
                element.getElementsByTagName("first_name").item(0).getTextContent(),
                element.getElementsByTagName("last_name").item(0).getTextContent(),
                element.getElementsByTagName("phone").item(0).getTextContent(),
                role
        );
    }
}