package cn.ae2bc.placer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class P2PPlacerSelectionTest {
    @Test
    void pointSelectionProducesOneTarget() {
        var selection = new P2PSelectionGeometry(3, 4, 5, 3, 4, 5);

        assertEquals(P2PSelectionGeometry.Validation.VALID, selection.validate());
        assertEquals(1, selection.targetCount());
        assertEquals(new P2PSelectionGeometry.Position(3, 4, 5), selection.positions(0, 0, 0).getFirst());
    }

    @Test
    void lineAndPlaneAreAllowedButVolumeIsRejected() {
        var line = new P2PSelectionGeometry(0, 0, 0, 15, 0, 0);
        var plane = new P2PSelectionGeometry(0, 0, 0, 15, 0, 15);
        var volume = new P2PSelectionGeometry(0, 0, 0, 1, 1, 1);

        assertEquals(P2PSelectionGeometry.Validation.VALID, line.validate());
        assertEquals(16, line.targetCount());
        assertEquals(P2PSelectionGeometry.Validation.VALID, plane.validate());
        assertEquals(256, plane.targetCount());
        assertEquals(P2PSelectionGeometry.Validation.VOLUME_NOT_ALLOWED, volume.validate());
    }

    @Test
    void axisSizeCannotExceedSixteen() {
        var selection = new P2PSelectionGeometry(0, 0, 0, 16, 0, 0);
        assertEquals(P2PSelectionGeometry.Validation.TOO_LARGE, selection.validate());
    }

    @Test
    void offsetsTranslateWithoutChangingSelectionSize() {
        var selection = new P2PSelectionGeometry(1, 2, 3, 2, 2, 3);
        var positions = selection.positions(5, -5, 1);

        assertEquals(2, positions.size());
        assertEquals(new P2PSelectionGeometry.Position(6, -3, 4), positions.getFirst());
        assertEquals(new P2PSelectionGeometry.Position(7, -3, 4), positions.get(1));
    }
}
