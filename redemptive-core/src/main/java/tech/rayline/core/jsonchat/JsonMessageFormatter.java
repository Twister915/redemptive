package tech.rayline.core.jsonchat;

import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Data public final class JsonMessageFormatter {
    private final InputStream inputStream;
    private final Map<String, String> variables = new HashMap<String, String>();


    public String get() throws Exception {
        return XmlJsonChatConverter.parseXML(inputStream, variables);
    }

    public JsonMessageFormatter with(String key, Object value) {
        variables.put(key, value.toString());
        return this;
    }

    public JsonMessageFormatter withModifier(String key, Object value) {
        return with(key, value);
    }
}
