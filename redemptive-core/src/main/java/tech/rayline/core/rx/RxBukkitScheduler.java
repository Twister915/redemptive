package tech.rayline.core.rx;

import lombok.Data;
import lombok.Delegate;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import tech.rayline.core.util.RunnableShorthand;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = false)
@Data
public final class RxBukkitScheduler extends Scheduler {
    private final JavaPlugin plugin;
    private final ConcurrencyMode concurrencyMode;

    private BukkitTask actualSchedule(Action0 action, int ticksDelay) {
        RunnableShorthand with = RunnableShorthand.forPlugin(plugin).with(action::call);
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
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            BukkitTask bukkitTask = actualSchedule(action, (int) Math.round((double) unit.toMillis(delayTime) / 50D));
            ScheduledAction scheduledAction = new ScheduledAction(action, allSubscriptions);
            scheduledAction.add(Subscriptions.create(bukkitTask::cancel));
            return scheduledAction;
        }
    }
}
