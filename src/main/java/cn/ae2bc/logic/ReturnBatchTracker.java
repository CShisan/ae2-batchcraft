package cn.ae2bc.logic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Tracks one batch of concurrently accepted tasks that use the same pattern. */
public final class ReturnBatchTracker<K, P> {
    public static final int MAX_TASKS = 64;

    private P pattern;
    private ReturnMode mode;
    private int taskCount;
    private final Map<K, Long> declaredOutputs = new LinkedHashMap<>();
    private K primaryKey;
    private long expectedPrimary;

    public boolean isActive() {
        return mode != null && mode != ReturnMode.UNBLOCKED && taskCount > 0;
    }

    public boolean canPotentiallyAccept(ReturnMode configuredMode) {
        return !isActive() || pattern != null && mode == configuredMode && taskCount < MAX_TASKS;
    }

    public boolean canAccept(P candidatePattern, ReturnMode configuredMode, Map<K, Long> outputs,
                             K candidatePrimaryKey, long candidatePrimaryAmount) {
        if (!isActive()) {
            return configuredMode == ReturnMode.UNBLOCKED
                    || canAdd(outputs, candidatePrimaryKey, candidatePrimaryAmount);
        }
        return pattern != null
                && pattern.equals(candidatePattern)
                && mode == configuredMode
                && taskCount < MAX_TASKS
                && declaredOutputs.equals(outputs)
                && Objects.equals(primaryKey, candidatePrimaryKey)
                && canAdd(outputs, candidatePrimaryKey, candidatePrimaryAmount);
    }

    public boolean begin(P candidatePattern, ReturnMode configuredMode, Map<K, Long> outputs,
                         K candidatePrimaryKey, long candidatePrimaryAmount) {
        if (!canAccept(candidatePattern, configuredMode, outputs, candidatePrimaryKey, candidatePrimaryAmount)) {
            return false;
        }
        if (configuredMode == ReturnMode.UNBLOCKED && !isActive()) {
            return true;
        }
        if (!isActive()) {
            pattern = candidatePattern;
            mode = configuredMode;
            taskCount = 0;
            declaredOutputs.clear();
            declaredOutputs.putAll(outputs);
            primaryKey = candidatePrimaryKey;
            expectedPrimary = 0;
        }
        expectedPrimary += candidatePrimaryAmount;
        taskCount++;
        return true;
    }

    public void rollback(long primaryAmount) {
        if (!isActive()) {
            return;
        }
        expectedPrimary = Math.max(0, expectedPrimary - primaryAmount);
        taskCount--;
        if (taskCount <= 0) {
            clear();
        }
    }

    public long filter(K key, long amount) {
        if (amount <= 0) {
            return 0;
        }
        if (!isActive()) {
            return amount;
        }
        return declaredOutputs.containsKey(key) ? amount : 0;
    }

    public boolean returned(K key, long amount) {
        if (!isActive() || amount <= 0) {
            return false;
        }
        if (Objects.equals(primaryKey, key)) {
            expectedPrimary = Math.max(0, expectedPrimary - amount);
        }
        boolean completed = expectedPrimary <= 0;
        if (completed) {
            clear();
        }
        return completed;
    }

    public void load(ReturnMode loadedMode, P loadedPattern, int loadedTaskCount,
                     Map<K, Long> loadedDeclaredOutputs, K loadedPrimaryKey, long loadedExpectedPrimary) {
        clear();
        if (loadedMode == null || loadedMode == ReturnMode.UNBLOCKED || loadedTaskCount <= 0
                || loadedDeclaredOutputs.isEmpty() || loadedPrimaryKey == null || loadedExpectedPrimary <= 0) {
            return;
        }
        mode = loadedMode;
        pattern = loadedPattern;
        taskCount = Math.min(loadedTaskCount, MAX_TASKS);
        declaredOutputs.putAll(loadedDeclaredOutputs);
        primaryKey = loadedPrimaryKey;
        expectedPrimary = loadedExpectedPrimary;
    }

    public void clear() {
        pattern = null;
        mode = null;
        taskCount = 0;
        declaredOutputs.clear();
        primaryKey = null;
        expectedPrimary = 0;
    }

    public P getPattern() {
        return pattern;
    }

    public ReturnMode getMode() {
        return mode;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public Map<K, Long> getDeclaredOutputs() {
        return Map.copyOf(declaredOutputs);
    }

    public K getPrimaryKey() {
        return primaryKey;
    }

    public long getExpectedPrimary() {
        return expectedPrimary;
    }

    private boolean canAdd(Map<K, Long> outputs, K candidatePrimaryKey, long candidatePrimaryAmount) {
        if (outputs.isEmpty() || candidatePrimaryKey == null || candidatePrimaryAmount <= 0) {
            return false;
        }
        if (expectedPrimary > Long.MAX_VALUE - candidatePrimaryAmount) {
            return false;
        }
        for (var entry : outputs.entrySet()) {
            long amount = entry.getValue();
            if (entry.getKey() == null || amount <= 0) {
                return false;
            }
        }
        return true;
    }
}
