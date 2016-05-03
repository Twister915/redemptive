package tech.rayline.core.jsonchat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class XmlJsonChatConverter {
    private final static DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private final static String[] BOOLEAN_OPTIONS = new String[]{"bold", "italic", "underlined", "strikethrough", "obfuscated"};
    private final static String[] COLORS = new String[]{"black","dark_blue","dark_green","dark_aqua","dark_red","dark_purple","gold","gray","dark_gray","blue","green","aqua","red","light_purple","yellow","white"};
    private final static String[] CLICK_ACTIONS = new String[]{"open_url", "open_file", "run_command", "suggest_command"};

    public static String parseXML(InputStream xml) throws Exception {
        DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
        Document parse = documentBuilder.parse(xml);
        Element root = parse.getDocumentElement();

        if (!root.getTagName().equals("message"))
            throw new JsonChatParseException("The root node is not a message! Start your xml with <message>!");
        List<Element> t = getChildrenElementsOfName(root, "t");
        if (t.size() != 0) {
            return parseContent(t).toString();
        }
        throw new JsonChatParseException("You did not specify a message!");
    }

    private static JSONObject parseContent(List<Element> elements) throws JsonChatParseException {
        JSONObject rootObject = new JSONObject();
        boolean first = false;
        for (Element item : elements) {
            JSONObject nodeObject = parseContent(item);
            if (!first) {
                rootObject = nodeObject;
                first = true;
            }
            else {
                Object extraRaw = rootObject.get("extra");
                if (extraRaw == null) {
                    extraRaw = new JSONArray();
                    rootObject.put("extra", extraRaw);
                }
                ((JSONArray) extraRaw).add(nodeObject);
            }
        }
        return rootObject;
    }

    private static List<Element> getChildrenElementsOfName(Element parent, String name) {
        List<Element> elements = new ArrayList<>();
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) item;
            if (!elem.getTagName().equals(name)) continue;
            elements.add(elem);
        }
        return elements;
    }

    /**
     * Returns a json object for a specific text node
     * @param textNode The text node
     * @return The json object
     */
    private static JSONObject parseContent(Element textNode) throws JsonChatParseException {
        JSONObject elementContent = new JSONObject();
        elementContent.put("text", getFirstLevelTextContent(textNode));

        for (String booleanOption : BOOLEAN_OPTIONS) {
            if (!textNode.hasAttribute(booleanOption)) continue;
            elementContent.put(booleanOption, Boolean.valueOf(textNode.getAttribute(booleanOption)));
        }

        String color = null;
        if (textNode.hasAttribute("color"))
            color = textNode.getAttribute("color").toLowerCase();
        if (color == null || !contains(COLORS, color))
            color = "white";
        elementContent.put("color", color);

        NodeList childNodes = textNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) continue;
            Element element = (Element) item;
            switch (element.getTagName()) {
                case "hover":
                    elementContent.put("hoverEvent", parseHoverEvent(element));
                    break;
                case "click":
                    elementContent.put("clickEvent", parseClickEvent(element));
                    break;
                default:
                    throw new JsonChatParseException("Unknown tag type " + element.getTagName());
            }
        }
        return elementContent;
    }

    private static String getFirstLevelTextContent(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < list.getLength(); ++i) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                textContent.append(child.getTextContent());
        }
        return textContent.toString();
    }

    private static JSONObject parseHoverEvent(Element element) throws JsonChatParseException {
        JSONObject hoverEvent = new JSONObject();
        String action = null;
        Object value = null;
        List<Element> childTs = getChildrenElementsOfName(element, "t"),
                childItem = getChildrenElementsOfName(element, "item"),
                childAchievement = getChildrenElementsOfName(element, "achievement");

        if (childTs.size() != 0) {
            action = "text";
            value = parseContent(childTs);
        }
        else if (childItem.size() == 1) {
//            action = "item";
//            Element item = childItem.get(0);
            //todo
            throw new JsonChatParseException("Cannot handle items at the moment, stay tuned!");
        }
        else if (childAchievement.size() == 1) {
            action = "achievement";
            value = getFirstLevelTextContent(childAchievement.get(0));
        }

        if (value != null) {
            hoverEvent.put("action", action);
            hoverEvent.put("value", value);
            return hoverEvent;
        }
        throw new JsonChatParseException("Invalid hover event specified!");
    }

    private static JSONObject parseClickEvent(Element element) throws JsonChatParseException {
        JSONObject jsonObject = new JSONObject();
        String action;
        if (!element.hasAttribute("action") || !contains(CLICK_ACTIONS, action = element.getAttribute("action")))
            throw new JsonChatParseException("No (or invalid) action specified for click event!");

        jsonObject.put("action", action);
        jsonObject.put("value", getFirstLevelTextContent(element));
        return jsonObject;
    }

    //despite this already being in GeneralUtils, I want this class to be self contained for portability
    private static <T> boolean contains(T[] array, T obj) {
        for (T t : array)
            if ((obj == null && t == null) || (obj != null && obj.equals(t)))
                return true;
        return false;
    }

    public static InputStream streamFrom(CharSequence sequence) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(sequence.toString().getBytes("UTF-8"));
    }
}
