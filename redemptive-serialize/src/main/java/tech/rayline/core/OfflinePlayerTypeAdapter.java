package tech.rayline.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.UUID;

public final class OfflinePlayerTypeAdapter extends TypeAdapter<OfflinePlayer> {
    @Override
    public void write(JsonWriter out, OfflinePlayer value) throws IOException {
        out.value(value.getUniqueId().toString());
    }

    @Override
    public OfflinePlayer read(JsonReader in) throws IOException {
        return Bukkit.getOfflinePlayer(UUID.fromString(in.nextString()));
    }
}
