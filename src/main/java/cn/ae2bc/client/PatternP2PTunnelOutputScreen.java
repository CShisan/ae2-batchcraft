package cn.ae2bc.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import cn.ae2bc.logic.ReturnMode;
import cn.ae2bc.menu.PatternP2PTunnelOutputMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.EnumMap;
import java.util.Map;

public final class PatternP2PTunnelOutputScreen extends AEBaseScreen<PatternP2PTunnelOutputMenu> {
    private final AECheckbox syncInputSettings;
    private final Map<ReturnMode, AE2Button> returnButtons = new EnumMap<>(ReturnMode.class);

    public PatternP2PTunnelOutputScreen(PatternP2PTunnelOutputMenu menu, Inventory playerInventory,
                                          Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        for (ReturnMode mode : ReturnMode.values()) {
            String modeName = mode.getSerializedName();
            returnButtons.put(mode, widgets.addButton("return" + Character.toUpperCase(modeName.charAt(0))
                            + modeName.substring(1),
                    Component.translatable("gui.ae2_batchcraft.return_mode." + modeName),
                    () -> menu.setReturnMode(mode)));
        }
        syncInputSettings = new AECheckbox(0, 0, 0, AECheckbox.SIZE, style,
                Component.translatable("gui.ae2_batchcraft.sync_input_settings"));
        widgets.add("syncInputSettings", syncInputSettings);
        syncInputSettings.setChangeListener(() -> menu.setSyncInputSettings(syncInputSettings.isSelected()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        syncInputSettings.setSelected(menu.syncInputSettings);
        for (var entry : returnButtons.entrySet()) {
            entry.getValue().active = !menu.syncInputSettings && entry.getKey() != menu.returnMode;
        }
        Component modeName = Component.translatable(
                "gui.ae2_batchcraft.return_mode." + menu.returnMode.getSerializedName());
        setTextContent("return_mode", Component.translatable(
                "gui.ae2_batchcraft.return_mode_value", modeName));
    }
}
