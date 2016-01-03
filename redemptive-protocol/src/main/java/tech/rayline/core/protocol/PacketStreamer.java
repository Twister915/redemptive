package tech.rayline.core.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import tech.rayline.core.inject.Injectable;
import tech.rayline.core.inject.InjectionProvider;
import tech.rayline.core.plugin.RedemptivePlugin;
import tech.rayline.core.rx.BaseStreamer;
import tech.rayline.core.rx.RxBukkitScheduler;

@Injectable
public final class PacketStreamer extends BaseStreamer {
    @InjectionProvider
    public PacketStreamer(RedemptivePlugin redemptivePlugin) {
        this(redemptivePlugin, redemptivePlugin.getSyncScheduler(), redemptivePlugin.getAsyncScheduler());
    }

    public PacketStreamer(Plugin plugin, RxBukkitScheduler syncScheduler, RxBukkitScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    public Observable<PacketEvent> getPacketObservable(PacketType... packets) {
        return getPacketObservable(ListenerPriority.NORMAL, packets);
    }

    public Observable<PacketEvent> getPacketObservable(final ListenerPriority listenerPriority, final PacketType... packets) {
        //create an observable which
        return Observable.create(new Observable.OnSubscribe<PacketEvent>() {
            @Override
            public void call(final Subscriber<? super PacketEvent> subscriber) {
                //creates a packet adapter that forwards all packets collected to the subscriber
                final PacketAdapter packetAdapter = new PacketAdapter(plugin, listenerPriority, packets) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        try {
                            subscriber.onNext(event);
                        } catch (Throwable t) {
                            Exceptions.throwOrReport(t, subscriber);
                        }
                    }
                };
                //registers said adapter
                ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
                //and unregisters the adapter on unsubscription
                subscriber.add(Subscriptions
                        .create(new Action0() {
                                    @Override
                                    public void call() {
                                        ProtocolLibrary
                                                .getProtocolManager()
                                                .removePacketListener(packetAdapter);
                                    }
                                }
                        )
                );
            }
        }).compose(this.<PacketEvent>getSyncTransformer());
    }
}
