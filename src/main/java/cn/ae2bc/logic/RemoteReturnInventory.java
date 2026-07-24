package cn.ae2bc.logic;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Insert-only view of the return inventory behind the input side of a tunnel.
 */
public final class RemoteReturnInventory implements GenericInternalInventory, MEStorage {
    private final Supplier<@Nullable GenericInternalInventory> targetSupplier;
    private final BiFunction<AEKey, Long, Long> amountFilter;
    private final Consumer<GenericStack> insertionListener;

    public RemoteReturnInventory(Supplier<@Nullable GenericInternalInventory> targetSupplier,
                                 BiFunction<AEKey, Long, Long> amountFilter,
                                 Consumer<GenericStack> insertionListener) {
        this.targetSupplier = Objects.requireNonNull(targetSupplier, "targetSupplier");
        this.amountFilter = Objects.requireNonNull(amountFilter, "amountFilter");
        this.insertionListener = Objects.requireNonNull(insertionListener, "insertionListener");
    }

    @Override
    public int size() {
        return PatternProviderReturnInventory.NUMBER_OF_SLOTS;
    }

    @Override
    public @Nullable GenericStack getStack(int slot) {
        var target = getTarget();
        return target != null ? target.getStack(slot) : null;
    }

    @Override
    public @Nullable AEKey getKey(int slot) {
        var target = getTarget();
        return target != null ? target.getKey(slot) : null;
    }

    @Override
    public long getAmount(int slot) {
        var target = getTarget();
        return target != null ? target.getAmount(slot) : 0;
    }

    @Override
    public long getMaxAmount(AEKey key) {
        var target = getTarget();
        if (target != null) {
            return target.getMaxAmount(key);
        }
        long capacity = getCapacity(key.getType());
        return key instanceof AEItemKey itemKey ? Math.min(itemKey.getMaxStackSize(), capacity) : capacity;
    }

    @Override
    public long getCapacity(AEKeyType keyType) {
        var target = getTarget();
        return target != null
                ? target.getCapacity(keyType)
                : GenericSlotCapacities.getMap().getOrDefault(keyType, Long.MAX_VALUE);
    }

    @Override
    public boolean canInsert() {
        var target = getTarget();
        return target != null && target.canInsert();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack newStack) {
        // Machines may only return products through insertion.
    }

    @Override
    public boolean isSupportedType(AEKeyType type) {
        var target = getTarget();
        return target != null ? target.isSupportedType(type) : AEKeyTypes.getAll().contains(type);
    }

    @Override
    public boolean isAllowedIn(int slot, AEKey what) {
        var target = getTarget();
        return target != null && target.isAllowedIn(slot, what);
    }

    @Override
    public long insert(int slot, AEKey what, long amount, Actionable mode) {
        amount = filteredAmount(what, amount);
        if (amount <= 0) {
            return 0;
        }
        var target = getTarget();
        if (target == null) {
            return 0;
        }
        long inserted = target.insert(slot, what, amount, mode);
        notifyInserted(what, inserted, mode);
        return inserted;
    }

    @Override
    public long extract(int slot, AEKey what, long amount, Actionable mode) {
        return 0;
    }

    @Override
    public void beginBatch() {
    }

    @Override
    public void endBatch() {
    }

    @Override
    public void endBatchSuppressed() {
    }

    @Override
    public void onChange() {
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        amount = filteredAmount(what, amount);
        if (amount <= 0) {
            return 0;
        }
        var target = getTarget();
        if (target instanceof MEStorage storage) {
            long inserted = storage.insert(what, amount, mode, source);
            notifyInserted(what, inserted, mode);
            return inserted;
        }
        if (target == null) {
            return 0;
        }

        long inserted = 0;
        for (int slot = 0; slot < target.size() && inserted < amount; slot++) {
            inserted += target.insert(slot, what, amount - inserted, mode);
        }
        notifyInserted(what, inserted, mode);
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        // Returned products are intentionally not exposed for extraction.
    }

    @Override
    public Component getDescription() {
        return Component.translatable("item.ae2_batchcraft.pattern_p2p_tunnel_output");
    }

    private @Nullable GenericInternalInventory getTarget() {
        var target = targetSupplier.get();
        return target == this || target instanceof RemoteReturnInventory ? null : target;
    }

    private void notifyInserted(AEKey what, long amount, Actionable mode) {
        if (mode == Actionable.MODULATE && amount > 0) {
            insertionListener.accept(new GenericStack(what, amount));
        }
    }

    private long filteredAmount(AEKey what, long amount) {
        if (amount <= 0) {
            return 0;
        }
        return Math.clamp(amountFilter.apply(what, amount), 0, amount);
    }
}
