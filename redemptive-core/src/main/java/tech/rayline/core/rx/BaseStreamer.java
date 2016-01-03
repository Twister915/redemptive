package tech.rayline.core.rx;

import lombok.Data;
import org.bukkit.plugin.Plugin;
import rx.Observable;

@Data
public abstract class BaseStreamer {
    protected final Plugin plugin;
    protected final RxBukkitScheduler syncScheduler, asyncScheduler;

    public <T> Observable.Transformer<T, T> getSyncTransformer() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.subscribeOn(syncScheduler);
            }
        };
    }

    public <T> Observable.Transformer<T, T> getAsyncTransformer() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.subscribeOn(asyncScheduler);
            }
        };
    }
}
