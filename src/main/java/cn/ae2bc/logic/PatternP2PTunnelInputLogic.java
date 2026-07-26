package cn.ae2bc.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.me.helpers.MachineSource;
import cn.ae2bc.link.RoundRobin;
import cn.ae2bc.part.PatternP2PTunnelPart;
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
            for (var output : getOutputSnapshot()) {
                if (output.isOperationalOutput() && output.getOutputLogic().canAcceptTask()) {
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
        synchronizeSettings();
        input.getHost().markForSave();
    }

    public void synchronizeSettings() {
        for (var output : getOutputSnapshot()) {
            output.getOutputLogic().applyInputSettings(returnMode);
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

        List<PatternP2PTunnelPart> outputs = getOutputSnapshot();
        int size = outputs.size();
        var metadata = patternMetadataCache.get(pattern);
        if (!metadata.isValid()) {
            return false;
        }
        for (int offset = 0; offset < size; offset++) {
            int index = RoundRobin.index(roundRobinCursor, offset, size);
            var output = outputs.get(index);
            if (!output.isOperationalOutput() || !output.getOutputLogic().canAcceptTask()) {
                continue;
            }
            if (output.getOutputLogic().tryAcceptPattern(pattern, metadata, inputs, actionSource)) {
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
                    .filter(PatternP2PTunnelPart::isOutput)
                    .toList();
            outputSnapshotDirty = false;
            outputAvailabilityDirty = true;
        }
        return outputSnapshot;
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
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        data.putInt(ROUND_ROBIN_CURSOR, roundRobinCursor);
        data.putByte(RETURN_MODE, (byte) returnMode.getId());
    }
}
