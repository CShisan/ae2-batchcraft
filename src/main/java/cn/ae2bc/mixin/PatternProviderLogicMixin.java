package cn.ae2bc.mixin;

import appeng.api.networking.IManagedGridNode;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.StackWorldBehaviors;
import cn.ae2bc.extension.PatternProviderExtractionExtension;
import cn.ae2bc.logic.ProductExtractionSettings;
import cn.ae2bc.logic.ProductExtractionTickState;
import cn.ae2bc.logic.ProductExtractor;
import cn.ae2bc.logic.ProductExtractionUpgradeInventory;
import cn.ae2bc.registry.ModContent;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.IdentityHashMap;
import java.util.ArrayList;

@Mixin(PatternProviderLogic.class)
public abstract class PatternProviderLogicMixin implements PatternProviderExtractionExtension {
    private static final String AE2BC_UPGRADES = "Ae2bcProductExtractionUpgrades";
    private static final String AE2BC_MARKERS = "Ae2bcProductExtractionMarkers";
    private static final String AE2BC_INTERVAL = "Ae2bcProductExtractionInterval";
    private static final String AE2BC_AMOUNT = "Ae2bcProductExtractionAmount";
    private static final String AE2BC_WHITELIST = "Ae2bcProductExtractionWhitelist";
    private static final String AE2BC_RECOVERY = "Ae2bcProductExtractionRecovery";

    @Shadow @Final private PatternProviderLogicHost host;
    @Shadow @Final private IManagedGridNode mainNode;
    @Shadow @Final private PatternProviderReturnInventory returnInv;

    @Unique private IUpgradeInventory ae2bc$productExtractionUpgrades;
    @Unique private GenericStackInv ae2bc$productExtractionMarkers;
    @Unique private final EnumMap<Direction, BlockCapabilityCache<MEStorage, Direction>> ae2bc$storageTargets =
            new EnumMap<>(Direction.class);
    @Unique private final EnumMap<Direction, Map<AEKeyType, ExternalStorageStrategy>> ae2bc$externalStrategies =
            new EnumMap<>(Direction.class);
    @Unique private int ae2bc$productExtractionInterval = ProductExtractionSettings.DEFAULT_INTERVAL;
    @Unique private int ae2bc$productExtractionAmount = ProductExtractionSettings.DEFAULT_AMOUNT;
    @Unique private boolean ae2bc$productExtractionWhitelist;
    @Unique private long ae2bc$lastExtractionTick = Long.MIN_VALUE;
    @Unique private int ae2bc$directionCursor;
    @Unique private final List<GenericStack> ae2bc$extractionRecovery = new ArrayList<>();
    @Unique private boolean ae2bc$productExtractionBypass;

    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;"
                    + "Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("RETURN"))
    private void ae2bc$initializeProductExtraction(CallbackInfo ci) {
        var providerItem = host.getTerminalIcon().getItem();
        ae2bc$productExtractionUpgrades = new ProductExtractionUpgradeInventory(providerItem,
                ModContent.PRODUCT_EXTRACTION_CARD.get(), this::ae2bc$onProductExtractionChanged);
        ae2bc$productExtractionMarkers = new GenericStackInv(Set.copyOf(AEKeyTypes.getAll()),
                this::ae2bc$onProductExtractionChanged, GenericStackInv.Mode.CONFIG_TYPES,
                ProductExtractionSettings.MARKER_SLOT_COUNT);
        // Direct machine output and Pattern P2P returns both converge on this inventory.
        ((GenericStackInvAccessor) returnInv).ae2bc$setFilter((slot, what) -> ae2bc$allowsReturnedProduct(what));
    }

    @Override
    public IUpgradeInventory ae2bc$getProductExtractionUpgrades() {
        return ae2bc$productExtractionUpgrades;
    }

    @Override
    public GenericStackInv ae2bc$getProductExtractionMarkers() {
        return ae2bc$productExtractionMarkers;
    }

    @Override
    public ProductExtractionSettings ae2bc$getProductExtractionSettings() {
        Set<AEKey> markers = new HashSet<>();
        for (int slot = 0; slot < ae2bc$productExtractionMarkers.size(); slot++) {
            if (ae2bc$productExtractionMarkers.getKey(slot) instanceof AEKey key) {
                markers.add(key);
            }
        }
        return new ProductExtractionSettings(ae2bc$hasProductExtractionCard(), ae2bc$productExtractionInterval,
                ae2bc$productExtractionAmount, ae2bc$productExtractionWhitelist, markers);
    }

