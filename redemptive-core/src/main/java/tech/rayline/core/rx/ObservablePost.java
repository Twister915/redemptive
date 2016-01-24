package tech.rayline.core.rx;

import lombok.Synchronized;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import java.util.HashSet;
import java.util.Set;

/**
 * This represents something that can churn out {@link rx.Observable}s and lets you post objects to the subscribers
 */
public final class ObservablePost<T> {
    private final Set<Subscriber<? super T>> subscribers = new HashSet<>();

    @Synchronized
    public void post(final T obj) {
        for (Subscriber<? super T> subscriber : subscribers) {
            try {
                subscriber.onNext(obj);
            } catch (Throwable t) {
                Exceptions.throwOrReport(t, subscriber);
            }
        }

    }

    @Synchronized
    public void complete() {
        for (Subscriber<? super T> subscriber : subscribers)
            subscriber.onCompleted();
        subscribers.clear();
    }

    @Synchronized
    public Observable<T> observe() {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        ObservablePost.this.removeSubscriber(subscriber);
                    }
                }));
                subscribers.add(subscriber);
            }
        });
    }

    @Synchronized
    private void removeSubscriber(Subscriber<? super T> subscriber) {
        subscribers.remove(subscriber);
    }
}
