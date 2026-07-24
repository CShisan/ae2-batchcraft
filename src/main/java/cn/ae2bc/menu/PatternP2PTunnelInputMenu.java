package cn.ae2bc.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.logic.ReturnMode;
import cn.ae2bc.part.PatternP2PTunnelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class PatternP2PTunnelInputMenu extends AEBaseMenu {
    private static final String SET_RETURN_MODE = "setReturnMode";

    public static final MenuType<PatternP2PTunnelInputMenu> TYPE = MenuTypeBuilder
            .create(PatternP2PTunnelInputMenu::new, PatternP2PTunnelPart.class)
            .withMenuTitle(part -> part.getPartItem().asItem().getDescription())
            .buildUnregistered(ResourceLocation.fromNamespaceAndPath(
                    Ae2bcMod.MOD_ID, "pattern_p2p_tunnel_input"));

    private final PatternP2PTunnelPart host;

    @GuiSync(0)
    public ReturnMode returnMode = ReturnMode.UNBLOCKED;

    public PatternP2PTunnelInputMenu(int id, Inventory playerInventory, PatternP2PTunnelPart host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        registerClientAction(SET_RETURN_MODE, ReturnMode.class, this::handleSetReturnMode);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide() && !host.isOutput()) {
            var logic = host.getInputLogic();
            returnMode = logic.getReturnMode();
        }
        super.broadcastChanges();
    }

    public void setReturnMode(ReturnMode mode) {
        returnMode = mode;
        sendClientAction(SET_RETURN_MODE, mode);
    }

    private void handleSetReturnMode(ReturnMode mode) {
        if (isServerSide() && !host.isOutput() && mode != null) {
            host.getInputLogic().setReturnMode(mode);
        }
    }

}