    @Override
    public void ae2bc$setProductExtractionInterval(int interval) {
        int clamped = ProductExtractionSettings.clampInterval(interval);
        if (ae2bc$productExtractionInterval != clamped) {
            ae2bc$productExtractionInterval = clamped;
            ae2bc$lastExtractionTick = Long.MIN_VALUE;
            ae2bc$onProductExtractionChanged();
        }
    }

    @Override
    public void ae2bc$setProductExtractionAmount(int amount) {
        int clamped = ProductExtractionSettings.clampAmount(amount);
        if (ae2bc$productExtractionAmount != clamped) {
            ae2bc$productExtractionAmount = clamped;
            ae2bc$onProductExtractionChanged();
        }
    }

    @Override
    public void ae2bc$setProductExtractionWhitelist(boolean whitelist) {
        if (ae2bc$productExtractionWhitelist != whitelist) {
            ae2bc$productExtractionWhitelist = whitelist;
            ae2bc$onProductExtractionChanged();
        }
    }

    @Override
    public boolean ae2bc$hasProductExtractionCard() {
        return ae2bc$productExtractionUpgrades.isInstalled(ModContent.PRODUCT_EXTRACTION_CARD.get());
    }

    @Unique
    private boolean ae2bc$allowsReturnedProduct(AEKey what) {
        if (ae2bc$productExtractionBypass || !ae2bc$hasProductExtractionCard()) {
            return true;
        }
        return ae2bc$getProductExtractionSettings().allows(what);
    }

    @Override
    @Unique
    public ProductExtractionTickState ae2bc$tickProductExtraction() {
        if (!mainNode.isActive() || !(host.getBlockEntity().getLevel() instanceof ServerLevel level)) {
            return ProductExtractionTickState.DISABLED;
        }
        ae2bc$drainRecovery();
        if (!ae2bc$hasProductExtractionCard()) {
            return ae2bc$extractionRecovery.isEmpty()
                    ? ProductExtractionTickState.DISABLED : ProductExtractionTickState.WAITING;
        }
        long tick = level.getGameTime();
        if (ae2bc$lastExtractionTick != Long.MIN_VALUE
                && tick - ae2bc$lastExtractionTick < ae2bc$productExtractionInterval) {
            return ProductExtractionTickState.WAITING;
        }
        ae2bc$lastExtractionTick = tick;

        List<Direction> directions = ((PatternProviderLogicAccessor) (Object) this)
                .ae2bc$getActiveSides().stream().sorted().toList();
        if (directions.isEmpty()) {
            return ProductExtractionTickState.ATTEMPTED;
        }
        int moved = 0;
        int start = Math.floorMod(ae2bc$directionCursor, directions.size());
        ProductExtractionSettings base = ae2bc$getProductExtractionSettings();
        for (int offset = 0; offset < directions.size() && moved < base.amount(); offset++) {
            Direction direction = directions.get((start + offset) % directions.size());
            var target = ae2bc$getTarget(level, direction);
            if (target.isEmpty()) {
                continue;
            }
            int remaining = base.amount() - moved;
            ae2bc$productExtractionBypass = true;
            try {
                moved += ProductExtractor.extract(target, returnInv,
                        new ProductExtractionSettings(true, base.interval(), remaining,
                                base.whitelist(), base.markers()),
                        new MachineSource(mainNode::getNode), this::ae2bc$queueRecovery);
            } finally {
                ae2bc$productExtractionBypass = false;
            }
        }
        ae2bc$directionCursor = (start + 1) % directions.size();
        return ProductExtractionTickState.ATTEMPTED;
    }

    @Unique
    private Map<AEKeyType, MEStorage> ae2bc$getTarget(ServerLevel level, Direction direction) {
        var pos = host.getBlockEntity().getBlockPos().relative(direction);
        Direction face = direction.getOpposite();
        var cache = ae2bc$storageTargets.get(direction);
        if (cache == null || cache.level() != level || !cache.pos().equals(pos) || cache.context() != face) {
            cache = BlockCapabilityCache.create(AECapabilities.ME_STORAGE, level, pos, face);
            ae2bc$storageTargets.put(direction, cache);
            ae2bc$externalStrategies.put(direction,
                    StackWorldBehaviors.createExternalStorageStrategies(level, pos, face));
        }
        MEStorage direct = cache.getCapability();
        if (direct != null) {
            Map<AEKeyType, MEStorage> result = new IdentityHashMap<>();
            for (var type : AEKeyTypes.getAll()) {
                result.put(type, direct);
            }
            return result;
        }
        Map<AEKeyType, MEStorage> result = new IdentityHashMap<>();
        for (var entry : ae2bc$externalStrategies.getOrDefault(direction, Map.of()).entrySet()) {
            var wrapper = entry.getValue().createWrapper(false, this::ae2bc$alertProductExtraction);
            if (wrapper != null) {
                result.put(entry.getKey(), wrapper);
            }
        }
        return result;
    }

