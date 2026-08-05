package cn.ae2bc.logic;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Set;

/** Immutable extraction policy used by product extractors. */
public record ProductExtractionSettings(boolean enabled, int interval, int amount,
                                        boolean whitelist, Set<AEKey> markers) {
    public static final int DEFAULT_INTERVAL = 20;
    public static final int DEFAULT_AMOUNT = 64;
    public static final int MIN_INTERVAL = 1;
    public static final int MAX_INTERVAL = 2000;
    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 64;
    public static final int MARKER_SLOT_COUNT = 18;

    public ProductExtractionSettings {
        interval = clampInterval(interval);
        amount = clampAmount(amount);
        markers = Set.copyOf(Objects.requireNonNull(markers, "markers"));
    }

    public static int clampInterval(int value) {
        return Math.clamp(value, MIN_INTERVAL, MAX_INTERVAL);
    }

    public static int clampAmount(int value) {
        return Math.clamp(value, MIN_AMOUNT, MAX_AMOUNT);
    }

    public boolean allows(ItemStack stack) {
        return allows(AEItemKey.of(stack));
    }

    public boolean allows(AEKey key) {
        if (key == null) {
            return false;
        }
        boolean marked = markers.contains(key);
        return whitelist ? marked : !marked;
    }
}
