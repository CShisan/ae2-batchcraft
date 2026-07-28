package cn.ae2bc.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import cn.ae2bc.logic.ReturnMode;
import cn.ae2bc.logic.PatternP2PUnitConfiguration;
import cn.ae2bc.menu.PatternP2PUnitManagerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.EnumMap;
import java.util.Map;

public final class PatternP2PUnitManagerScreen extends PatternP2PUnitPagedScreen<PatternP2PUnitManagerMenu> {
    private final AECheckbox syncMain;
    private final AECheckbox breakRecovery;
    private final Map<ReturnMode, AE2Button> returnButtons = new EnumMap<>(ReturnMode.class);
    private final AE2Button redstoneMode;
    private ValidatedIntegerField strengthInput;
    private ValidatedIntegerField pulseTimeInput;
    private ValidatedIntegerField pulsePeriodInput;

    public PatternP2PUnitManagerScreen(PatternP2PUnitManagerMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
        syncMain = new AECheckbox(0, 0, 0, AECheckbox.SIZE, style,
                Component.translatable("gui.ae2_batchcraft.sync_input_settings"));
        widgets.add("syncMain", syncMain);
        syncMain.setChangeListener(() -> menu.setSyncMain(syncMain.isSelected()));
        for (ReturnMode mode : ReturnMode.values()) {
            var button = widgets.addButton("return" + camel(mode.getSerializedName()),
                    Component.translatable("gui.ae2_batchcraft.return_mode." + mode.getSerializedName()),
                    () -> menu.setReturnMode(mode));
            returnButtons.put(mode, button);
        }
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
            case COMMON -> 96;
            case BREAK -> 62;
            case REDSTONE -> 125;
        };
    }

    @Override
    protected void updatePageVisibility() {
        boolean common = isPage(Page.COMMON);
        boolean breakPort = isPage(Page.BREAK);
        boolean redstonePort = isPage(Page.REDSTONE);

        syncMain.visible = common;
        for (var button : returnButtons.values()) {
            button.visible = common;
        }
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
        syncMain.setSelected(menu.syncMain);
        breakRecovery.setSelected(menu.breakRecovery);
        boolean editable = !menu.syncMain;
        for (var entry : returnButtons.entrySet()) entry.getValue().active = editable && entry.getKey() != menu.returnMode;
        redstoneMode.active = editable;
        breakRecovery.active = editable;
        strengthInput.setEditable(editable);
        pulseTimeInput.setEditable(editable);
        pulsePeriodInput.setEditable(editable);
        redstoneMode.setMessage(Component.translatable(
                "gui.ae2_batchcraft.pp2p_unit.redstone_mode." + menu.redstoneMode.getSerializedName()));
        strengthInput.syncValue(menu.redstoneStrength);
        pulseTimeInput.syncValue(menu.pulseWidth);
        pulsePeriodInput.syncValue(menu.pulsePeriod);
    }

    private static String camel(String value) {
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (char c : value.toCharArray()) {
            if (c == '_') upper = true;
            else { result.append(upper ? Character.toUpperCase(c) : c); upper = false; }
        }
        return result.toString();
    }
}
