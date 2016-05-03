package tech.rayline.core.jsonchat;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

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

    private static void assertJsonEquiv(String o1, String o2) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        assertEquals(jsonParser.parse(o1).toString(), jsonParser.parse(o2).toString());
    }

    private static String parseXML(String xml) throws Exception {
        return  XmlJsonChatConverter.parseXML(XmlJsonChatConverter.streamFrom(xml));
    }

}