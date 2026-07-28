package cn.ae2bc.extension;

import cn.ae2bc.pattern.MaterialInputConfigData;
import cn.ae2bc.pattern.MaterialOutputForm;
import net.minecraft.core.Direction;

public interface PatternEncodingTermMenuExtension {
    MaterialInputConfigData ae2bc$getMaterialInputConfig();

    void ae2bc$setInputDirection(int slot, Direction direction);

    void ae2bc$setMaterialOutputForm(int slot, MaterialOutputForm form);
}
