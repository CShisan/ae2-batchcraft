package cn.ae2bc.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record P2PPlacerSettings(
        P2PPlacerMode mode,
        Direction direction,
        int offsetX,
        int offsetY,
        int offsetZ) {
    public static final int MAX_OFFSET = 5;
    public static final P2PPlacerSettings DEFAULT = new P2PPlacerSettings(
            P2PPlacerMode.OUTPUT, Direction.NORTH, 0, 0, 0);

    public static final Codec<P2PPlacerSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            P2PPlacerMode.CODEC.optionalFieldOf("mode", P2PPlacerMode.OUTPUT)
                    .forGetter(P2PPlacerSettings::mode),
            Direction.CODEC.optionalFieldOf("direction", Direction.NORTH)
                    .forGetter(P2PPlacerSettings::direction),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_x", 0)
                    .forGetter(P2PPlacerSettings::offsetX),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_y", 0)
                    .forGetter(P2PPlacerSettings::offsetY),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_z", 0)
                    .forGetter(P2PPlacerSettings::offsetZ)
    ).apply(instance, P2PPlacerSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, P2PPlacerSettings> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeEnum(value.mode);
                buffer.writeEnum(value.direction);
                buffer.writeByte(value.offsetX);
                buffer.writeByte(value.offsetY);
                buffer.writeByte(value.offsetZ);
            },
            buffer -> new P2PPlacerSettings(
                    buffer.readEnum(P2PPlacerMode.class),
                    buffer.readEnum(Direction.class),
                    buffer.readByte(),
                    buffer.readByte(),
                    buffer.readByte()));

    public P2PPlacerSettings {
        mode = mode == null ? P2PPlacerMode.OUTPUT : mode;
        direction = direction == null ? Direction.NORTH : direction;
        offsetX = clampOffset(offsetX);
        offsetY = clampOffset(offsetY);
        offsetZ = clampOffset(offsetZ);
    }

    public P2PPlacerSettings withMode(P2PPlacerMode newMode) {
        return new P2PPlacerSettings(newMode, direction, offsetX, offsetY, offsetZ);
    }

    public P2PPlacerSettings withDirection(Direction newDirection) {
        return new P2PPlacerSettings(mode, newDirection, offsetX, offsetY, offsetZ);
    }

    public P2PPlacerSettings withOffsets(int x, int y, int z) {
        return new P2PPlacerSettings(mode, direction, x, y, z);
    }

    public P2PPlacerSettings resetOffsets() {
        return withOffsets(0, 0, 0);
    }

    public P2PPlacerSettings resetOffsetsForNewSelection() {
        return withOffsets(0, 1, 0);
    }

    public static int clampOffset(int value) {
        return Math.clamp(value, -MAX_OFFSET, MAX_OFFSET);
    }
}
