package tech.rayline.core.rx;

import lombok.Data;
import lombok.Delegate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import tech.rayline.core.util.RunnableShorthand;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = false)
@Data
public final class RxBukkitScheduler extends Scheduler {
    private final JavaPlugin plugin;
    private final ConcurrencyMode concurrencyMode;

    private BukkitTask actualSchedule(final Action0 action, int ticksDelay) {
        RunnableShorthand with = RunnableShorthand.forPlugin(plugin).with(new Runnable() {
            @Override
            public void run() {
                action.call();
            }
        });
        if (concurrencyMode == ConcurrencyMode.ASYNC)
            with.async();
        return with.later(ticksDelay);
    }

    @Override
    public Worker createWorker() {
        return new BukkitWorker();
    }

    private final class BukkitWorker extends Worker {
        @Delegate(types = Subscription.class)
        private final CompositeSubscription allSubscriptions = new CompositeSubscription();


        @Override
        public Subscription schedule(Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
            if (unit.toMillis(delayTime) == 0 && concurrencyMode == ConcurrencyMode.SYNC && Bukkit.getServer().isPrimaryThread()) {
                action.call();
                return Subscriptions.unsubscribed();
            }

            return new RxBukkitTaskSubscription(action, delayTime, unit, allSubscriptions);
        }
    }

    private final class RxBukkitTaskSubscription implements Subscription {
        private final BukkitTask handle;
        private final CompositeSubscription parent;
        private boolean unsubscribed;

        private RxBukkitTaskSubscription(final Action0 action, long time, TimeUnit unit, CompositeSubscription parent) {
            this.handle = actualSchedule(new Action0() {
                @Override
                public void call() {
                    action.call();
                    unsubscribed = true;
                }
            }, (int) Math.round((double) unit.toMillis(time) / 50D));
            this.parent = parent;

            parent.add(this);
        }

        @Override
        public void unsubscribe() {
            unsubscribed = true;
            handle.cancel();
            parent.remove(this);
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed;
        }
    }
}
