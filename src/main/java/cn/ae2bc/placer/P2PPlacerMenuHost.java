package cn.ae2bc.placer;

import appeng.api.inventories.InternalInventory;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.contents.StackDependentSupplier;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.SupplierInternalInventory;
import cn.ae2bc.item.WirelessPatternP2PPlacerItem;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public final class P2PPlacerMenuHost
        extends WirelessTerminalMenuHost<WirelessPatternP2PPlacerItem> {
    private final SupplierInternalInventory<InternalInventory> materials;
    private final SupplierInternalInventory<InternalInventory> cableFilter;
    private final SupplierInternalInventory<InternalInventory> partFilter;

    public P2PPlacerMenuHost(WirelessPatternP2PPlacerItem item, Player player,
                             ItemMenuHostLocator locator,
                             BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        materials = new SupplierInternalInventory<>(new StackDependentSupplier<>(
                this::getItemStack, WirelessPatternP2PPlacerItem::getMaterialInventory));
        cableFilter = new SupplierInternalInventory<>(new StackDependentSupplier<>(
                this::getItemStack, WirelessPatternP2PPlacerItem::getCableFilterInventory));
        partFilter = new SupplierInternalInventory<>(new StackDependentSupplier<>(
                this::getItemStack, WirelessPatternP2PPlacerItem::getPartFilterInventory));
    }

    public InternalInventory getMaterials() {
        return materials;
    }

    public InternalInventory getCableFilter() {
        return cableFilter;
    }

    public InternalInventory getPartFilter() {
        return partFilter;
    }

}
