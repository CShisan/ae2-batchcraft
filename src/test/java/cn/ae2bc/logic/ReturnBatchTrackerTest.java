package cn.ae2bc.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReturnBatchTrackerTest {
    @Test
    void idleNeverBlocksReturnsInAnyConfiguredMode() {
        var tracker = new ReturnBatchTracker<String, String>();

        assertEquals(12, tracker.filter("unrelated", 12));
    }

    @Test
    void acceptsOnlyTheSamePatternIntoAnActiveBatch() {
        var tracker = new ReturnBatchTracker<String, String>();
        var outputs = Map.of("result", 4L, "byproduct", 1L);

        assertTrue(tracker.begin("pattern-a", ReturnMode.STRICT, outputs, "result", 4));
        assertTrue(tracker.canAccept("pattern-a", ReturnMode.STRICT, outputs, "result", 4));
        assertFalse(tracker.canAccept("pattern-b", ReturnMode.STRICT, outputs, "result", 4));
        assertFalse(tracker.canAccept("pattern-a", ReturnMode.UNBLOCKED, outputs, "result", 4));
    }

    @Test
    void strictModeFiltersOnlyByDeclaredOutputType() {
        var tracker = new ReturnBatchTracker<String, String>();
        var outputs = Map.of("result", 4L, "byproduct", 1L);
        assertTrue(tracker.begin("pattern", ReturnMode.STRICT, outputs, "result", 4));
        assertTrue(tracker.begin("pattern", ReturnMode.STRICT, outputs, "result", 4));

        assertEquals(9, tracker.filter("result", 9));
        assertEquals(20, tracker.filter("byproduct", 20));
        assertEquals(0, tracker.filter("unrelated", 20));
        assertFalse(tracker.returned("byproduct", 20));
        assertEquals(20, tracker.filter("byproduct", 20));
        assertFalse(tracker.returned("result", 3));
        assertTrue(tracker.returned("result", 100));
        assertFalse(tracker.isActive());
        assertEquals(3, tracker.filter("unrelated", 3));
    }

    @Test
    void primaryOutputsFromMultipleMachineSlotsCompleteOneBatchCumulatively() {
        var tracker = new ReturnBatchTracker<String, String>();
        assertTrue(tracker.begin("pattern", ReturnMode.STRICT, Map.of("result", 3L), "result", 3));

        assertFalse(tracker.returned("result", 1));
        assertFalse(tracker.returned("result", 1));
        assertTrue(tracker.returned("result", 1));
        assertFalse(tracker.isActive());
    }

    @Test
    void capsOneBatchAtSixtyFourTasks() {
        var tracker = new ReturnBatchTracker<String, String>();
        var outputs = Map.of("result", 1L);
        for (int i = 0; i < ReturnBatchTracker.MAX_TASKS; i++) {
            assertTrue(tracker.begin("pattern", ReturnMode.STRICT, outputs, "result", 1));
        }

        assertEquals(ReturnBatchTracker.MAX_TASKS, tracker.getTaskCount());
        assertFalse(tracker.canAccept("pattern", ReturnMode.STRICT, outputs, "result", 1));
    }

    @Test
    void rollbackRemovesOnlyTheLastAdmission() {
        var tracker = new ReturnBatchTracker<String, String>();
        var outputs = Map.of("result", 2L);
        assertTrue(tracker.begin("pattern", ReturnMode.STRICT, outputs, "result", 2));
        assertTrue(tracker.begin("pattern", ReturnMode.STRICT, outputs, "result", 2));

        tracker.rollback(2);

        assertTrue(tracker.isActive());
        assertEquals(1, tracker.getTaskCount());
        assertEquals(2, tracker.getDeclaredOutputs().get("result"));
    }
}
