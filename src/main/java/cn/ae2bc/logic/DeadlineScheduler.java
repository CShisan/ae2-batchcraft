package cn.ae2bc.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Keeps one live deadline per identity and discards replaced queue entries lazily. */
final class DeadlineScheduler<T> {
    private static final int COMPACTION_SLACK = 64;

    private final Map<T, Ticket<T>> scheduled = new IdentityHashMap<>();
    private final PriorityQueue<Ticket<T>> queue = new PriorityQueue<>(
            Comparator.<Ticket<T>>comparingLong(Ticket::deadline)
                    .thenComparingLong(Ticket::sequence));
    private long sequence;

    void schedule(T value, long deadline) {
        var ticket = new Ticket<>(value, deadline, sequence++);
        scheduled.put(value, ticket);
        queue.add(ticket);
        compactIfNeeded();
    }

    void cancel(T value) {
        scheduled.remove(value);
        compactIfNeeded();
    }

    boolean isScheduled(T value) {
        return scheduled.containsKey(value);
    }

    List<T> takeDue(long now) {
        if (queue.isEmpty() || queue.peek().deadline() > now) {
            return List.of();
        }

        var due = new ArrayList<T>();
        while (!queue.isEmpty() && queue.peek().deadline() <= now) {
            var ticket = queue.remove();
            if (scheduled.remove(ticket.value(), ticket)) {
                due.add(ticket.value());
            }
        }
        return due;
    }

    private void compactIfNeeded() {
        if (queue.size() <= scheduled.size() * 4 + COMPACTION_SLACK) {
            return;
        }
        queue.clear();
        queue.addAll(scheduled.values());
    }

    private record Ticket<T>(T value, long deadline, long sequence) {
    }
}
