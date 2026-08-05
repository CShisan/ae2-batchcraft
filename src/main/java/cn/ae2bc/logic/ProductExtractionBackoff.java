package cn.ae2bc.logic;

/** Computes the next extraction delay while bounding empty-machine polling. */
final class ProductExtractionBackoff {
    static final int MAX_IDLE_DELAY = 20;
    private int idleDelay = 1;

    int nextDelay(ProductExtractionTickState state, int configuredInterval) {
        int interval = ProductExtractionSettings.clampInterval(configuredInterval);
        return switch (state) {
            case PROGRESSED -> {
                idleDelay = 1;
                yield interval;
            }
            case NO_PROGRESS -> {
                int result = Math.max(interval, idleDelay);
                idleDelay = Math.min(MAX_IDLE_DELAY, idleDelay * 2);
                yield result;
            }
            case WAITING -> 1;
            case DISABLED -> Integer.MAX_VALUE;
        };
    }

    void reset() {
        idleDelay = 1;
    }
}
