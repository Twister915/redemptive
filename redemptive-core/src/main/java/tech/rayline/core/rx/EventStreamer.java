package tech.rayline.core.rx;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

public final class EventStreamer extends BaseStreamer {
    public EventStreamer(Plugin plugin, RxBukkitScheduler syncScheduler, RxBukkitScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(Class<? extends T>... events) {
        return observeEvent(EventPriority.NORMAL, events);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, Class<? extends T>... events) {
        return observeEvent(priority, false, events);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, boolean ignoreCancelled, Class<? extends T>... events) {
        //creates an observer which...
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                //creates an empty listener
                Listener listener = new Listener() {};
                //creates an event executor
                @SuppressWarnings("unchecked")
                EventExecutor executor = (listener1, event) -> {
                    //check to make sure the event bukkit sent us can apply to what the observer is expecting (type T)
                    //strangely, they'll send things like the EntityDamageEvent when we want to cast to EntityDamageByEntity
                    //so it's best if we manually check here to make sure it can apply to any class
                    boolean canAssign = false; //track a "canAssign" value
                    Class<? extends Event> eventClass = event.getClass(); //get the class of the event
                    for (Class<? extends T> aClass : events) { //go through all classes we're concerned with emitting (T is their mutual supertype)
                        if (aClass.isAssignableFrom(eventClass)) { //if one of the classes (from the events classes arg) is assignable to the event class (is a superclass or equal to)
                            canAssign = true; //then we're good, and can break
                            break;
                        }
                    }

                    //if we never discovered a class which is assignable, this is one of those weird and rare cases where Bukkit is stupid
                    if (!canAssign)
                        return; //so we return

                    try {
                        subscriber.onNext((T) event);
                    } catch (Throwable t) {
                        Exceptions.throwOrReport(t, subscriber);
                    }
                };

                //registers all the event types to that listener
                PluginManager pluginManager = Bukkit.getPluginManager();
                for (Class<? extends T> event : events)
                    pluginManager.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);

                //and registers a HandlerList.unregisterAll call as the unsubscribe action
                subscriber.add(Subscriptions.create(() -> HandlerList.unregisterAll(listener)));

                //also needs to unsubscribe when the plugin disables
                pluginManager.registerEvent(PluginDisableEvent.class, listener, EventPriority.MONITOR, (l, event) -> {
                    PluginDisableEvent disableEvent = (PluginDisableEvent) event;
                    if (disableEvent.getPlugin().equals(plugin))
                        subscriber.onCompleted();

                }, plugin, false);
            }
        }).compose(getSyncTransformer());
    }
}
