package tech.rayline.core.rx;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

public final class PeriodicPlayerStreamer extends BaseStreamer {
    public PeriodicPlayerStreamer(Plugin plugin, RxBukkitScheduler syncScheduler, RxBukkitScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    public final Observable<Player> observePlayerEvery(Player player, long time, TimeUnit unit) {
        //create an observer which will...
        return Observable
                .create(new Observable.OnSubscribe<Player>() {
                    @Override
                    public void call(Subscriber<? super Player> subscriber) {
                        //start immediately
                        subscriber.onStart();

                        //create a worker which can run our stuff:
                        Scheduler.Worker worker = syncScheduler.createWorker();
                        //scheduling a task which...
                        worker.schedulePeriodically(() -> {
                            //attempts to emit a player, catch and reporting ALL exceptions
                            try {
                                subscriber.onNext(player);
                            } catch (Throwable e) {
                                try {
                                    worker.unsubscribe();
                                } finally {
                                    Exceptions.throwOrReport(e, subscriber);
                                }
                            }
                        }, 0, time, unit);

                        //and unsubscribing the worker with the subscription
                        subscriber.add(Subscriptions.create(worker::unsubscribe));
                    }
            })
            //keep this up while the player is online
            .takeWhile(OfflinePlayer::isOnline)
            //toss in some of the transformer sauce
            .compose(getSyncTransformer());
    }
}
