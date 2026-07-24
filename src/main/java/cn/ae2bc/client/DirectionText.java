package cn.ae2bc.client;

import cn.ae2bc.logic.DirectionLayout;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class DirectionText {
    private DirectionText() {
    }

    public static Component name(@Nullable Direction direction, DirectionLayout layout) {
        if (direction == null) {
            return Component.translatable("gui.ae2_batchcraft.direction.auto");
        }
        Component base = Component.translatable("gui.ae2_batchcraft.direction." + direction.getName());
        if (direction == layout.left()) {
            return Component.translatable("gui.ae2_batchcraft.direction.left", base);
        }
        if (direction == layout.right()) {
            return Component.translatable("gui.ae2_batchcraft.direction.right", base);
        }
        return base;
    }
}
