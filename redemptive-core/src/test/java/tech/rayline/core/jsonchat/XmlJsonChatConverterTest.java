package tech.rayline.core.jsonchat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class XmlJsonChatConverterTest {
    @Test
    public void testBasicText() throws Exception {
        String s = parseXML("<message><t>Hey, yow you doing</t></message>");
        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"color\":\"white\",\"text\":\"Hey, yow you doing\"}],\"text\":\"\"}", s);
    }

    @Test
    public void testColoredBoldText() throws Exception {
        String s = parseXML("<message><t color=\"red\" bold=\"true\">Hey, yow you doing</t></message>");
        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"color\":\"red\",\"text\":\"Hey, yow you doing\",\"bold\":true}],\"text\":\"\"}", s);
    }

    @Test
    public void testClickEvent() throws Exception {
        String s = parseXML("<message><t color=\"green\">View More<click action=\"run_command\">/moreinfo {{data}}</click></t></message>");
        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"\\/moreinfo {{data}}\"},\"color\":\"green\",\"text\":\"View More\"}],\"text\":\"\"}", s);
    }

    @Test
    public void testHoverEvent() throws Exception {
        String s = parseXML("" +
                "<message>" +
                "<t color=\"red\" bold=\"true\">" +
                "[x]" +
                "<hover>" +
                "<t color=\"green\">kek</t>" +
                "<t color=\"red\" bold=\"true\">This user is banned</t>" +
                "</hover>" +
                "</t>" +
                "</message>");

        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"color\":\"white\",\"extra\":[{\"color\":\"green\",\"text\":\"kek\"},{\"color\":\"red\",\"text\":\"This user is banned\",\"bold\":true}],\"text\":\"\"}},\"color\":\"red\",\"text\":\"[x]\",\"bold\":true}],\"text\":\"\"}", s);
    }

    @Test
    public void testExtra() throws Exception {
        String s = parseXML("<message><t>Hey </t><t color=\"green\">how are you? </t><t bold=\"true\" color=\"green\">Click here for more info<click action=\"run_command\">/viewinfo {{msg-id}}</click></t></message>");
        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"color\":\"white\",\"text\":\"Hey \"},{\"color\":\"green\",\"text\":\"how are you? \"},{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"\\/viewinfo {{msg-id}}\"},\"color\":\"green\",\"text\":\"Click here for more info\",\"bold\":true}],\"text\":\"\"}", s);
    }

    @Test
    public void testNestedTNodes() throws Exception {
        String s = parseXML("<message><t>Hello <t color=\"blue\">Joe</t> how are you?</t></message>");
        assertJsonEquiv("{\"color\":\"white\",\"extra\":[{\"color\":\"white\",\"extra\":[{\"color\":\"blue\",\"text\":\"Joe\"},{\"text\":\" how are you?\"}],\"text\":\"Hello \"}],\"text\":\"\"}\n", s);
    }

    private static void assertJsonEquiv(String o1, String o2) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        assertEquals(orderJson(jsonParser.parse(o1)).toString(), orderJson(jsonParser.parse(o2)).toString());
    }

    private static Object orderJson(Object o) {
        if (o instanceof JSONArray) return orderJson(((JSONArray) o));
        else if (o instanceof JSONObject) return orderJson(((JSONObject) o));
        else return o;
    }

    private static JSONArray orderJson(JSONArray object) {
        JSONArray out = new JSONArray();
        for (int i = 0; i < object.size(); i++) {
            out.add(orderJson(object.get(i)));
        }
        return out;
    }

    private static JSONObject orderJson(JSONObject object) {
        List<String> keys = new ArrayList<>(object.keySet());
        Collections.sort(keys);
        JSONObject ret = new JSONObject();
        for (String key : keys) {
            ret.put(key, orderJson(object.get(key)));
        }
        return ret;
    }

    private static String parseXML(String xml) throws Exception {
        return XmlJsonChatConverter.parseXML(XmlJsonChatConverter.streamFrom(xml));
    }

}