package cn.ae2bc.placer;

import java.util.ArrayList;
import java.util.List;

/** Pure coordinate logic for placer selections, kept independent of Minecraft for unit testing. */
public record P2PSelectionGeometry(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
    public static final int MAX_AXIS_SIZE = 16;
    public static final int MAX_TARGETS = MAX_AXIS_SIZE * MAX_AXIS_SIZE;

    public Validation validate() {
        int varyingAxes = 0;
        if (firstX != secondX) {
            varyingAxes++;
        }
        if (firstY != secondY) {
            varyingAxes++;
        }
        if (firstZ != secondZ) {
            varyingAxes++;
        }
        if (varyingAxes == 3) {
            return Validation.VOLUME_NOT_ALLOWED;
        }
        if (sizeX() > MAX_AXIS_SIZE || sizeY() > MAX_AXIS_SIZE || sizeZ() > MAX_AXIS_SIZE
                || targetCount() > MAX_TARGETS) {
            return Validation.TOO_LARGE;
        }
        return Validation.VALID;
    }

    public int sizeX() {
        return Math.abs(secondX - firstX) + 1;
    }

    public int sizeY() {
        return Math.abs(secondY - firstY) + 1;
    }

    public int sizeZ() {
        return Math.abs(secondZ - firstZ) + 1;
    }

    public int targetCount() {
        return sizeX() * sizeY() * sizeZ();
    }

    public List<Position> positions(int offsetX, int offsetY, int offsetZ) {
        if (validate() != Validation.VALID) {
            return List.of();
        }

        int minX = Math.min(firstX, secondX) + offsetX;
        int minY = Math.min(firstY, secondY) + offsetY;
        int minZ = Math.min(firstZ, secondZ) + offsetZ;
        int maxX = Math.max(firstX, secondX) + offsetX;
        int maxY = Math.max(firstY, secondY) + offsetY;
        int maxZ = Math.max(firstZ, secondZ) + offsetZ;
        List<Position> result = new ArrayList<>(targetCount());
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    result.add(new Position(x, y, z));
                }
            }
        }
        return List.copyOf(result);
    }

    public enum Validation {
        VALID,
        VOLUME_NOT_ALLOWED,
        TOO_LARGE
    }

    public record Position(int x, int y, int z) {
    }
}
