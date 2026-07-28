package cn.ae2bc.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.me.helpers.MachineSource;
import cn.ae2bc.link.RoundRobin;
import cn.ae2bc.part.PatternP2PTunnelPart;
import cn.ae2bc.part.PatternTaskEndpoint;
import cn.ae2bc.part.PatternP2PUnitManagerPart;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Objects;

/**
 * Owns the main-side task admission and round-robin dispatch.
 */
public final class PatternP2PTunnelInputLogic {
    private static final String ROUND_ROBIN_CURSOR = "RoundRobinCursor";
    private static final String RETURN_MODE = "ReturnMode";
    private static final String PATTERN_P2P_UNIT_CONFIGURATION = "PatternP2PUnitConfiguration";
    private static final String PATTERN_P2P_UNIT_CONFIGURATION_REVISION = "PatternP2PUnitConfigurationRevision";

    private final IManagedGridNode mainNode;
    private final PatternP2PTunnelPart input;
    private final IActionSource actionSource;
    private final PatternMetadataCache patternMetadataCache = new PatternMetadataCache();
    private List<PatternP2PTunnelPart> outputSnapshot = List.of();
    private boolean outputSnapshotDirty = true;
    private boolean outputAvailabilityDirty = true;
    private boolean hasAvailableOutput;
    private int roundRobinCursor;
    private long cursorSaveTick = Long.MIN_VALUE;
    private ReturnMode returnMode = ReturnMode.UNBLOCKED;
    private PatternP2PUnitConfiguration patternP2PUnitConfiguration = PatternP2PUnitConfiguration.DEFAULT;
    private long patternP2PUnitConfigurationRevision;

    public PatternP2PTunnelInputLogic(IManagedGridNode mainNode, PatternP2PTunnelPart input) {
        this.mainNode = mainNode;
        this.input = input;
        this.actionSource = new MachineSource(mainNode::getNode);
    }

    public boolean hasAvailableOutput() {
        if (!mainNode.isActive() || !input.hasConfiguredFrequency()) {
            return false;
        }
        if (outputAvailabilityDirty) {
            hasAvailableOutput = false;
            for (var output : getTaskEndpoints()) {
                if (output.isOperationalTaskEndpoint() && output.canAcceptTask()) {
                    hasAvailableOutput = true;
                    break;
                }
            }
            outputAvailabilityDirty = false;
        }
        return hasAvailableOutput;
    }

    public ReturnMode getReturnMode() {
        return returnMode;
    }

    public void setReturnMode(ReturnMode mode) {
        Objects.requireNonNull(mode, "mode");
        if (returnMode == mode) {
            return;
        }
        returnMode = mode;
        setPatternP2PUnitConfiguration(patternP2PUnitConfiguration.withReturnMode(mode));
    }

    public PatternP2PUnitConfiguration getPatternP2PUnitConfiguration() {
        return patternP2PUnitConfiguration;
    }

    public long getPatternP2PUnitConfigurationRevision() {
        return patternP2PUnitConfigurationRevision;
    }

    public void setPatternP2PUnitConfiguration(PatternP2PUnitConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        if (patternP2PUnitConfiguration.equals(configuration)) {
            return;
        }
        patternP2PUnitConfiguration = configuration;
        returnMode = configuration.returnMode();
        patternP2PUnitConfigurationRevision++;
        synchronizeSettings();
        input.getHost().markForSave();
    }

    public void synchronizeSettings() {
        for (var output : getOutputSnapshot()) {
            output.getOutputLogic().applyInputSettings(returnMode);
        }
        for (var manager : getPatternP2PUnitManagers()) {
            manager.getLogic().applyMainConfiguration(patternP2PUnitConfiguration,
                    patternP2PUnitConfigurationRevision);
        }
    }

    public void invalidateOutputs() {
        outputSnapshotDirty = true;
        outputAvailabilityDirty = true;
    }

    public void invalidateOutputAvailability() {
        outputAvailabilityDirty = true;
    }

    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs) {
        if (!mainNode.isActive() || !input.hasConfiguredFrequency()) {
            return false;
        }

        List<PatternTaskEndpoint> outputs = getTaskEndpoints();
        int size = outputs.size();
        var metadata = patternMetadataCache.get(pattern);
        if (!metadata.isValid()) {
            return false;
        }
        for (int offset = 0; offset < size; offset++) {
            int index = RoundRobin.index(roundRobinCursor, offset, size);
            var output = outputs.get(index);
            if (!output.isOperationalTaskEndpoint() || !output.canAcceptTask()) {
                continue;
            }
            if (output.tryAcceptPattern(pattern, metadata, inputs, actionSource)) {
                roundRobinCursor = RoundRobin.advance(index, size);
                markCursorForSave();
                return true;
            }
        }
        return false;
    }

    private List<PatternP2PTunnelPart> getOutputSnapshot() {
        if (outputSnapshotDirty) {
            outputSnapshot = input.getOutputs().stream()
                    .filter(PatternP2PTunnelPart::isStandardOutput)
                    .toList();
            outputSnapshotDirty = false;
            outputAvailabilityDirty = true;
        }
        return outputSnapshot;
    }

    private List<PatternTaskEndpoint> getTaskEndpoints() {
        var outputs = new java.util.ArrayList<PatternTaskEndpoint>(getOutputSnapshot());
        outputs.addAll(getPatternP2PUnitManagers());
        return outputs;
    }

    private List<PatternP2PUnitManagerPart> getPatternP2PUnitManagers() {
        var grid = mainNode.getGrid();
        if (grid == null || !input.hasConfiguredFrequency()) {
            return List.of();
        }
        return grid.getMachines(PatternP2PUnitManagerPart.class).stream()
                .filter(manager -> manager.getFrequency() == input.getFrequency())
                .toList();
    }

    private void markCursorForSave() {
        var level = input.getLevel();
        if (level == null) {
            input.getHost().markForSave();
            return;
        }
        long currentTick = level.getGameTime();
        if (cursorSaveTick != currentTick) {
            cursorSaveTick = currentTick;
            input.getHost().markForSave();
        }
    }

    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        roundRobinCursor = data.getInt(ROUND_ROBIN_CURSOR);
        returnMode = data.contains(RETURN_MODE)
                ? ReturnMode.fromId(data.getByte(RETURN_MODE)) : ReturnMode.UNBLOCKED;
        patternP2PUnitConfiguration = data.contains(PATTERN_P2P_UNIT_CONFIGURATION)
                ? PatternP2PUnitConfiguration.read(data.getCompound(PATTERN_P2P_UNIT_CONFIGURATION))
                : PatternP2PUnitConfiguration.DEFAULT.withReturnMode(returnMode);
        returnMode = patternP2PUnitConfiguration.returnMode();
        patternP2PUnitConfigurationRevision = data.getLong(PATTERN_P2P_UNIT_CONFIGURATION_REVISION);
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        data.putInt(ROUND_ROBIN_CURSOR, roundRobinCursor);
        data.putByte(RETURN_MODE, (byte) returnMode.getId());
        data.put(PATTERN_P2P_UNIT_CONFIGURATION, patternP2PUnitConfiguration.write());
        data.putLong(PATTERN_P2P_UNIT_CONFIGURATION_REVISION, patternP2PUnitConfigurationRevision);
    }
}
