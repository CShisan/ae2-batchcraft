package cn.ae2bc.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/** Deduplicates return producers and releases them after the first real network progress. */
public final class ReturnProgressListeners {
    private final Set<Runnable> listeners = Collections.newSetFromMap(new WeakHashMap<>());

    public void register(Runnable listener, boolean hasStoredStacks, Runnable wakeHandler) {
        if (hasStoredStacks && listeners.add(listener)) {
            wakeHandler.run();
        }
    }

    public void unregister(Runnable listener) {
        listeners.remove(listener);
    }

    public List<Runnable> takeAfterProgress(boolean progressed) {
        if (!progressed || listeners.isEmpty()) {
            return List.of();
        }
        var result = new ArrayList<>(listeners);
        listeners.clear();
        return result;
    }
}
