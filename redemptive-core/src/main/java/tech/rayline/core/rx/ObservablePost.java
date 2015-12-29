package tech.rayline.core.rx;

import lombok.Synchronized;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

import java.util.HashSet;
import java.util.Set;

/**
 * This represents something that can churn out {@link rx.Observable}s and lets you post objects to the subscribers
 */
public final class ObservablePost<T> {
    private final Set<Subscriber<? super T>> subscribers = new HashSet<>();

    @Synchronized
    public void post(T obj) {
        subscribers.forEach(subscriber -> {
            try {
                subscriber.onNext(obj);
            } catch (Throwable t) {
                Exceptions.throwOrReport(t, subscriber);
            }
        });
    }

    @Synchronized
    public void complete() {
        subscribers.forEach(Observer::onCompleted);
        subscribers.clear();
    }

    @Synchronized
    public Observable<T> observe() {
        return Observable.create(subscriber -> {
            subscriber.add(Subscriptions.create(() -> removeSubscriber(subscriber)));
            subscribers.add(subscriber);
        });
    }

    @Synchronized
    private void removeSubscriber(Subscriber<? super T> subscriber) {
        subscribers.remove(subscriber);
    }
}
