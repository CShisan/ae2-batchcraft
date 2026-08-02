package cn.ae2bc.placer;

import java.util.ArrayList;
import java.util.List;

/** Pure coordinate logic for placer selections, kept independent of Minecraft for unit testing. */
public record ComponentSelectionGeometry(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
    public static final int MAX_AXIS_SIZE = 16;
    public static final long MAX_TARGETS = (long) MAX_AXIS_SIZE * MAX_AXIS_SIZE;

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

    public long sizeX() {
        return Math.abs((long) secondX - firstX) + 1;
    }

    public long sizeY() {
        return Math.abs((long) secondY - firstY) + 1;
    }

    public long sizeZ() {
        return Math.abs((long) secondZ - firstZ) + 1;
    }

    public long targetCount() {
        return sizeX() * sizeY() * sizeZ();
    }

    public List<Position> positions(int offsetX, int offsetY, int offsetZ) {
        if (validate() != Validation.VALID) {
            return List.of();
        }

        long minX = (long) Math.min(firstX, secondX) + offsetX;
        long minY = (long) Math.min(firstY, secondY) + offsetY;
        long minZ = (long) Math.min(firstZ, secondZ) + offsetZ;
        long maxX = (long) Math.max(firstX, secondX) + offsetX;
        long maxY = (long) Math.max(firstY, secondY) + offsetY;
        long maxZ = (long) Math.max(firstZ, secondZ) + offsetZ;
        if (minX < Integer.MIN_VALUE || maxX > Integer.MAX_VALUE
                || minY < Integer.MIN_VALUE || maxY > Integer.MAX_VALUE
                || minZ < Integer.MIN_VALUE || maxZ > Integer.MAX_VALUE) {
            return List.of();
        }
        List<Position> result = new ArrayList<>((int) targetCount());
        for (long y = minY; y <= maxY; y++) {
            for (long z = minZ; z <= maxZ; z++) {
                for (long x = minX; x <= maxX; x++) {
                    result.add(new Position((int) x, (int) y, (int) z));
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
