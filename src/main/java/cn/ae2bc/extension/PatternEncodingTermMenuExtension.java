package cn.ae2bc.extension;

import cn.ae2bc.pattern.InputDirectionData;
import net.minecraft.core.Direction;

public interface PatternEncodingTermMenuExtension {
    InputDirectionData ae2bc$getInputDirections();

    void ae2bc$setInputDirection(int slot, Direction direction);
}
