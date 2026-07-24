package cn.ae2bc.item;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import cn.ae2bc.menu.P2PPlacerMenu;
import cn.ae2bc.placer.P2PPlacerMenuHost;
import cn.ae2bc.placer.P2PPlacerSelection;
import cn.ae2bc.placer.P2PPlacerSettings;
import cn.ae2bc.registry.ModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.DoubleSupplier;

public final class WirelessPatternP2PPlacerItem extends WirelessTerminalItem {
    public static final int MATERIAL_SLOT_COUNT = 6;

    public WirelessPatternP2PPlacerItem(DoubleSupplier powerCapacity, Properties properties) {
        super(powerCapacity, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack placer = context.getItemInHand();
        var dimension = context.getLevel().dimension().location();
        if (context.isSecondaryUseActive()) {
            if (!context.getLevel().isClientSide()) {
                placer.set(ModContent.PLACER_SELECTION.get(),
                        P2PPlacerSelection.start(dimension, context.getClickedPos()));
                P2PPlacerSettings settings = placer.getOrDefault(
                        ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT);
                placer.set(ModContent.PLACER_SETTINGS.get(), settings.resetOffsetsForNewSelection());
                player.displayClientMessage(Component.translatable(
                        "message.ae2_batchcraft.wp2pp_placer.first_set",
                        context.getClickedPos().toShortString()), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        P2PPlacerSelection selection = placer.get(ModContent.PLACER_SELECTION.get());
        if (selection == null || selection.second() != null || !selection.dimension().equals(dimension)) {
            return InteractionResult.PASS;
        }

        P2PPlacerSelection completed = selection.complete(context.getClickedPos());
        if (!context.getLevel().isClientSide()) {
            P2PPlacerSelection.Validation validation = completed.validate();
            if (validation == P2PPlacerSelection.Validation.VALID) {
                placer.set(ModContent.PLACER_SELECTION.get(), completed);
                player.displayClientMessage(Component.translatable(
                        "message.ae2_batchcraft.wp2pp_placer.selection_set",
                        completed.sizeX(), completed.sizeY(), completed.sizeZ()), true);
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.ae2_batchcraft.wp2pp_placer.selection_"
                                + validation.name().toLowerCase(Locale.ROOT)), true);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public MenuType<?> getMenuType() {
        return P2PPlacerMenu.TYPE;
    }

    @Override
    public @NotNull P2PPlacerMenuHost getMenuHost(Player player, ItemMenuHostLocator locator,
                                                  @Nullable BlockHitResult hitResult) {
        return new P2PPlacerMenuHost(this, player, locator,
                (p, subMenu) -> openFromInventory(p, locator, true));
    }

    public static AppEngInternalInventory getMaterialInventory(ItemStack placer) {
        var inventory = new AppEngInternalInventory(new InternalInventoryHost() {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inventory) {
                placer.set(DataComponents.CONTAINER, inventory.toItemContainerContents());
            }

            @Override
            public boolean isClientSide() {
                return false;
            }
        }, MATERIAL_SLOT_COUNT);
        inventory.setEnableClientEvents(true);
        inventory.setFilter(new MaterialFilter());
        inventory.fromItemContainerContents(placer.getOrDefault(
                DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        return inventory;
    }

    public static AppEngInternalInventory getCableFilterInventory(ItemStack placer) {
        var inventory = new AppEngInternalInventory(new InternalInventoryHost() {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inventory) {
                ItemStack cable = inventory.getStackInSlot(0);
                if (cable.isEmpty()) {
                    placer.remove(ModContent.PLACER_CABLE.get());
                } else {
                    placer.set(ModContent.PLACER_CABLE.get(), ItemContainerContents.fromItems(
                            java.util.List.of(cable.copyWithCount(1))));
                }
            }

            @Override
            public boolean isClientSide() {
                return false;
            }
        }, 1, 1);
        inventory.setEnableClientEvents(true);
        inventory.setFilter(new CableFilter());
        ItemStack cable = getMarkedCable(placer);
        if (!cable.isEmpty()) {
            inventory.fromItemContainerContents(ItemContainerContents.fromItems(java.util.List.of(cable)));
        }
        return inventory;
    }

    public static ItemStack getMarkedCable(ItemStack placer) {
        ItemStack cable = placer.getOrDefault(ModContent.PLACER_CABLE.get(), ItemContainerContents.EMPTY).copyOne();
        return cable.isEmpty() ? cable : cable.copyWithCount(1);
    }

    public static boolean isUsableCable(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IPartItem<?> partItem)) {
            return false;
        }
        var part = partItem.createPart();
        return part instanceof ICablePart cable && cable.supportsBuses() == BusSupport.CABLE;
    }

    public static boolean isAllowedMaterial(ItemStack stack) {
        return stack.is(ModContent.PATTERN_P2P_TUNNEL_INPUT.get())
                || stack.is(ModContent.PATTERN_P2P_TUNNEL_OUTPUT.get())
                || isUsableCable(stack);
    }

    private static final class MaterialFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(appeng.api.inventories.InternalInventory inventory, int slot, ItemStack stack) {
            return isAllowedMaterial(stack);
        }
    }

    private static final class CableFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(appeng.api.inventories.InternalInventory inventory, int slot, ItemStack stack) {
            return stack.isEmpty() || isUsableCable(stack);
        }
    }
}
