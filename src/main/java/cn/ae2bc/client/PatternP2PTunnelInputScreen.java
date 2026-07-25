package cn.ae2bc.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import cn.ae2bc.logic.ReturnMode;
import cn.ae2bc.menu.PatternP2PTunnelInputMenu;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PatternP2PTunnelInputScreen extends AEBaseScreen<PatternP2PTunnelInputMenu> {
    private final AE2Button strictButton;
    private final AE2Button unblockedButton;

    public PatternP2PTunnelInputScreen(PatternP2PTunnelInputMenu menu, Inventory playerInventory,
                                         Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        strictButton = widgets.addButton("returnStrict",
                Component.translatable("gui.ae2_batchcraft.return_mode.strict"),
                () -> menu.setReturnMode(ReturnMode.STRICT));
        strictButton.setTooltip(Tooltip.create(Component.translatable(
                "gui.ae2_batchcraft.return_mode.strict.tooltip")));
        unblockedButton = widgets.addButton("returnUnblocked",
                Component.translatable("gui.ae2_batchcraft.return_mode.unblocked"),
                () -> menu.setReturnMode(ReturnMode.UNBLOCKED));
        unblockedButton.setTooltip(Tooltip.create(Component.translatable(
                "gui.ae2_batchcraft.return_mode.unblocked.tooltip")));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        strictButton.active = menu.returnMode != ReturnMode.STRICT;
        unblockedButton.active = menu.returnMode != ReturnMode.UNBLOCKED;
    }
}
