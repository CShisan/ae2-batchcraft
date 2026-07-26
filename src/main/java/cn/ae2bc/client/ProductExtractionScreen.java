package cn.ae2bc.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import cn.ae2bc.logic.ProductExtractionSettings;
import cn.ae2bc.menu.ProductExtractionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ProductExtractionScreen extends AEBaseScreen<ProductExtractionMenu> {
    private EditBox intervalInput;
    private EditBox amountInput;
    private final AECheckbox modeToggle;
    private final AE2Button intervalReset;
    private final AE2Button amountReset;
    private boolean syncing;

    public ProductExtractionScreen(ProductExtractionMenu menu, Inventory playerInventory,
                                   Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        AESubScreen.addBackButton(menu, "back", widgets);
        modeToggle = widgets.addCheckbox("modeToggle", Component.empty(), this::changeFilterMode);
        modeToggle.setTooltip(Tooltip.create(Component.translatable(
                "gui.ae2_batchcraft.product_extraction.toggle")));
        intervalReset = widgets.addButton("intervalReset",
                Component.translatable("gui.ae2_batchcraft.product_extraction.reset"),
                () -> menu.setInterval(ProductExtractionSettings.DEFAULT_INTERVAL));
        amountReset = widgets.addButton("amountReset",
                Component.translatable("gui.ae2_batchcraft.product_extraction.reset"),
                () -> menu.setAmount(ProductExtractionSettings.DEFAULT_AMOUNT));
    }

    @Override
    protected void init() {
        super.init();
        intervalInput = addRenderableWidget(new EditBox(font, leftPos + 58, topPos + 19, 40, 16,
                Component.translatable("gui.ae2_batchcraft.product_extraction.interval")));
        amountInput = addRenderableWidget(new EditBox(font, leftPos + 58, topPos + 40, 40, 16,
                Component.translatable("gui.ae2_batchcraft.product_extraction.amount")));
        intervalInput.setResponder(value -> updateInterval());
        amountInput.setResponder(value -> updateAmount());
        syncInputs();
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY,
                       float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        drawSlotBackgrounds(guiGraphics, offsetX, offsetY, ProductExtractionMenu.MARKER_SLOT);
        drawSlotBackgrounds(guiGraphics, offsetX, offsetY, SlotSemantics.PLAYER_INVENTORY);
        drawSlotBackgrounds(guiGraphics, offsetX, offsetY, SlotSemantics.PLAYER_HOTBAR);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (!intervalInput.isFocused() && !amountInput.isFocused()) {
            syncInputs();
        }
        modeToggle.active = menu.cardInstalled;
        modeToggle.setSelected(menu.whitelist);
        intervalReset.active = menu.cardInstalled
                && menu.interval != ProductExtractionSettings.DEFAULT_INTERVAL;
        amountReset.active = menu.cardInstalled
                && menu.amount != ProductExtractionSettings.DEFAULT_AMOUNT;
    }

    private void syncInputs() {
        if (intervalInput == null || amountInput == null) {
            return;
        }
        syncing = true;
        intervalInput.setValue(Integer.toString(menu.interval));
        amountInput.setValue(Integer.toString(menu.amount));
        boolean active = menu.cardInstalled;
        intervalInput.setEditable(active);
        amountInput.setEditable(active);
        syncing = false;
    }

    private void updateInterval() {
        if (syncing || !menu.cardInstalled) {
            return;
        }
        try {
            int value = Integer.parseInt(intervalInput.getValue());
            if (value >= ProductExtractionSettings.MIN_INTERVAL && value <= ProductExtractionSettings.MAX_INTERVAL) {
                menu.setInterval(value);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateAmount() {
        if (syncing || !menu.cardInstalled) {
            return;
        }
        try {
            int value = Integer.parseInt(amountInput.getValue());
            if (value >= ProductExtractionSettings.MIN_AMOUNT && value <= ProductExtractionSettings.MAX_AMOUNT) {
                menu.setAmount(value);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void changeFilterMode() {
        if (menu.cardInstalled) {
            menu.setWhitelist(modeToggle.isSelected());
        }
    }

    private void drawSlotBackgrounds(GuiGraphics guiGraphics, int offsetX, int offsetY, SlotSemantic semantic) {
        for (var slot : menu.getSlots(semantic)) {
            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(offsetX + slot.x - 1, offsetY + slot.y - 1)
                    .blit(guiGraphics);
        }
    }

}
