package cn.ae2bc.client;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.items.PatternEncodingTermMenu;
import cn.ae2bc.logic.DirectionLayout;
import cn.ae2bc.extension.PatternEncodingTermMenuExtension;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public final class SetInputDirectionScreen<C extends PatternEncodingTermMenu>
        extends AESubScreen<C, PatternEncodingTermScreen<C>> {
    private static final String STYLE = "/screens/ae2_batchcraft/set_input_direction.json";
    private final int inputSlot;

    public SetInputDirectionScreen(PatternEncodingTermScreen<C> parent, int inputSlot, DirectionLayout layout) {
        super(parent, STYLE);
        this.inputSlot = inputSlot;

        widgets.addButton("auto", DirectionText.name(null, layout), () -> select(null));
        widgets.addButton("front", DirectionText.name(layout.front(), layout), () -> select(layout.front()));
        widgets.addButton("left", DirectionText.name(layout.left(), layout), () -> select(layout.left()));
        widgets.addButton("up", DirectionText.name(Direction.UP, layout), () -> select(Direction.UP));
        widgets.addButton("right", DirectionText.name(layout.right(), layout), () -> select(layout.right()));
        widgets.addButton("down", DirectionText.name(Direction.DOWN, layout), () -> select(Direction.DOWN));
        widgets.addButton("opposite", DirectionText.name(layout.back(), layout), () -> select(layout.back()));

        var icon = getMenu().getHost().getMainMenuIcon();
        var backButton = new TabButton(Icon.BACK, icon.getHoverName(), button -> returnToParent());
        widgets.add("back", backButton);
    }

    private void select(@Nullable Direction direction) {
        ((PatternEncodingTermMenuExtension) getMenu()).ae2bc$setInputDirection(inputSlot, direction);
        returnToParent();
    }

    @Override
    public void onClose() {
        returnToParent();
    }
}
