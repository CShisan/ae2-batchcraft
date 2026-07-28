package cn.ae2bc.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternP2PUnitConfigurationTest {
    @Test
    void defaultsMatchConfiguredBehavior() {
        assertTrue(PatternP2PUnitConfiguration.DEFAULT.breakRecovery());
        assertEquals(15, PatternP2PUnitConfiguration.DEFAULT.redstoneStrength());
        assertEquals(RedstoneOutputMode.SINGLE_TRIGGER, PatternP2PUnitConfiguration.DEFAULT.redstoneMode());
    }

    @Test
    void clampsSignalAndPulseBounds() {
        var value = new PatternP2PUnitConfiguration(ReturnMode.STRICT, false, 99,
                RedstoneOutputMode.PERIODIC_PULSE, 0, 0);

        assertEquals(15, value.redstoneStrength());
        assertEquals(1, value.pulseWidthTicks());
        assertEquals(1, value.pulsePeriodTicks());
    }

    @Test
    void pulsePeriodControlsPulseTimeUpperBound() {
        var value = new PatternP2PUnitConfiguration(ReturnMode.UNBLOCKED, true, 10,
                RedstoneOutputMode.PERIODIC_PULSE, 40, 10);

        assertEquals(10, value.pulseWidthTicks());
        assertEquals(10, value.pulsePeriodTicks());
    }

    @Test
    void pulsePeriodIsLimitedToTwoThousandTicks() {
        var value = new PatternP2PUnitConfiguration(ReturnMode.UNBLOCKED, true, 10,
                RedstoneOutputMode.PERIODIC_PULSE, 3000, 3000);

        assertEquals(2000, value.pulseWidthTicks());
        assertEquals(2000, value.pulsePeriodTicks());
    }

    @Test
    void copyMethodsPreserveUnchangedFields() {
        var value = PatternP2PUnitConfiguration.DEFAULT.withReturnMode(ReturnMode.STRICT)
                .withBreakRecovery(false)
                .withRedstone(7, RedstoneOutputMode.CONTINUOUS, 4, 30);

        assertEquals(ReturnMode.STRICT, value.returnMode());
        assertFalse(value.breakRecovery());
        assertEquals(7, value.redstoneStrength());
        assertEquals(4, value.pulseWidthTicks());
        assertEquals(30, value.pulsePeriodTicks());
    }

    @Test
    void redstoneModeCyclesInUiOrder() {
        assertEquals(RedstoneOutputMode.PERIODIC_PULSE, RedstoneOutputMode.SINGLE_TRIGGER.next());
        assertEquals(RedstoneOutputMode.CONTINUOUS, RedstoneOutputMode.PERIODIC_PULSE.next());
        assertEquals(RedstoneOutputMode.SINGLE_TRIGGER, RedstoneOutputMode.CONTINUOUS.next());
    }
}
