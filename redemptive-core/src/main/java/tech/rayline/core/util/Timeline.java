package tech.rayline.core.util;

import lombok.Data;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Timeline {
    private final List<TimelineNode> nodes = new ArrayList<>();

    public static TimelineBuilder create() {
        return new TimelineBuilder();
    }

    public void addAt(Runnable runnable, long time, TimeUnit unit) {
        nodes.add(new TimelineNode(runnable, unit.toMillis(time)));
    }

    public Subscription play(Scheduler scheduler, Action0 endHandler) {
        Scheduler.Worker worker = scheduler.createWorker();
        Collections.sort(nodes, new Comparator<TimelineNode>() {
            @Override
            public int compare(TimelineNode o1, TimelineNode o2) {
                return (int) (o1.msAfter - o2.msAfter);
            }
        });
        for (TimelineNode node : nodes)
            worker.schedule(node, node.msAfter, TimeUnit.MILLISECONDS);
        long endTime = nodes.get(nodes.size() - 1).msAfter;
        worker.schedule(endHandler, endTime, TimeUnit.MILLISECONDS);
        return worker;
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
