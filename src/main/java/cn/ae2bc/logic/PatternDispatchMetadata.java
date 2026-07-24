package cn.ae2bc.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.pattern.InputDirectionData;
import cn.ae2bc.registry.ModContent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class PatternDispatchMetadata {
    private static final PatternDispatchMetadata INVALID = new PatternDispatchMetadata(
            Map.of(), null, InputDirectionData.EMPTY, false);

    private final Map<AEKey, Long> declaredOutputs;
    private final GenericStack primaryOutput;
    private final InputDirectionData inputDirections;
    private final boolean explicitDirections;

    private PatternDispatchMetadata(Map<AEKey, Long> declaredOutputs, GenericStack primaryOutput,
                                    InputDirectionData inputDirections, boolean explicitDirections) {
        this.declaredOutputs = declaredOutputs;
        this.primaryOutput = primaryOutput;
        this.inputDirections = inputDirections;
        this.explicitDirections = explicitDirections;
    }

    static PatternDispatchMetadata create(IPatternDetails pattern) {
        try {
            var patternOutputs = pattern.getOutputs();
            Map<AEKey, Long> outputs = new LinkedHashMap<>(patternOutputs.size());
            for (GenericStack output : patternOutputs) {
                if (output == null || output.amount() <= 0) {
                    return INVALID;
                }
                outputs.merge(output.what(), output.amount(), Math::addExact);
            }
            GenericStack primary = pattern.getPrimaryOutput();
            if (primary == null || primary.amount() <= 0 || !outputs.containsKey(primary.what())) {
                return INVALID;
            }

            InputDirectionData directions = getInputDirections(pattern);
            return new PatternDispatchMetadata(
                    Collections.unmodifiableMap(outputs), primary, directions,
                    hasExplicitDirections(pattern, directions));
        } catch (ArithmeticException exception) {
            Ae2bcMod.LOGGER.warn("Pattern output amounts overflow while preparing dispatch metadata", exception);
            return INVALID;
        }
    }

    private static InputDirectionData getInputDirections(IPatternDetails pattern) {
        if (!(pattern instanceof AEProcessingPattern processingPattern)) {
            return InputDirectionData.EMPTY;
        }
        InputDirectionData directions = processingPattern.getDefinition().get(ModContent.INPUT_DIRECTIONS.get());
        return directions == null ? InputDirectionData.EMPTY : directions;
    }

    private static boolean hasExplicitDirections(IPatternDetails pattern, InputDirectionData directions) {
        if (!(pattern instanceof AEProcessingPattern processingPattern)) {
            return false;
        }
        var sparseInputs = processingPattern.getSparseInputs();
        for (int slot = 0; slot < sparseInputs.size() && InputDirectionData.isValidSlot(slot); slot++) {
            if (sparseInputs.get(slot) != null && directions.getDirection(slot) != null) {
                return true;
            }
        }
        return false;
    }

    boolean isValid() {
        return primaryOutput != null;
    }

    Map<AEKey, Long> declaredOutputs() {
        return declaredOutputs;
    }

    GenericStack primaryOutput() {
        if (primaryOutput == null) {
            throw new IllegalStateException("Invalid pattern metadata has no primary output");
        }
        return primaryOutput;
    }

    InputDirectionData inputDirections() {
        return inputDirections;
    }

    boolean hasExplicitDirections() {
        return explicitDirections;
    }
}
