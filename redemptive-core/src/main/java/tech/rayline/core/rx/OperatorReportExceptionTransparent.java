package tech.rayline.core.rx;

import lombok.Data;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Allows you to transparently handle an exception for logging purposes
 */
@Data
public final class OperatorReportExceptionTransparent<T> implements Observable.Operator<T, T> {
    /**
     * The action to perform with any exceptions, transparently.
     */
    private final Action1<Throwable> handler;
    private final Func1<Throwable, Boolean> filter;

    public static <T> OperatorReportExceptionTransparent<T> with(Action1<Throwable> action) {
        return new OperatorReportExceptionTransparent<>(action, null);
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(final Throwable e) {
                try {
                    if (filter != null && filter.call(e))
                        handler.call(e);
                } catch (Exception e1) {
                    Exceptions.throwOrReport(e1, subscriber);
                } finally {
                    subscriber.onError(e);
                }
            }

            @Override
            public void onNext(final T t) {
                subscriber.onNext(t);
            }
        };
    }
}
