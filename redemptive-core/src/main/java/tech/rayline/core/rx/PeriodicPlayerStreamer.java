package tech.rayline.core.rx;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

public final class PeriodicPlayerStreamer extends BaseStreamer {
    public PeriodicPlayerStreamer(Plugin plugin, RxBukkitScheduler syncScheduler, RxBukkitScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    public final Observable<Player> observePlayerEvery(final Player player, final long time, final TimeUnit unit) {
        //create an observer which will...
        return Observable
                .create(new Observable.OnSubscribe<Player>() {
                    @Override
                    public void call(final Subscriber<? super Player> subscriber) {
                        //start immediately
                        subscriber.onStart();

                        //create a worker which can run our stuff:
                        final Scheduler.Worker worker = syncScheduler.createWorker();
                        //scheduling a task which...
                        worker.schedulePeriodically(new Action0() {
                            @Override
                            public void call() {
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
                            }
                        }, 0, time, unit);

                        //and unsubscribing the worker with the subscription
                        subscriber.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                worker.unsubscribe();
                            }
                        }));
                    }
            })
            //keep this up while the player is online
            .takeWhile(new Func1<Player, Boolean>() {
                @Override
                public Boolean call(Player player1) {
                    return player1.isOnline();
                }
            })
            //toss in some of the transformer sauce
        .compose(this.<Player>getSyncTransformer());
    }
}
