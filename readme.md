# Redemptive Core

Make your plugins simpler.

## Features

The following features are provided by the Redemptive library:

* Full featured command library
* Scoreboard system (WIP)
* Inventory GUI
* Controlled inventory (hotbar buttons)
* Bean -> File serialization through annotated fields
* Formats system (formats.yml)
* ReactiveX event handling
* Shorthands for common tasks (ItemShorthand, RunnableShorthand)
* Extremely basic geometric data structures (Region, Point)
* Timeline (for sequencing runnables)
* Dependency downloading from maven repos and injection of dependencies at runtime 
* Injectable component libraries (redemptive-serialize providing GSON, redemptive-sql providing HikariCP, redemptive-persist providing Morphia)
* Serializable player state encapsulation

An overview of a few of the key systems in Redemptive

## Important differences

* We use onModuleEnable, not onEnable
* You may throw any exception from onModuleEnable, it will simply disable the plugin
* You do not need to register commands in plugin.yml so long as they are redemptive commands registered by the plugin during runtime (using registerCommand)
* Listeners are exceptionally rare, being replaced by the ReactiveX event handling system.

## Commands

Below is an example command-

```
public final class CancelCommand extends RedemptiveCommand {
    public CancelCommand() {
        super("cancel");
    }

    @Override
    protected void handleCommand(Player player, String[] args, BlockRecorder recorder) throws CommandException {
        player.sendMessage(Animatic.getInstance().formatAt("recording.cancelled").get());
        Animatic.getInstance().clearRecorder(player);
    }
}
```

Registration:

```
registerCommand(new CancelCommand());
```

You can also use a RedemptiveCommand as a subcommand to another RedemptiveCommand
```
@CommandPermission("animatic.any")
public final class AnimaticCommand extends BaseAnimaticCommand {
    public AnimaticCommand() {
        super("animatic", new RecordCommand(), new PlayerCommand("player"), new ReloadCommand());
    }
}
```

Note, that this principal applies recursively. You can make a subcommand itself have a subcommand.

All subcommands are automatically tab-completable for those that have permission (checks for this are done through @CommandPermission, you have no obligation to use this, but for "vanilla redemptive" permission checks, like inbuilt tab complete, you must use this annotation)

All errors should be handled through the use of exceptions. You may handle exceptions thrown from a command in a general sense by overriding this method

```
protected void handleCommandException(CommandException ex, String[] args, CommandSender sender)
```

If there is an unhandled exception that does not subclass CommandException, then you will receive the hilariously named **UnhandledCommandExceptionException**

## ReactiveX event handling
 
 ReactiveX is documented here https://www.spigotmc.org/threads/rxbukkit-a-new-event-philosphy.115344/
 
 You may observe events using the methods on your plugin, all of which are named observeEvent.
 
 
## Formats System

You may specify a set of "formats" for a plugin as to not include any message strings in your source.

The following is an example of "getting" a format with variables

```
player.sendMessage(instance.formatAt("players.list-diffworld-line").withModifier("name", staticPlayer.getName()).get());
```

You must add
```
@UsesFormats
```
to your plugin type to enable this feature.

Note, there is a special key: ```prefix``` which can be added to your formats.yml and will always be prepended to formats. You can omit the prefix setting in a specific format (say, for an action bar message) by using ```withPrefix(false)``` while building your format.
 
## Dependency Downloading and Loading
You may annotate your plugin with the following annotation: 
```
@MavenLibraries({@MavenLibrary("io.reactivex:rxjava:1.0.16")})
```

*Note* This library (reactivex) is already downloaded for every plugin.

The only library you are required to shade into your final plugin jar is redemptive-core itself. 

You may also specify a repository to download the dependency from through the property _repo_ on the _MavenLibrary_ annotation

If you wish to shade dependencies manually, add the ```@IgnoreLibraries``` annotation and all superclasses will be ignored.

## Component Injection

This feature is designed as a fix for carrying around code you don't need in your jar. If you don't need gson, don't use it. Inject the component library if you do need it.

Your first step is to have the dependency for your component be present at runtime. This may either be accomplished via the dependency downloading system, or by simply shading the component in (far more common for the redemptive-* components).

Then, simply add a field to your plugin class that looks like this
```
@Inject private GsonBridge bridge
```

Where, in this case, we are injecting the GsonBridge from redemptive-serialize. The field will be non-null (assuming no exceptions) before your onModuleEnable is called.