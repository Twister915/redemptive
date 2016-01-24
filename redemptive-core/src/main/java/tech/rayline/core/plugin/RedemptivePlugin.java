package tech.rayline.core.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import rx.Observable;
import tech.rayline.core.command.CommandMeta;
import tech.rayline.core.command.RDCommand;
import tech.rayline.core.inject.Injector;
import tech.rayline.core.parse.ReadOnlyResource;
import tech.rayline.core.parse.ResourceFile;
import tech.rayline.core.parse.ResourceFileGraph;
import tech.rayline.core.rx.ConcurrencyMode;
import tech.rayline.core.rx.EventStreamer;
import tech.rayline.core.rx.PeriodicPlayerStreamer;
import tech.rayline.core.rx.RxBukkitScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Represents a {@link org.bukkit.plugin.Plugin} extension using all that redemptive has to offer
 */
@Getter
public abstract class RedemptivePlugin extends JavaPlugin {
    private Formatter formatter;
    private RxBukkitScheduler syncScheduler, asyncScheduler;
    private EventStreamer eventStreamer;
    private PeriodicPlayerStreamer playerStreamer;
    private ResourceFileGraph resourceFileGraph;

    @ResourceFile(raw = true, filename = "formats.yml") @ReadOnlyResource private YAMLConfigurationFile formatsFile;

    @Getter(AccessLevel.NONE) private Object[] injected;

    //"abstract" methods
    protected void onModuleEnable() throws Exception {}
    protected void onModuleDisable() throws Exception {}

    //plugin stuff
    @Override
    public final void onEnable() {
        try {
            //init rx
            syncScheduler = new RxBukkitScheduler(this, ConcurrencyMode.SYNC);
            asyncScheduler = new RxBukkitScheduler(this, ConcurrencyMode.ASYNC);
            eventStreamer = new EventStreamer(this, syncScheduler, asyncScheduler);

            //inject
            injected = Injector.injectTo(this);

            //resource file graph
            resourceFileGraph = new ResourceFileGraph(this);
            resourceFileGraph.hookToObject(this);
            resourceFileGraph.writeDefaults();
            resourceFileGraph.loadAll();

            //init files
            if (!getClass().isAnnotationPresent(NoConfig.class))
                saveDefaultConfig();

            if (getClass().isAnnotationPresent(UsesFormats.class))
                formatter = new Formatter(formatsFile);

            else formatter = null;

            onModuleEnable();
        } catch (Throwable t) {
            getLogger().severe("Unable to properly enable this plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            t.printStackTrace();
        }
    }

    @Override
    public final void onDisable() {
        try {
            onModuleDisable();

            Injector.handleDisable(injected);
            injected = null;

            resourceFileGraph.saveAll();
        } catch (Throwable t) {
            getLogger().severe("Unable to properly disable this plugin!");
            t.printStackTrace();
        }
    }

    public final Formatter.FormatBuilder formatAt(String key) {
        return getFormatter().begin(key);
    }

    public void saveAll() {
        resourceFileGraph.saveAll();
    }

    public void saveResourcesFor(Object object) {
        resourceFileGraph.saveAll(object);
    }

    public void loadResourcesFor(Object object) {
        resourceFileGraph.hookToObject(object);
        resourceFileGraph.writeDefaultsFor(object);
        resourceFileGraph.loadFor(object);
    }

    //register commands
    public final <T extends RDCommand> T registerCommand(T command) {
        PluginCommand command1;
        try {
            Constructor commandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            commandConstructor.setAccessible(true);
            command1 = (PluginCommand) commandConstructor.newInstance(command.getName(), this);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not register command " + command.getName());
        }
        command1.setExecutor(command); //Set the exectuor
        command1.setTabCompleter(command); //Tab completer
        CommandMeta annotation = command.getClass().getAnnotation(CommandMeta.class); //Get the commandMeta
        if (annotation != null){
            command1.setAliases(Arrays.asList(annotation.aliases()));
            command1.setDescription(annotation.description());
            command1.setUsage(annotation.usage());
        }
        CommandMap commandMap;
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(pluginManager);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not register command " + command.getName());
        }
        commandMap.register(this.getDescription().getName(), command1); //Register it with Bukkit
        return command;
    }

    @Deprecated public final void regsiterCommand(RDCommand... commands) {
        for (RDCommand command : commands) registerCommand(command);
    }

    public final void registerCommand(RDCommand... commands) {
        for (RDCommand command : commands) registerCommand(command);
    }

    //listeners
    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(Class<? extends T>... events) {
        return eventStreamer.observeEvent(events);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, Class<? extends T>... events) {
        return eventStreamer.observeEvent(priority, events);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, boolean ignoreCancelled, Class<? extends T>... events) {
        return eventStreamer.observeEvent(priority, ignoreCancelled, events);
    }

    public <T extends Listener> T registerListener(T listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }
}
