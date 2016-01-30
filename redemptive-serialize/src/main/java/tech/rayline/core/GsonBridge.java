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
import tech.rayline.core.library.MavenLibrary;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.lang.reflect.Modifier;

@Getter
@Injectable(libraries = {@MavenLibrary("com.google.code.gson:gson:2.5")})
public final class GsonBridge {
    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .registerTypeHierarchyAdapter(Location.class, new LocationTypeAdapter())
            .registerTypeHierarchyAdapter(OfflinePlayer.class, new OfflinePlayerTypeAdapter())
            .registerTypeHierarchyAdapter(World.class, new WorldTypeAdapter())
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    @InjectionProvider
    public GsonBridge(RedemptivePlugin redemptivePlugin) {}
    public GsonBridge() {}
}
