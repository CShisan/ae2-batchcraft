package cn.ae2bc.logic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

final class RecoveryBuffer<K> {
    private final Map<K, Long> amounts = new LinkedHashMap<>();

    boolean isEmpty() {
        return amounts.isEmpty();
    }

    void queue(K key, long amount) {
        if (key != null && amount > 0) {
            amounts.merge(key, amount, RecoveryBuffer::saturatingAdd);
        }
    }

    boolean drain(BiFunction<K, Long, Long> inserter) {
        boolean changed = false;
        for (var iterator = amounts.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            long inserted = Math.clamp(inserter.apply(entry.getKey(), entry.getValue()), 0, entry.getValue());
            if (inserted >= entry.getValue()) {
                iterator.remove();
                changed = true;
            } else if (inserted > 0) {
                entry.setValue(entry.getValue() - inserted);
                changed = true;
            }
        }
        return changed;
    }

    Iterable<Map.Entry<K, Long>> entries() {
        return amounts.entrySet();
    }

    void clear() {
        amounts.clear();
    }

    private static long saturatingAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
