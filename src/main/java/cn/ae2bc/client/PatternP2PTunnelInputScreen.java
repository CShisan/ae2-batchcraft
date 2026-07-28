package cn.ae2bc.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import cn.ae2bc.logic.PatternP2PUnitConfiguration;
import cn.ae2bc.logic.ReturnMode;
import cn.ae2bc.menu.PatternP2PTunnelInputMenu;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


public final class PatternP2PTunnelInputScreen extends PatternP2PUnitPagedScreen<PatternP2PTunnelInputMenu> {
    private final AE2Button strictButton;
    private final AE2Button unblockedButton;
    private final AECheckbox breakRecovery;
    private final AE2Button redstoneMode;
    private ValidatedIntegerField strengthInput;
    private ValidatedIntegerField pulseTimeInput;
    private ValidatedIntegerField pulsePeriodInput;

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
        breakRecovery = new AECheckbox(0, 0, 0, AECheckbox.SIZE, style, Component.empty());
        widgets.add("breakRecovery", breakRecovery);
        PatternP2PUnitConfigScreenSupport.applyBreakPortTooltip(breakRecovery);
        breakRecovery.setChangeListener(() -> menu.setBreakRecovery(breakRecovery.isSelected()));
        redstoneMode = widgets.addButton("redstoneMode", Component.empty(),
                () -> menu.setRedstoneMode(menu.redstoneMode.next()));
    }

    @Override
    protected void init() {
        super.init();
        strengthInput = addRenderableWidget(new ValidatedIntegerField(font,
                leftPos + 112, topPos + 59, 36, 16,
                Component.translatable("gui.ae2_batchcraft.pp2p_unit.redstone_strength"),
                () -> 0, () -> 15, menu::setRedstoneStrength));
        pulseTimeInput = addRenderableWidget(new ValidatedIntegerField(font,
                leftPos + 112, topPos + 80, 36, 16,
                Component.translatable("gui.ae2_batchcraft.pp2p_unit.pulse_width"),
                () -> 1, () -> menu.pulsePeriod, menu::setPulseWidth));
        pulsePeriodInput = addRenderableWidget(new ValidatedIntegerField(font,
                leftPos + 112, topPos + 101, 36, 16,
                Component.translatable("gui.ae2_batchcraft.pp2p_unit.pulse_period"),
                () -> 1, () -> PatternP2PUnitConfiguration.MAX_PULSE_TICKS, menu::setPulsePeriod));
        PatternP2PUnitConfigScreenSupport.applyRedstonePortTooltips(redstoneMode,
                strengthInput, pulseTimeInput, pulsePeriodInput);
        updatePageVisibility();
    }

    @Override
    protected int getPageHeight(Page page) {
        return switch (page) {
            case COMMON -> 80;
            case BREAK -> 62;
            case REDSTONE -> 125;
        };
    }

    @Override
    protected void updatePageVisibility() {
        boolean common = isPage(Page.COMMON);
        boolean breakPort = isPage(Page.BREAK);
        boolean redstonePort = isPage(Page.REDSTONE);

        strictButton.visible = common;
        unblockedButton.visible = common;
        breakRecovery.visible = breakPort;
        redstoneMode.visible = redstonePort;
        if (strengthInput != null) {
            strengthInput.visible = redstonePort;
            pulseTimeInput.visible = redstonePort;
            pulsePeriodInput.visible = redstonePort;
        }

        setTextHidden("return_mode", !common);
        setTextHidden("break_recovery", !breakPort);
        setTextHidden("redstone_mode_label", !redstonePort);
        setTextHidden("strength", !redstonePort);
        setTextHidden("pulse_width", !redstonePort);
        setTextHidden("pulse_width_unit", !redstonePort);
        setTextHidden("pulse_period", !redstonePort);
        setTextHidden("pulse_period_unit", !redstonePort);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        strictButton.active = menu.returnMode != ReturnMode.STRICT;
        unblockedButton.active = menu.returnMode != ReturnMode.UNBLOCKED;
        breakRecovery.setSelected(menu.breakRecovery);
        redstoneMode.setMessage(Component.translatable(
                "gui.ae2_batchcraft.pp2p_unit.redstone_mode." + menu.redstoneMode.getSerializedName()));
        strengthInput.syncValue(menu.redstoneStrength);
        pulseTimeInput.syncValue(menu.pulseWidth);
        pulsePeriodInput.syncValue(menu.pulsePeriod);
    }

}
