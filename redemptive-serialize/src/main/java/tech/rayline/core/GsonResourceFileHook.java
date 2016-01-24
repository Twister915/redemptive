package tech.rayline.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import tech.rayline.core.parse.ResourceFileHook;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public final class GsonResourceFileHook implements ResourceFileHook<JsonElement> {
    private final Gson gson = new GsonBridge().getGson();
    private final JsonParser jsonParser = new JsonParser();

    @Override
    public <T> T read(RedemptivePlugin plugin, File file, Class<T> type) throws Exception {
        return gson.fromJson(new FileReader(file), type);
    }

    @Override
    //warning- this method does not use any of the type adapters from this project
    public JsonElement readRaw(RedemptivePlugin plugin, File file) throws Exception {
        return jsonParser.parse(new FileReader(file));
    }

    @Override
    public void write(RedemptivePlugin plugin, Object o, File file) throws Exception {
        try (FileWriter fileWriter = new FileWriter(file)) {
            String s = gson.toJson(o);
            fileWriter.write(s);
        }
    }

    @Override
    public void writeRaw(RedemptivePlugin plugin, Object jsonElement, File file) throws Exception {
        FileWriter fileWriter = new FileWriter(file);
        try {
            fileWriter.write(jsonElement.toString());
        } finally {
            fileWriter.flush();
            fileWriter.close();
        }
    }
}
