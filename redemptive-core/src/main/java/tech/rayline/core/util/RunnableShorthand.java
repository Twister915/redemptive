package tech.rayline.core.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "forPlugin")
public final class RunnableShorthand {
    private final JavaPlugin plugin;
    private final List<Runnable> runnables = new ArrayList<>();
    private boolean async;
    private long delay;

    public BukkitTask repeat(long ticks) {
        BukkitRunnable bukkitRunnable = toRunnable();
        if (async)
            return bukkitRunnable.runTaskTimerAsynchronously(plugin, delay, ticks);
        else
            return bukkitRunnable.runTaskTimer(plugin, delay, ticks);
    }

    public BukkitTask later(long ticks) {
        BukkitRunnable bukkitRunnable = toRunnable();
        if (async)
            return bukkitRunnable.runTaskLaterAsynchronously(plugin, ticks + delay);
        else
            return bukkitRunnable.runTaskLater(plugin, ticks + delay);
    }

    public BukkitTask go() {
        BukkitRunnable bukkitRunnable = toRunnable();
        if (async)
            return bukkitRunnable.runTaskAsynchronously(plugin);
        else
            return bukkitRunnable.runTask(plugin);
    }

    private BukkitRunnable toRunnable() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                for (Runnable runnable : runnables)
                    runnable.run();
            }
        };
    }

    public RunnableShorthand async() {
        async = !async;
        return this;
    }

    public RunnableShorthand delay(long ticks) {
        delay += ticks;
        return this;
    }

    public RunnableShorthand resetDelay() {
        delay = 0;
        return this;
    }

    public RunnableShorthand with(Runnable runnable) {
        runnables.add(runnable);
        return this;
    }
}
