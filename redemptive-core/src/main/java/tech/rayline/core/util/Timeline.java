package tech.rayline.core.util;

import lombok.Data;
import rx.Scheduler;
import rx.functions.Action0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Timeline {
    private final List<TimelineNode> nodes = new ArrayList<>();
    private Scheduler.Worker worker;

    public static TimelineBuilder create() {
        return new TimelineBuilder();
    }

    public void addAt(Runnable runnable, long time, TimeUnit unit) {
        ensureNotPlaying();
        nodes.add(new TimelineNode(runnable, unit.toMillis(time)));
    }

    public void playLoop(final Scheduler scheduler) {
        play(scheduler, new Action0() {
            @Override
            public void call() {
                clearWorker();
                playLoop(scheduler);
            }
        });
    }

    public void playOnce(Scheduler scheduler) {
        play(scheduler, new Action0() {
            @Override
            public void call() {
                clearWorker();
            }
        });
    }

    public void play(Scheduler scheduler, Action0 endHandler) {
        ensureNotPlaying();
        worker = scheduler.createWorker();
        Collections.sort(nodes, new Comparator<TimelineNode>() {
            @Override
            public int compare(TimelineNode o1, TimelineNode o2) {
                return (int) (o2.msAfter - o1.msAfter);
            }
        });
        for (TimelineNode node : nodes)
            worker.schedule(node, node.msAfter, TimeUnit.MILLISECONDS);
        long endTime = nodes.get(nodes.size() - 1).msAfter;
        worker.schedule(endHandler, endTime, TimeUnit.MILLISECONDS);
    }

    public boolean isPlaying() {
        return worker != null;
    }

    public void stop() {
        if (!isPlaying())
            throw new IllegalStateException("We're not currently playing this timeline!");

        worker.unsubscribe();
        clearWorker();
    }

    private void clearWorker() {
        worker = null;
    }

    private void ensureNotPlaying() {
        if (isPlaying())
            throw new IllegalStateException("We're currently playing the timeline!");
    }

    @Data public static final class TimelineNode implements Action0 {
        private final Runnable runnable;
        private final long msAfter;

        @Override
        public void call() {
            runnable.run();
        }
    }

    public static final class TimelineBuilder {
        private final Timeline building = new Timeline();
        private long msIn = 0;

        public TimelineBuilder then(Runnable runnable, long time, TimeUnit later) {
            return at(runnable, (msIn += later.toMillis(time)), TimeUnit.MILLISECONDS);
        }

        public TimelineBuilder at(Runnable runnable, long absoluteTime, TimeUnit later) {
            building.addAt(runnable, absoluteTime, later);
            return this;
        }

        public Timeline get() {
            return building;
        }
    }
}
