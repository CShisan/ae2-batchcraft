package cn.ae2bc.part;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.behaviors.PlacementStrategy;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.api.util.AECableType;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import appeng.parts.p2p.P2PModels;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.Platform;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.logic.RedstoneOutputMode;
import cn.ae2bc.logic.PatternP2PUnitIdentityColors;
import cn.ae2bc.logic.PatternP2PUnitPortType;
import cn.ae2bc.client.model.PatternP2PUnitModelData;
import cn.ae2bc.pattern.MaterialOutputForm;
import cn.ae2bc.registry.ModContent;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.client.model.data.ModelData;
import appeng.client.render.cablebus.P2PTunnelFrequencyModelData;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A task-gated unit endpoint. Ports never perform world interaction without an active manager task. */
public final class PatternP2PUnitPortPart extends AEBasePart implements IGridTickable {
    private static final ResourceLocation IDENTITY_MODEL = ResourceLocation.fromNamespaceAndPath(
            Ae2bcMod.MOD_ID, "part/p2p/pp2p_unit_port_identity");
    private static final Map<PatternP2PUnitPortType, PatternP2PUnitPortModels> MODELS = createModels();

    private final PatternP2PUnitPortType type;
    private final IActionSource actionSource = new MachineSource(this);
    private final PortReturnInventory returnInventory = new PortReturnInventory();
    private final IItemHandler returnItemHandler = new GenericStackItemStorage(returnInventory);
    private final IFluidHandler returnFluidHandler = new GenericStackFluidStorage(returnInventory);
    private @Nullable UUID boundPatternP2PUnitId;
    private short boundFrequency;
    private @Nullable PlacementStrategy placementStrategy;
    private @Nullable List<PickupStrategy> pickupStrategies;
    private @Nullable Map<appeng.api.stacks.AEKeyType, ExternalStorageStrategy> externalStrategies;
    private @Nullable BlockCapabilityCache<IEnergyStorage, Direction> energyTargetCache;
    private int redstonePower;
    private long taskStartTick = Long.MIN_VALUE;

    public PatternP2PUnitPortPart(IPartItem<?> partItem, PatternP2PUnitPortType type) {
        super(partItem);
        this.type = type;
        getMainNode().addService(IGridTickable.class, this);
    }

    private static Map<PatternP2PUnitPortType, PatternP2PUnitPortModels> createModels() {
        Map<PatternP2PUnitPortType, PatternP2PUnitPortModels> result = new EnumMap<>(PatternP2PUnitPortType.class);
        for (PatternP2PUnitPortType type : PatternP2PUnitPortType.values()) {
            ResourceLocation front = ResourceLocation.fromNamespaceAndPath(
                    Ae2bcMod.MOD_ID, "part/p2p/pp2p_unit_port_" + type.name().toLowerCase());
            result.put(type, new PatternP2PUnitPortModels(front));
        }
        return result;
    }

    public static void registerModels() {
        PartModels.registerModels(MODELS.values().stream()
                .flatMap(models -> models.models().stream())
                .flatMap(model -> model.getModels().stream())
                .toList());
    }

    public PatternP2PUnitPortType getType() {
        return type;
    }

    public @Nullable UUID getBoundPatternP2PUnitId() {
        return boundPatternP2PUnitId;
    }

    public short getBoundFrequency() {
        PatternP2PUnitManagerPart manager = getManager();
        return manager == null ? boundFrequency : manager.getFrequency();
    }

    public boolean isBoundUnitTaskActive() {
        PatternP2PUnitManagerPart manager = getManager();
        return manager != null && manager.isTaskActive();
    }

    public boolean isBoundTo(PatternP2PUnitManagerPart manager) {
        return boundPatternP2PUnitId != null && boundPatternP2PUnitId.equals(manager.getPatternP2PUnitId());
    }

    public long insertInput(PatternP2PUnitManagerPart manager, GenericStack stack,
                            MaterialOutputForm form, Actionable mode) {
        if (!isBoundTo(manager) || stack == null || stack.amount() <= 0
                || PatternP2PUnitPortType.forOutputForm(form) != type || !form.supports(stack.what())) {
            return 0;
        }
        // Admission probes happen before task activation; world mutation does not.
        if (mode == Actionable.MODULATE && !manager.getLogic().isTaskOperational()) {
            return 0;
        }
        return switch (type) {
            case DROP, PLACE -> getPlacementStrategy().placeInWorld(
                    stack.what(), stack.amount(), mode, type == PatternP2PUnitPortType.DROP);
            case TRANSFER -> insertIntoTarget(stack.what(), stack.amount(), mode);
            default -> 0;
        };
    }

