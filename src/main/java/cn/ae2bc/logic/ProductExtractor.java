package cn.ae2bc.logic;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import cn.ae2bc.Ae2bcMod;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Objects;

/** Moves filtered item stacks into an AE2 return inventory without extracting items that cannot be stored. */
public final class ProductExtractor {
    @FunctionalInterface
    public interface OverflowHandler {
        void recover(ItemStack stack);
    }

    private ProductExtractor() {
    }

    public static int extract(IItemHandler source, GenericInternalInventory destination,
                              ProductExtractionSettings settings, OverflowHandler overflowHandler) {
        Objects.requireNonNull(overflowHandler, "overflowHandler");
        if (source == null || destination == null || !destination.canInsert() || !settings.enabled()) {
            return 0;
        }

        int moved = 0;
        destination.beginBatch();
        try {
            for (int slot = 0; slot < source.getSlots() && moved < settings.amount(); slot++) {
                int remaining = settings.amount() - moved;
                ItemStack simulated = source.extractItem(slot, remaining, true);
                if (simulated.isEmpty() || !settings.allows(simulated)) {
                    continue;
                }
                AEItemKey key = AEItemKey.of(simulated);
                if (key == null) {
                    continue;
                }

                int accepted = (int) Math.min(simulated.getCount(), insert(destination, key,
                        Math.min(simulated.getCount(), remaining), Actionable.SIMULATE));
                if (accepted <= 0) {
                    continue;
                }

                ItemStack extracted = source.extractItem(slot, accepted, false);
                if (extracted.isEmpty()) {
                    continue;
                }
                AEItemKey extractedKey = AEItemKey.of(extracted);
                int insertable = extractedKey == null ? 0 : (int) Math.min(extracted.getCount(),
                        insert(destination, extractedKey, extracted.getCount(), Actionable.SIMULATE));
                int inserted = insertable <= 0 ? 0 : (int) insert(
                        destination, extractedKey, insertable, Actionable.MODULATE);
                ExtractionRecoveryPlan recovery = ExtractionRecoveryPlan.create(extracted.getCount(), inserted);
                moved += recovery.insertedCount();

                if (recovery.remainderCount() > 0) {
                    ItemStack remainder = reinsert(source, slot,
                            extracted.copyWithCount(recovery.remainderCount()));
                    if (!remainder.isEmpty()) {
                        Ae2bcMod.LOGGER.error("Item handler rejected {} after an inconsistent extraction simulation",
                                remainder);
                        overflowHandler.recover(remainder.copy());
                    }
                }
            }
        } finally {
            destination.endBatch();
        }
        return moved;
    }

    public static int extract(IItemHandler source, RemoteReturnInventory destination,
                              ProductExtractionSettings settings, OverflowHandler overflowHandler) {
        destination.setProductExtractionBypass(true);
        try {
            return extract(source, (GenericInternalInventory) destination, settings, overflowHandler);
        } finally {
            destination.setProductExtractionBypass(false);
        }
    }

    private static long insert(GenericInternalInventory destination, AEItemKey key, int amount,
                               Actionable mode) {
        long inserted = 0;
        for (int slot = 0; slot < destination.size() && inserted < amount; slot++) {
            inserted += destination.insert(slot, key, amount - inserted, mode);
        }
        return inserted;
    }

    private static ItemStack reinsert(IItemHandler source, int preferredSlot, ItemStack stack) {
        ItemStack remainder = source.insertItem(preferredSlot, stack, false);
        for (int slot = 0; slot < source.getSlots() && !remainder.isEmpty(); slot++) {
            if (slot != preferredSlot) {
                remainder = source.insertItem(slot, remainder, false);
            }
        }
        return remainder;
    }
}
