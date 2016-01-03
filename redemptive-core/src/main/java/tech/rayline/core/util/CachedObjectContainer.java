package tech.rayline.core.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.Optional;


@EqualsAndHashCode
@ToString
public class CachedObjectContainer<K> {
    private final Func0<K> provider;
    private final Func1<K, Boolean> checker;

    private K currentValue;

    public CachedObjectContainer(Func0<K> provider) {
        this(provider, new Func1<K, Boolean>() {
            @Override
            public Boolean call(K k) {
                return true;
            }
        });
    }

    public CachedObjectContainer(Func0<K> provider, Func1<K, Boolean> checker) {
        this.provider = provider;
        this.checker = checker;
    }

    public Optional<K> getValue() {
        attemptReconstruct();
        return Optional.ofNullable(currentValue);
    }

    /**
     * Returns true of the object that we are currently holding is "clean" or has not been broken or is not considered broken.
     * @return above
     */
    public boolean isCurrentObjectClean() {
        return currentValue != null && checker.call(currentValue);
    }

    public void breakObject() {
        currentValue = null;
        attemptReconstruct();
    }

    protected void attemptReconstruct() {
        if (!isCurrentObjectClean())
            currentValue = provider.call();
    }
}
