package cn.ae2bc.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import cn.ae2bc.menu.PatternP2PTunnelEnergyMenu;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PatternP2PTunnelEnergyScreen extends AEBaseScreen<PatternP2PTunnelEnergyMenu> {
    private final AE2Button passiveButton;
    private final AE2Button activeButton;

    public PatternP2PTunnelEnergyScreen(PatternP2PTunnelEnergyMenu menu, Inventory playerInventory,
                                          Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        passiveButton = widgets.addButton("passive", Component.translatable(
                "gui.ae2_batchcraft.energy.mode.passive"), () -> menu.setPullEnabled(false));
        passiveButton.setTooltip(Tooltip.create(Component.translatable(
                "gui.ae2_batchcraft.energy.mode.passive.tooltip")));
        activeButton = widgets.addButton("active", Component.translatable(
                "gui.ae2_batchcraft.energy.mode.active"), () -> menu.setPullEnabled(true));
        activeButton.setTooltip(Tooltip.create(Component.translatable(
                "gui.ae2_batchcraft.energy.mode.active.tooltip")));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        passiveButton.active = menu.pullEnabled;
        activeButton.active = !menu.pullEnabled;
        Component mode = Component.translatable(menu.pullEnabled
                ? "gui.ae2_batchcraft.energy.mode.active"
                : "gui.ae2_batchcraft.energy.mode.passive");
        setTextContent("mode", Component.translatable("gui.ae2_batchcraft.energy.mode_value", mode));
        setTextContent("interval", Component.translatable(
                "gui.ae2_batchcraft.energy.pull_interval", menu.pullInterval));
    }
}
