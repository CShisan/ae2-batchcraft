package cn.ae2bc.menu;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.api.inventories.InternalInventory;
import appeng.items.tools.MemoryCardItem;
import appeng.me.service.P2PService;
import appeng.util.Platform;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.item.WirelessPatternP2PPlacerItem;
import cn.ae2bc.placer.P2PPlacementService;
import cn.ae2bc.placer.P2PPlacerMenuHost;
import cn.ae2bc.placer.P2PPlacerMode;
import cn.ae2bc.placer.P2PPlacerSelection;
import cn.ae2bc.placer.P2PPlacerSettings;
import cn.ae2bc.registry.ModContent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public final class P2PPlacerMenu extends AEBaseMenu {
    private static final String SET_MODE = "setMode";
    private static final String SET_DIRECTION = "setDirection";
    private static final String ADJUST_X = "adjustX";
    private static final String ADJUST_Y = "adjustY";
    private static final String ADJUST_Z = "adjustZ";
    private static final String RESET_OFFSETS = "resetOffsets";
    private static final String CLEAR_SELECTION = "clearSelection";
    private static final String EXECUTE = "execute";
    private static final String LOAD_FREQUENCY = "loadFrequency";
    private static final String RESET_FREQUENCY = "resetFrequency";

    public static final MenuType<P2PPlacerMenu> TYPE = MenuTypeBuilder
            .create(P2PPlacerMenu::new, P2PPlacerMenuHost.class)
            .withMenuTitle(host -> host.getItemStack().getHoverName())
            .buildUnregistered(ResourceLocation.fromNamespaceAndPath(Ae2bcMod.MOD_ID, "wp2pp_placer"));

    private final P2PPlacerMenuHost host;

    @GuiSync(0)
    public P2PPlacerMode mode = P2PPlacerMode.OUTPUT;
    @GuiSync(1)
    public Direction direction = Direction.NORTH;
    @GuiSync(2)
    public int offsetX;
    @GuiSync(3)
    public int offsetY;
    @GuiSync(4)
    public int offsetZ;
    @GuiSync(5)
    public P2PPlacerSelection.Validation selectionState = P2PPlacerSelection.Validation.INCOMPLETE;
    @GuiSync(6)
    public int sizeX;
    @GuiSync(7)
    public int sizeY;
    @GuiSync(8)
    public int sizeZ;
    @GuiSync(9)
    public boolean hasCable;
    @GuiSync(10)
    public int frequency;

    public P2PPlacerMenu(int id, Inventory playerInventory, P2PPlacerMenuHost host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        addSlot(new CableMarkerSlot(host.getCableFilter()), SlotSemantics.CONFIG);
        for (int i = 0; i < WirelessPatternP2PPlacerItem.MATERIAL_SLOT_COUNT; i++) {
            addSlot(new AppEngSlot(host.getMaterials(), i), SlotSemantics.STORAGE);
        }
        createPlayerInventorySlots(playerInventory);

        registerClientAction(SET_MODE, P2PPlacerMode.class, this::handleSetMode);
        registerClientAction(SET_DIRECTION, Direction.class, this::handleSetDirection);
        registerClientAction(ADJUST_X, Integer.class, value -> handleAdjustOffset(value, 0));
        registerClientAction(ADJUST_Y, Integer.class, value -> handleAdjustOffset(value, 1));
        registerClientAction(ADJUST_Z, Integer.class, value -> handleAdjustOffset(value, 2));
        registerClientAction(RESET_OFFSETS, this::handleResetOffsets);
        registerClientAction(CLEAR_SELECTION, this::handleClearSelection);
        registerClientAction(EXECUTE, this::handleExecute);
        registerClientAction(LOAD_FREQUENCY, Boolean.class, this::handleLoadFrequency);
        registerClientAction(RESET_FREQUENCY, this::handleResetFrequency);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            var stack = host.getItemStack();
            var settings = stack.getOrDefault(ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT);
            mode = settings.mode();
            direction = settings.direction();
            offsetX = settings.offsetX();
            offsetY = settings.offsetY();
            offsetZ = settings.offsetZ();
            var selection = stack.get(ModContent.PLACER_SELECTION.get());
            hasCable = WirelessPatternP2PPlacerItem.isUsableCable(
                    WirelessPatternP2PPlacerItem.getMarkedCable(stack));
            frequency = Short.toUnsignedInt(stack.getOrDefault(
                    ModContent.PLACER_FREQUENCY.get(), (short) 0));
            if (selection == null) {
                selectionState = P2PPlacerSelection.Validation.INCOMPLETE;
                sizeX = sizeY = sizeZ = 0;
            } else {
                selectionState = selection.validate();
                sizeX = selection.sizeX();
                sizeY = selection.sizeY();
                sizeZ = selection.sizeZ();
            }
        }
        super.broadcastChanges();
    }

    public void setMode(P2PPlacerMode value) {
        if (value != null) {
            mode = value;
            sendClientAction(SET_MODE, value);
        }
    }

    public void setDirection(Direction value) {
        if (value != null) {
            direction = value;
            sendClientAction(SET_DIRECTION, value);
        }
    }

    public void adjustOffset(int axis, int delta) {
        if (delta == 0) {
            return;
        }
        switch (axis) {
            case 0 -> offsetX = P2PPlacerSettings.clampOffset(offsetX + delta);
            case 1 -> offsetY = P2PPlacerSettings.clampOffset(offsetY + delta);
            case 2 -> offsetZ = P2PPlacerSettings.clampOffset(offsetZ + delta);
            default -> throw new IllegalArgumentException("axis");
        }
        sendClientAction(switch (axis) {
            case 0 -> ADJUST_X;
            case 1 -> ADJUST_Y;
            default -> ADJUST_Z;
        }, delta);
    }

    public void resetOffsets() {
        offsetX = offsetY = offsetZ = 0;
        sendClientAction(RESET_OFFSETS);
    }

    public void clearSelection() {
        sendClientAction(CLEAR_SELECTION);
    }

    public void execute() {
        sendClientAction(EXECUTE);
    }

    public void loadFrequency(boolean createNew) {
        sendClientAction(LOAD_FREQUENCY, createNew);
    }

    public void resetFrequency() {
        frequency = 0;
        sendClientAction(RESET_FREQUENCY);
    }

    private void handleSetMode(P2PPlacerMode value) {
        if (isServerSide() && value != null) {
            updateSettings(host.getItemStack().getOrDefault(
                    ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT).withMode(value));
        }
    }

    private void handleSetDirection(Direction value) {
        if (isServerSide() && value != null) {
            updateSettings(host.getItemStack().getOrDefault(
                    ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT).withDirection(value));
        }
    }

    private void handleAdjustOffset(Integer value, int axis) {
        if (!isServerSide() || value == null) {
            return;
        }
        var settings = host.getItemStack().getOrDefault(ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT);
        int x = settings.offsetX();
        int y = settings.offsetY();
        int z = settings.offsetZ();
        switch (axis) {
            case 0 -> x = P2PPlacerSettings.clampOffset(x + value);
            case 1 -> y = P2PPlacerSettings.clampOffset(y + value);
            case 2 -> z = P2PPlacerSettings.clampOffset(z + value);
            default -> { return; }
        }
        updateSettings(settings.withOffsets(x, y, z));
    }

    private void handleResetOffsets() {
        if (isServerSide()) {
            updateSettings(host.getItemStack().getOrDefault(
                    ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT).resetOffsets());
        }
    }

    private void handleClearSelection() {
        if (isServerSide()) {
            host.getItemStack().remove(ModContent.PLACER_SELECTION.get());
        }
    }

    private void handleExecute() {
        if (isServerSide() && getPlayer() instanceof ServerPlayer serverPlayer) {
            var linkStatus = host.getLinkStatus();
            if (!linkStatus.connected() && linkStatus.statusDescription() != null) {
                serverPlayer.displayClientMessage(linkStatus.statusDescription(), false);
            }
            var result = P2PPlacementService.place(serverPlayer, host);
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.ae2_batchcraft.wp2pp_placer.result",
                    result.placed(), result.occupied(), result.materialFailed(), result.placementFailed()), true);
        }
    }

    private void handleLoadFrequency(Boolean createNew) {
        if (!isServerSide() || createNew == null) {
            return;
        }

        if (createNew) {
            var node = host.getActionableNode();
            var grid = node == null ? null : node.getGrid();
            if (grid == null || !host.getLinkStatus().connected()) {
                getPlayer().displayClientMessage(Component.translatable(
                        "message.ae2_batchcraft.wp2pp_placer.frequency.network_required"), true);
                return;
            }

            short newFrequency = P2PService.get(grid).newFrequency();
            setFrequency(newFrequency);
            ItemStack card = findMemoryCard();
            if (card.getItem() instanceof IMemoryCard) {
                writeFrequencyToCard(card, newFrequency);
            }
            getPlayer().displayClientMessage(Component.translatable(
                    "message.ae2_batchcraft.wp2pp_placer.frequency.generated",
                    Platform.p2p().toHexString(newFrequency)), true);
            return;
        }

        ItemStack card = findMemoryCard();
        if (!(card.getItem() instanceof IMemoryCard)) {
            getPlayer().displayClientMessage(Component.translatable(
                    "message.ae2_batchcraft.wp2pp_placer.frequency.no_card"), true);
            return;
        }

        var storedType = card.get(AEComponents.EXPORTED_P2P_TYPE);
        var storedFrequency = card.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (storedFrequency == null
                || (storedType != ModContent.PATTERN_P2P_TUNNEL_INPUT.get()
                    && storedType != ModContent.PATTERN_P2P_TUNNEL_OUTPUT.get())) {
            getPlayer().displayClientMessage(Component.translatable(
                    "message.ae2_batchcraft.wp2pp_placer.frequency.invalid_card"), true);
            return;
        }

        setFrequency(storedFrequency);
        getPlayer().displayClientMessage(Component.translatable(
                "message.ae2_batchcraft.wp2pp_placer.frequency.loaded",
                Platform.p2p().toHexString(storedFrequency)), true);
    }

    private void handleResetFrequency() {
        if (!isServerSide()) {
            return;
        }
        setFrequency((short) 0);
        getPlayer().displayClientMessage(Component.translatable(
                "message.ae2_batchcraft.wp2pp_placer.frequency.reset"), true);
    }

    private ItemStack findMemoryCard() {
        if (getCarried().getItem() instanceof IMemoryCard) {
            return getCarried();
        }
        if (getPlayer().getMainHandItem().getItem() instanceof IMemoryCard) {
            return getPlayer().getMainHandItem();
        }
        if (getPlayer().getOffhandItem().getItem() instanceof IMemoryCard) {
            return getPlayer().getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private void setFrequency(short value) {
        host.getItemStack().set(ModContent.PLACER_FREQUENCY.get(), value);
        frequency = Short.toUnsignedInt(value);
    }

    private static void writeFrequencyToCard(ItemStack card, short value) {
        MemoryCardItem.clearCard(card);
        card.set(AEComponents.EXPORTED_SETTINGS_SOURCE,
                ModContent.PATTERN_P2P_TUNNEL_INPUT.get().getDescription());
        card.set(AEComponents.EXPORTED_P2P_TYPE, ModContent.PATTERN_P2P_TUNNEL_INPUT.get());
        card.set(AEComponents.EXPORTED_P2P_FREQUENCY, value);
        var colors = Platform.p2p().toColors(value);
        card.set(AEComponents.MEMORY_CARD_COLORS, new MemoryCardColors(
                colors[0], colors[0], colors[1], colors[1],
                colors[2], colors[2], colors[3], colors[3]));
    }

    private void updateSettings(P2PPlacerSettings settings) {
        host.getItemStack().set(ModContent.PLACER_SETTINGS.get(), settings);
    }

    private static final class CableMarkerSlot extends FakeSlot {
        private CableMarkerSlot(InternalInventory inventory) {
            super(inventory, 0);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }

        @Override
        public void increase(ItemStack stack) {
            set(stack);
        }

        @Override
        public void decrease(ItemStack stack) {
            set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
    }
}
