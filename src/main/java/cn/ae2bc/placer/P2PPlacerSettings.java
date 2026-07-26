package cn.ae2bc.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record P2PPlacerSettings(
        Direction direction,
        int offsetX,
        int offsetY,
        int offsetZ) {
    public static final int MAX_OFFSET = 16;
    public static final P2PPlacerSettings DEFAULT = new P2PPlacerSettings(
            Direction.UP, 0, 1, 0);

    public static final Codec<P2PPlacerSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Direction.CODEC.optionalFieldOf("direction", Direction.UP)
                    .forGetter(P2PPlacerSettings::direction),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_x", 0)
                    .forGetter(P2PPlacerSettings::offsetX),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_y", 1)
                    .forGetter(P2PPlacerSettings::offsetY),
            Codec.intRange(-MAX_OFFSET, MAX_OFFSET).optionalFieldOf("offset_z", 0)
                    .forGetter(P2PPlacerSettings::offsetZ)
    ).apply(instance, P2PPlacerSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, P2PPlacerSettings> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeEnum(value.direction);
                buffer.writeByte(value.offsetX);
                buffer.writeByte(value.offsetY);
                buffer.writeByte(value.offsetZ);
            },
            buffer -> new P2PPlacerSettings(
                    buffer.readEnum(Direction.class),
                    buffer.readByte(),
                    buffer.readByte(),
                    buffer.readByte()));

    public P2PPlacerSettings {
        direction = direction == null ? Direction.UP : direction;
        offsetX = clampOffset(offsetX);
        offsetY = clampOffset(offsetY);
        offsetZ = clampOffset(offsetZ);
    }

    public P2PPlacerSettings withDirection(Direction newDirection) {
        return new P2PPlacerSettings(newDirection, offsetX, offsetY, offsetZ);
    }

    public P2PPlacerSettings withOffsets(int x, int y, int z) {
        return new P2PPlacerSettings(direction, x, y, z);
    }

    public P2PPlacerSettings resetOffsets() {
        return withOffsets(0, 1, 0);
    }

    public static int clampOffset(int value) {
        return Math.clamp(value, -MAX_OFFSET, MAX_OFFSET);
    }
}
