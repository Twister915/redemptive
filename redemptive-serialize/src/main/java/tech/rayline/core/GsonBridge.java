package tech.rayline.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import tech.rayline.core.inject.Injectable;
import tech.rayline.core.inject.InjectionProvider;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.lang.reflect.Modifier;

@Getter
@Injectable
public final class GsonBridge {
    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .registerTypeAdapter(Location.class, new LocationTypeAdapter())
            .registerTypeAdapter(OfflinePlayer.class, new OfflinePlayerTypeAdapter())
            .registerTypeAdapter(World.class, new WorldTypeAdapter())
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    @InjectionProvider
    public GsonBridge(RedemptivePlugin redemptivePlugin) {}
}