    @Unique
    private void ae2bc$queueRecovery(GenericStack stack) {
        for (int i = 0; i < ae2bc$extractionRecovery.size(); i++) {
            GenericStack existing = ae2bc$extractionRecovery.get(i);
            if (existing.what().equals(stack.what())) {
                long amount = existing.amount() > Long.MAX_VALUE - stack.amount()
                        ? Long.MAX_VALUE : existing.amount() + stack.amount();
                ae2bc$extractionRecovery.set(i, new GenericStack(existing.what(), amount));
                host.saveChanges();
                return;
            }
        }
        ae2bc$extractionRecovery.add(stack);
        host.saveChanges();
    }

    @Unique
    private void ae2bc$drainRecovery() {
        boolean changed = false;
        IActionSource source = new MachineSource(mainNode::getNode);
        ae2bc$productExtractionBypass = true;
        try {
            for (var iterator = ae2bc$extractionRecovery.listIterator(); iterator.hasNext(); ) {
                GenericStack stack = iterator.next();
                long inserted = returnInv.insert(stack.what(), stack.amount(), Actionable.MODULATE, source);
                if (inserted >= stack.amount()) {
                    iterator.remove();
                    changed = true;
                } else if (inserted > 0) {
                    iterator.set(new GenericStack(stack.what(), stack.amount() - inserted));
                    changed = true;
                }
            }
        } finally {
            ae2bc$productExtractionBypass = false;
        }
        if (changed) {
            host.saveChanges();
        }
    }

    @Unique
    private void ae2bc$onProductExtractionChanged() {
        host.saveChanges();
        ae2bc$alertProductExtraction();
    }

    @Unique
    private void ae2bc$alertProductExtraction() {
        mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void ae2bc$readProductExtraction(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        ae2bc$productExtractionUpgrades.readFromNBT(data, AE2BC_UPGRADES, registries);
        ae2bc$productExtractionMarkers.readFromChildTag(data, AE2BC_MARKERS, registries);
        ae2bc$productExtractionInterval = data.contains(AE2BC_INTERVAL, Tag.TAG_INT)
                ? ProductExtractionSettings.clampInterval(data.getInt(AE2BC_INTERVAL))
                : ProductExtractionSettings.DEFAULT_INTERVAL;
        ae2bc$productExtractionAmount = data.contains(AE2BC_AMOUNT, Tag.TAG_INT)
                ? ProductExtractionSettings.clampAmount(data.getInt(AE2BC_AMOUNT))
                : ProductExtractionSettings.DEFAULT_AMOUNT;
        ae2bc$productExtractionWhitelist = data.getBoolean(AE2BC_WHITELIST);
        ae2bc$extractionRecovery.clear();
        var recovery = data.getList(AE2BC_RECOVERY, Tag.TAG_COMPOUND);
        for (int i = 0; i < recovery.size(); i++) {
            GenericStack stack = GenericStack.readTag(registries, recovery.getCompound(i));
            if (stack != null && stack.amount() > 0) {
                ae2bc$extractionRecovery.add(stack);
            }
        }
        ae2bc$lastExtractionTick = Long.MIN_VALUE;
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void ae2bc$writeProductExtraction(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        ae2bc$productExtractionUpgrades.writeToNBT(data, AE2BC_UPGRADES, registries);
        ae2bc$productExtractionMarkers.writeToChildTag(data, AE2BC_MARKERS, registries);
        data.putInt(AE2BC_INTERVAL, ae2bc$productExtractionInterval);
        data.putInt(AE2BC_AMOUNT, ae2bc$productExtractionAmount);
        data.putBoolean(AE2BC_WHITELIST, ae2bc$productExtractionWhitelist);
        ListTag recovery = new ListTag();
        for (GenericStack stack : ae2bc$extractionRecovery) {
            recovery.add(GenericStack.writeTag(registries, stack));
        }
        data.put(AE2BC_RECOVERY, recovery);
    }

    @Inject(method = "addDrops", at = @At("TAIL"))
    private void ae2bc$dropProductExtractionUpgrades(List<ItemStack> drops, CallbackInfo ci) {
        for (ItemStack stack : ae2bc$productExtractionUpgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        for (GenericStack stack : ae2bc$extractionRecovery) {
            stack.what().addDrops(stack.amount(), drops, host.getBlockEntity().getLevel(),
                    host.getBlockEntity().getBlockPos());
        }
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void ae2bc$clearProductExtraction(CallbackInfo ci) {
        ae2bc$productExtractionUpgrades.clear();
        ae2bc$productExtractionMarkers.clear();
        ae2bc$extractionRecovery.clear();
    }
}
