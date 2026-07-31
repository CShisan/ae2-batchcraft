package cn.ae2bc.logic;

record ExtractionRecoveryPlan(int insertedCount, int remainderCount) {
    static ExtractionRecoveryPlan create(int extractedCount, int insertedCount) {
        int normalizedExtracted = Math.max(0, extractedCount);
        int normalizedInserted = Math.clamp(insertedCount, 0, normalizedExtracted);
        return new ExtractionRecoveryPlan(normalizedInserted, normalizedExtracted - normalizedInserted);
    }
}