    private long insertIntoTarget(AEKey what, long amount, Actionable mode) {
        var strategy = getExternalStrategies().get(what.getType());
        if (strategy == null) {
            return 0;
        }
        MEStorage storage = strategy.createWrapper(false, this::wake);
        return storage == null ? 0 : storage.insert(what, amount, mode, actionSource);
    }

    private PlacementStrategy getPlacementStrategy() {
        if (placementStrategy == null && getLevel() instanceof ServerLevel level && getSide() != null) {
            var target = getBlockEntity().getBlockPos().relative(getSide());
            placementStrategy = StackWorldBehaviors.createPlacementStrategies(level, target,
                    getSide().getOpposite(), getBlockEntity(), getMainNode().getNode().getOwningPlayerProfileId());
        }
        return placementStrategy == null ? PlacementStrategy.noop() : placementStrategy;
    }

    private Map<appeng.api.stacks.AEKeyType, ExternalStorageStrategy> getExternalStrategies() {
        if (externalStrategies == null && getLevel() instanceof ServerLevel level && getSide() != null) {
            externalStrategies = StackWorldBehaviors.createExternalStorageStrategies(
                    level, getBlockEntity().getBlockPos().relative(getSide()), getSide().getOpposite());
        }
        return externalStrategies == null ? Map.of() : externalStrategies;
    }

    private List<PickupStrategy> getPickupStrategies() {
        if (pickupStrategies == null && getLevel() instanceof ServerLevel level && getSide() != null) {
            pickupStrategies = StackWorldBehaviors.createPickupStrategies(level,
                    getBlockEntity().getBlockPos().relative(getSide()), getSide().getOpposite(),
                    getBlockEntity(), ItemEnchantments.EMPTY,
                    getMainNode().getNode().getOwningPlayerProfileId());
        }
        return pickupStrategies == null ? List.of() : pickupStrategies;
    }

    private @Nullable PatternP2PUnitManagerPart getManager() {
        var grid = getMainNode().getGrid();
        if (grid == null || boundPatternP2PUnitId == null) {
            return null;
        }
        for (var candidate : grid.getMachines(PatternP2PUnitManagerPart.class)) {
            if (boundPatternP2PUnitId.equals(candidate.getPatternP2PUnitId())) {
                return candidate;
            }
        }
        return null;
    }

