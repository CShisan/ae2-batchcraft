package cn.ae2bc.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtractionRecoveryPlanTest {
    @Test
    void conservesEveryExtractedItem() {
        for (int extracted = 0; extracted <= 64; extracted++) {
            for (int inserted = -2; inserted <= 66; inserted++) {
                ExtractionRecoveryPlan plan = ExtractionRecoveryPlan.create(extracted, inserted);
                assertEquals(extracted, plan.insertedCount() + plan.remainderCount());
            }
        }
    }

    @Test
    void reportsTheRejectedRemainder() {
        assertEquals(new ExtractionRecoveryPlan(2, 2), ExtractionRecoveryPlan.create(4, 2));
    }
}
