package tech.rayline.core.rx;

import lombok.Data;
import org.bukkit.plugin.Plugin;
import rx.Observable;

@Data
public abstract class BaseStreamer {
    protected final Plugin plugin;
    protected final RxBukkitScheduler syncScheduler, asyncScheduler;

    public <T> Observable.Transformer<T, T> getSyncTransformer() {
        return tObservable -> tObservable.subscribeOn(syncScheduler);
    }

    public <T> Observable.Transformer<T, T> getAsyncTransformer() {
        return tObservable -> tObservable.subscribeOn(asyncScheduler);
    }
}