    private boolean runPickup(PatternP2PUnitManagerPart manager) {
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return false;
        }
        boolean changed = false;
        for (PickupStrategy strategy : getPickupStrategies()) {
            strategy.reset();
            PickupStrategy.Result result = strategy.tryPickup(grid.getEnergyService(),
                    (what, amount, mode) -> handlePickedUp(manager, what, amount, mode));
            changed |= result == PickupStrategy.Result.PICKED_UP;
        }
        return changed;
    }

    private boolean pickupEntities(PatternP2PUnitManagerPart manager) {
        if (!(getLevel() instanceof ServerLevel level) || getSide() == null) {
            return false;
        }
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return false;
        }
        boolean changed = false;
        var target = getBlockEntity().getBlockPos().relative(getSide());
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(target))) {
            for (PickupStrategy strategy : getPickupStrategies()) {
                if (strategy.canPickUpEntity(entity)) {
                    changed |= strategy.pickUpEntity(grid.getEnergyService(),
                            (what, amount, mode) -> manager.getLogic()
                                    .insertReturned(what, amount, mode), entity);
                    break;
                }
            }
        }
        return changed;
    }

    private long handlePickedUp(PatternP2PUnitManagerPart manager, AEKey what, long amount, Actionable mode) {
        var configuration = manager.getLogic().getEffectiveConfiguration();
        long accepted = manager.getLogic().simulateReturned(what, amount);
        if (accepted < amount || mode == Actionable.SIMULATE) {
            return accepted;
        }
        if (configuration.breakRecovery() || what instanceof AEFluidKey) {
            return manager.getLogic().insertReturned(what, amount, Actionable.MODULATE);
        }
        if (what instanceof AEItemKey itemKey && getLevel() instanceof ServerLevel level) {
            List<ItemStack> drops = new java.util.ArrayList<>();
            itemKey.addDrops(amount, drops, level, getBlockEntity().getBlockPos().relative(getSide()));
            Platform.spawnDrops(level, getBlockEntity().getBlockPos().relative(getSide()), drops);
            return amount;
        }
        return 0;
    }

    private boolean updateRedstone(PatternP2PUnitManagerPart manager) {
        var settings = manager.getLogic().getEffectiveConfiguration();
        long activeTicks = Math.max(0, getLevel().getGameTime() - taskStartTick);
        int next = switch (settings.redstoneMode()) {
            case CONTINUOUS -> settings.redstoneStrength();
            case SINGLE_TRIGGER -> activeTicks < settings.pulseWidthTicks() ? settings.redstoneStrength() : 0;
            case PERIODIC_PULSE -> activeTicks % settings.pulsePeriodTicks() < settings.pulseWidthTicks()
                    ? settings.redstoneStrength() : 0;
        };
        setRedstonePower(next);
        return settings.redstoneMode() == RedstoneOutputMode.PERIODIC_PULSE
                || settings.redstoneMode() == RedstoneOutputMode.SINGLE_TRIGGER
                && activeTicks <= settings.pulseWidthTicks();
    }

    private void setRedstonePower(int power) {
        int clamped = Math.clamp(power, 0, 15);
        if (redstonePower != clamped) {
            redstonePower = clamped;
            Platform.notifyBlocksOfNeighbors(getLevel(), getBlockEntity().getBlockPos());
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, TickRates.Interface.getMax(), false, 5);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        PatternP2PUnitManagerPart manager = getManager();
        if (manager == null || !manager.getLogic().isTaskOperational()) {
            taskStartTick = Long.MIN_VALUE;
            setRedstonePower(0);
            return TickRateModulation.IDLE;
        }
        if (taskStartTick == Long.MIN_VALUE) {
            taskStartTick = getLevel().getGameTime();
        }
        boolean changed = switch (type) {
            case BREAK -> runPickup(manager);
            case PICKUP -> pickupEntities(manager);
            case ENERGY -> false;
            case REDSTONE -> {
                yield updateRedstone(manager);
            }
            default -> false;
        };
        return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    /**
     * Accepts energy supplied by a pattern P2P energy tunnel and forwards it to the
     * adjacent machine. It deliberately does not use the subnet's AE energy service.
     */
    public int receiveExternalEnergy(int maxReceive, boolean simulate) {
        PatternP2PUnitManagerPart manager = getManager();
        if (type != PatternP2PUnitPortType.ENERGY || maxReceive <= 0 || manager == null
                || !manager.getLogic().isTaskOperational()) {
            return 0;
        }

        Direction side = getSide();
        if (side == null || !(getLevel() instanceof ServerLevel level)) {
            return 0;
        }

        var targetPos = getBlockEntity().getBlockPos().relative(side);
        Direction targetSide = side.getOpposite();
        if (energyTargetCache == null
                || energyTargetCache.level() != level
                || !energyTargetCache.pos().equals(targetPos)
                || energyTargetCache.context() != targetSide) {
            energyTargetCache = BlockCapabilityCache.create(
                    Capabilities.EnergyStorage.BLOCK, level, targetPos, targetSide);
        }
        IEnergyStorage target = energyTargetCache.getCapability();
        if (target == null || !target.canReceive()) {
            return 0;
        }
        return target.receiveEnergy(maxReceive, simulate);
    }

    public boolean isReturnPort() {
        return type == PatternP2PUnitPortType.RETURN || type == PatternP2PUnitPortType.PICKUP;
    }

    public GenericInternalInventory getReturnInventory() {
        return returnInventory;
    }

    public MEStorage getReturnStorage() {
        return returnInventory;
    }

    public IItemHandler getReturnItemHandler() {
        return returnItemHandler;
    }

    public IFluidHandler getReturnFluidHandler() {
        return returnFluidHandler;
    }

    @Override
    public boolean canConnectRedstone() {
        return type == PatternP2PUnitPortType.REDSTONE;
    }

    @Override
    public int isProvidingStrongPower() {
        return type == PatternP2PUnitPortType.REDSTONE ? redstonePower : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return isProvidingStrongPower();
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        boundPatternP2PUnitId = data.hasUUID("PatternP2PUnitId") ? data.getUUID("PatternP2PUnitId") : null;
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        if (boundPatternP2PUnitId != null) {
            data.putUUID("PatternP2PUnitId", boundPatternP2PUnitId);
        } else {
            data.remove("PatternP2PUnitId");
        }
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (!(heldItem.getItem() instanceof IMemoryCard card) || hand == InteractionHand.OFF_HAND) {
            return super.onUseItemOn(heldItem, player, hand, pos);
        }
        if (isClientSide()) {
            return true;
        }
        UUID patternP2PUnitId = heldItem.get(ModContent.PATTERN_P2P_UNIT_ID.get());
        if (patternP2PUnitId == null) {
            card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            return true;
        }
        boundPatternP2PUnitId = patternP2PUnitId;
        getHost().markForSave();
        getHost().markForUpdate();
        wake();
        card.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
        return true;
    }

    private void wake() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    @Override
    public void onNeighborChanged(net.minecraft.world.level.BlockGetter level,
                                  net.minecraft.core.BlockPos pos, net.minecraft.core.BlockPos neighbor) {
        placementStrategy = null;
        pickupStrategies = null;
        externalStrategies = null;
        energyTargetCache = null;
        wake();
    }

    @Override
    public void getBoxes(IPartCollisionHelper helper) {
        helper.addBox(3, 3, 13, 13, 13, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.get(type).getModel(isPowered(), isActive());
    }

    @Override
    public ModelData getModelData() {
        long frequencyValue = Short.toUnsignedLong(boundFrequency);
        long patternP2PUnitValue = Short.toUnsignedLong(PatternP2PUnitIdentityColors.encode(boundPatternP2PUnitId));
        if (isActive() && isPowered()) {
            frequencyValue |= 0x10000L;
            patternP2PUnitValue |= 0x10000L;
        }
        return ModelData.builder()
                .with(P2PTunnelFrequencyModelData.FREQUENCY, frequencyValue)
                .with(PatternP2PUnitModelData.PATTERN_P2P_UNIT_ID, patternP2PUnitValue)
                .build();
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(boundPatternP2PUnitId != null);
        if (boundPatternP2PUnitId != null) {
            data.writeUUID(boundPatternP2PUnitId);
        }
        PatternP2PUnitManagerPart manager = getManager();
        data.writeShort(manager == null ? 0 : manager.getFrequency());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        UUID previous = boundPatternP2PUnitId;
        short previousFrequency = boundFrequency;
        boundPatternP2PUnitId = data.readBoolean() ? data.readUUID() : null;
        boundFrequency = data.readShort();
        return changed || previousFrequency != boundFrequency
                || !java.util.Objects.equals(previous, boundPatternP2PUnitId);
    }

    private record PatternP2PUnitPortModels(IPartModel off, IPartModel on, IPartModel channel) {
        private PatternP2PUnitPortModels(ResourceLocation front) {
            this(new PartModel(P2PModels.MODEL_STATUS_OFF, P2PModels.MODEL_FREQUENCY, front, IDENTITY_MODEL),
                    new PartModel(P2PModels.MODEL_STATUS_ON, P2PModels.MODEL_FREQUENCY, front, IDENTITY_MODEL),
                    new PartModel(P2PModels.MODEL_STATUS_HAS_CHANNEL, P2PModels.MODEL_FREQUENCY, front,
                            IDENTITY_MODEL));
        }

        private IPartModel getModel(boolean hasPower, boolean hasChannel) {
            return hasPower && hasChannel ? channel : hasPower ? on : off;
        }

        private List<IPartModel> models() {
            return List.of(off, on, channel);
        }
    }

    private final class PortReturnInventory implements GenericInternalInventory, MEStorage {
        @Override public int size() { return 9; }
        @Override public @Nullable GenericStack getStack(int slot) { return null; }
        @Override public @Nullable AEKey getKey(int slot) { return null; }
        @Override public long getAmount(int slot) { return 0; }
        @Override public long getMaxAmount(AEKey key) { return getCapacity(key.getType()); }
        @Override public long getCapacity(appeng.api.stacks.AEKeyType keyType) {
            return GenericSlotCapacities.getMap().getOrDefault(keyType, Long.MAX_VALUE);
        }
        @Override public boolean canInsert() {
            PatternP2PUnitManagerPart manager = getManager();
            return isReturnPort() && manager != null && manager.getLogic().isTaskOperational();
        }
        @Override public boolean canExtract() { return false; }
        @Override public void setStack(int slot, @Nullable GenericStack newStack) { }
        @Override public boolean isSupportedType(appeng.api.stacks.AEKeyType type) { return true; }
        @Override public boolean isAllowedIn(int slot, AEKey what) {
            PatternP2PUnitManagerPart manager = getManager();
            return isReturnPort() && manager != null
                    && manager.getLogic().simulateReturned(what, 1) > 0;
        }
        @Override public long insert(int slot, AEKey what, long amount, Actionable mode) {
            PatternP2PUnitManagerPart manager = getManager();
            return isReturnPort() && manager != null
                    ? manager.getLogic().insertReturned(what, amount, mode) : 0;
        }
        @Override public long extract(int slot, AEKey what, long amount, Actionable mode) { return 0; }
        @Override public void beginBatch() { }
        @Override public void endBatch() { }
        @Override public void endBatchSuppressed() { }
        @Override public void onChange() { }
        @Override public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return insert(0, what, amount, mode);
        }
        @Override public long extract(AEKey what, long amount, Actionable mode, IActionSource source) { return 0; }
        @Override public void getAvailableStacks(appeng.api.stacks.KeyCounter out) { }
        @Override public net.minecraft.network.chat.Component getDescription() {
            return net.minecraft.network.chat.Component.translatable("item.ae2_batchcraft.pp2p_unit_port_return");
        }
    }
}
