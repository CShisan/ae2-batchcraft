package cn.ae2bc.client;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.items.PatternEncodingTermMenu;
import cn.ae2bc.logic.DirectionLayout;
import cn.ae2bc.extension.PatternEncodingTermMenuExtension;
import cn.ae2bc.pattern.MaterialOutputForm;
import appeng.api.stacks.GenericStack;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public final class SetInputDirectionScreen<C extends PatternEncodingTermMenu>
        extends AESubScreen<C, PatternEncodingTermScreen<C>> {
    private static final String STYLE = "/screens/ae2_batchcraft/set_input_direction.json";
    private final int inputSlot;

    public SetInputDirectionScreen(PatternEncodingTermScreen<C> parent, int inputSlot, DirectionLayout layout) {
        super(parent, STYLE);
        this.inputSlot = inputSlot;

        widgets.addButton("auto", DirectionText.name(null, layout), () -> selectDirection(null));
        widgets.addButton("front", DirectionText.name(layout.front(), layout), () -> selectDirection(layout.front()));
        widgets.addButton("left", DirectionText.name(layout.left(), layout), () -> selectDirection(layout.left()));
        widgets.addButton("up", DirectionText.name(Direction.UP, layout), () -> selectDirection(Direction.UP));
        widgets.addButton("right", DirectionText.name(layout.right(), layout), () -> selectDirection(layout.right()));
        widgets.addButton("down", DirectionText.name(Direction.DOWN, layout), () -> selectDirection(Direction.DOWN));
        widgets.addButton("opposite", DirectionText.name(layout.back(), layout), () -> selectDirection(layout.back()));
        for (MaterialOutputForm form : MaterialOutputForm.values()) {
            String name = form.getSerializedName();
            var button = widgets.addButton("output" + Character.toUpperCase(name.charAt(0)) + name.substring(1),
                    Component.translatable("gui.ae2_batchcraft.material_output_form." + name),
                    () -> selectOutputForm(form));
            GenericStack input = GenericStack.fromItemStack(
                    getMenu().getProcessingInputSlots()[inputSlot].getItem());
            button.active = input != null && form.supports(input.what());
        }

        var icon = getMenu().getHost().getMainMenuIcon();
        var backButton = new TabButton(Icon.BACK, icon.getHoverName(), button -> returnToParent());
        widgets.add("back", backButton);
    }

    private void selectDirection(@Nullable Direction direction) {
        ((PatternEncodingTermMenuExtension) getMenu()).ae2bc$setInputDirection(inputSlot, direction);
    }

    private void selectOutputForm(MaterialOutputForm form) {
        ((PatternEncodingTermMenuExtension) getMenu()).ae2bc$setMaterialOutputForm(inputSlot, form);
    }

    @Override
    public void onClose() {
        returnToParent();
    }
}
