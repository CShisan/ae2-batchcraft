package cn.ae2bc.part;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartHelper;
import appeng.api.parts.PartModels;
import appeng.api.util.AECableType;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.P2PModels;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.logic.FairEnergyDistributor;
import cn.ae2bc.menu.PatternP2PTunnelEnergyMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public final class PatternP2PTunnelEnergyPart extends EnergyAcceptorPart {
    public static final int PULL_INTERVAL = 5;
    private static final P2PModels MODELS = new P2PModels(
            ResourceLocation.fromNamespaceAndPath(Ae2bcMod.MOD_ID, "part/p2p/pattern_p2p_tunnel_energy"));

    private final IEnergyStorage energyStorage = new DynamicEnergyStorage();
    private boolean transferringEnergy;
    private boolean pullEnabled;
    private int pullInterval = PULL_INTERVAL;
    private int distributionCursor;
    private long demandCacheTick = Long.MIN_VALUE;
    private int demandCacheFe;
    private IGrid eligibleOutputsGrid;
    private long eligibleOutputsTick = Long.MIN_VALUE;
    private List<PatternP2PTunnelPart> eligibleOutputs = List.of();
    private BlockCapabilityCache<IEnergyStorage, Direction> sourceEnergyCache;

    public PatternP2PTunnelEnergyPart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().addService(IGridTickable.class, new PullTicker());
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public boolean isPullEnabled() {
        return pullEnabled;
    }

    public int getPullInterval() {
        return pullInterval;
    }

    public void setPullEnabled(boolean enabled) {
        if (pullEnabled == enabled) {
            return;
        }
        pullEnabled = enabled;
        pullInterval = PULL_INTERVAL;
        getHost().markForSave();
        getHost().markForUpdate();
        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (enabled) {
                grid.getTickManager().wakeDevice(getMainNode().getNode());
            } else {
                grid.getTickManager().sleepDevice(getMainNode().getNode());
            }
        }
    }

    public static void registerModels() {
        PartModels.registerModels(MODELS.getModels().stream()
                .flatMap(model -> model.getModels().stream())
                .toList());
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        pullEnabled = data.getBoolean("PullEnabled");
        pullInterval = PULL_INTERVAL;
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean("PullEnabled", pullEnabled);
    }

    @Override
    protected double getFunnelPowerDemand(double maxRequired) {
        if (maxRequired <= 0) {
            return 0;
        }

        double localDemand = super.getFunnelPowerDemand(maxRequired);
        double remainingCapacity = Math.max(0, maxRequired - localDemand);
        int remoteLimit = aeToFeFloor(remainingCapacity);
        int remoteDemand = getRemoteDemandFe(getEligibleOutputs(), remoteLimit);
        return Math.min(maxRequired, localDemand + PowerUnit.FE.convertTo(PowerUnit.AE, remoteDemand));
    }

    @Override
    protected double funnelPowerIntoStorage(double power, Actionable mode) {
        if (power <= 0 || transferringEnergy) {
            return power;
        }

        transferringEnergy = true;
        try {
            double remaining = super.funnelPowerIntoStorage(power, mode);
            int availableFe = aeToFeFloor(remaining);
            if (availableFe <= 0) {
                return remaining;
            }

            int acceptedFe = distributeToOutputs(availableFe, mode == Actionable.SIMULATE);
            double acceptedAe = PowerUnit.FE.convertTo(PowerUnit.AE, acceptedFe);
            return Math.max(0, remaining - acceptedAe);
        } finally {
            transferringEnergy = false;
        }
    }

    private int distributeToOutputs(int offered, boolean simulate) {
        List<PatternP2PTunnelPart> outputs = getEligibleOutputs();
        int outputCount = outputs.size();
        if (offered <= 0 || outputCount == 0) {
            return 0;
        }

        int startIndex = Math.floorMod(distributionCursor, outputCount);
        int accepted = FairEnergyDistributor.distribute(offered, outputCount, startIndex,
                (index, amount) -> outputs.get(index).receiveExternalEnergy(amount, simulate));
        if (!simulate) {
            distributionCursor = (startIndex + 1) % outputCount;
        }
        return accepted;
    }

    private int getPullDemandFe() {
        double maxAe = PowerUnit.FE.convertTo(PowerUnit.AE, Integer.MAX_VALUE);
        return aeToFeCeil(getFunnelPowerDemand(maxAe));
    }

    private int getCachedDemandFe() {
        var blockEntity = getBlockEntity();
        var level = blockEntity == null ? null : blockEntity.getLevel();
        if (level == null) {
            return 0;
        }

        long gameTime = level.getGameTime();
        if (demandCacheTick != gameTime) {
            demandCacheFe = getPullDemandFe();
            demandCacheTick = gameTime;
        }
        return demandCacheFe;
    }

    private void invalidateDemandCache() {
        demandCacheTick = Long.MIN_VALUE;
    }

    private PullOutcome pullEnergyFromAdjacent() {
        int demand = getPullDemandFe();
        if (demand <= 0) {
            return PullOutcome.NO_DEMAND;
        }

        Direction side = getSide();
        if (side == null || !(getLevel() instanceof ServerLevel level)) {
            return PullOutcome.NO_SOURCE;
        }

        var targetPos = getBlockEntity().getBlockPos().relative(side);
        var targetHost = PartHelper.getPartHost(level, targetPos);
        if (targetHost != null
                && targetHost.getPart(side.getOpposite()) instanceof PatternP2PTunnelEnergyPart) {
            return PullOutcome.NO_SOURCE;
        }

        Direction targetSide = side.getOpposite();
        if (sourceEnergyCache == null
                || sourceEnergyCache.level() != level
                || !sourceEnergyCache.pos().equals(targetPos)
                || sourceEnergyCache.context() != targetSide) {
            sourceEnergyCache = BlockCapabilityCache.create(
                    Capabilities.EnergyStorage.BLOCK, level, targetPos, targetSide);
        }
        var source = sourceEnergyCache.getCapability();
        if (source == null || !source.canExtract()) {
            return PullOutcome.NO_SOURCE;
        }

        int available = source.extractEnergy(demand, true);
        int receivable = getEnergyStorage().receiveEnergy(available, true);
        if (receivable <= 0) {
            return PullOutcome.NO_SOURCE;
        }

        int extracted = source.extractEnergy(receivable, false);
        int accepted = getEnergyStorage().receiveEnergy(extracted, false);
        int rejected = extracted - accepted;
        if (rejected > 0 && source.canReceive()) {
            source.receiveEnergy(rejected, false);
        }

        int remainingDemand = getPullDemandFe();
        boolean sourceHasMore = remainingDemand > 0
                && source.extractEnergy(remainingDemand, true) > 0;
        return new PullOutcome(accepted, remainingDemand, sourceHasMore);
    }

    private int getRemoteDemandFe(List<PatternP2PTunnelPart> outputs, int limit) {
        int demand = 0;
        for (var output : outputs) {
            int remaining = limit - demand;
            if (remaining <= 0) {
                break;
            }
            demand += output.receiveExternalEnergy(remaining, true);
        }
        return demand;
    }

    private List<PatternP2PTunnelPart> getEligibleOutputs() {
        var grid = getMainNode().getGrid();
        if (grid == null) {
            eligibleOutputsGrid = null;
            eligibleOutputsTick = Long.MIN_VALUE;
            eligibleOutputs = List.of();
            return eligibleOutputs;
        }
        var level = getLevel();
        long gameTime = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (eligibleOutputsGrid != grid || eligibleOutputsTick != gameTime) {
            eligibleOutputs = grid.getMachines(PatternP2PTunnelPart.class).stream()
                    .filter(PatternP2PTunnelPart::isOperationalOutput)
                    .toList();
            eligibleOutputsGrid = grid;
            eligibleOutputsTick = gameTime;
        }
        return eligibleOutputs;
    }

    private static int aeToFeFloor(double amount) {
        double converted = PowerUnit.AE.convertTo(PowerUnit.FE, Math.max(0, amount));
        if (converted >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) converted;
    }

    private static int aeToFeCeil(double amount) {
        double converted = PowerUnit.AE.convertTo(PowerUnit.FE, Math.max(0, amount));
        if (converted >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.ceil(converted);
    }

    @Override
    public void getBoxes(IPartCollisionHelper helper) {
        helper.addBox(5, 5, 12, 11, 11, 13);
        helper.addBox(3, 3, 13, 13, 13, 14);
        helper.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!isClientSide()) {
            openConfigurationMenu(player);
        }
        return true;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (hand == InteractionHand.MAIN_HAND && heldItem.isEmpty()) {
            if (!isClientSide()) {
                openConfigurationMenu(player);
            }
            return true;
        }
        return super.onUseItemOn(heldItem, player, hand, pos);
    }

    private void openConfigurationMenu(Player player) {
        MenuOpener.open(PatternP2PTunnelEnergyMenu.TYPE, player, MenuLocators.forPart(this));
    }

    @Override
    protected boolean shouldSendMissingChannelStateToClient() {
        return false;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(isPowered(), isActive());
    }

    private final class PullTicker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(1, PULL_INTERVAL, !pullEnabled, PULL_INTERVAL);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!pullEnabled || transferringEnergy) {
                pullInterval = PULL_INTERVAL;
                return TickRateModulation.SLEEP;
            }

            var result = pullEnergyFromAdjacent();
            if (result.shouldAccelerate()) {
                pullInterval = Math.max(1, pullInterval - 2);
                return TickRateModulation.FASTER;
            }
            if (result.remainingDemand() <= 0 || !result.sourceHasMore()) {
                pullInterval = PULL_INTERVAL;
                return TickRateModulation.IDLE;
            }

            pullInterval = Math.min(PULL_INTERVAL, pullInterval + 1);
            return TickRateModulation.SLOWER;
        }
    }

    private final class DynamicEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }
            double overflow = injectExternalPower(PowerUnit.FE, maxReceive,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE);
            int accepted = (int) Math.clamp(maxReceive - overflow, 0, maxReceive);
            if (!simulate && accepted > 0) {
                invalidateDemandCache();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return getCachedDemandFe();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private record PullOutcome(int accepted, int remainingDemand, boolean sourceHasMore) {
        private static final PullOutcome NO_DEMAND = new PullOutcome(0, 0, false);
        private static final PullOutcome NO_SOURCE = new PullOutcome(0, 1, false);

        private boolean shouldAccelerate() {
            return accepted > 0 && remainingDemand > 0 && sourceHasMore;
        }
    }
}
