package cn.ae2bc.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.part.PatternP2PTunnelEnergyPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class PatternP2PTunnelEnergyMenu extends AEBaseMenu {
    private static final String SET_PULL_ENABLED = "setPullEnabled";

    public static final MenuType<PatternP2PTunnelEnergyMenu> TYPE = MenuTypeBuilder
            .create(PatternP2PTunnelEnergyMenu::new, PatternP2PTunnelEnergyPart.class)
            .withMenuTitle(part -> part.getPartItem().asItem().getDescription())
            .buildUnregistered(ResourceLocation.fromNamespaceAndPath(
                    Ae2bcMod.MOD_ID, "pattern_p2p_tunnel_energy"));

    private final PatternP2PTunnelEnergyPart host;

    @GuiSync(0)
    public boolean pullEnabled;
    @GuiSync(1)
    public int pullInterval = PatternP2PTunnelEnergyPart.PULL_INTERVAL;

    public PatternP2PTunnelEnergyMenu(int id, Inventory playerInventory, PatternP2PTunnelEnergyPart host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        registerClientAction(SET_PULL_ENABLED, Boolean.class, this::handleSetPullEnabled);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            pullEnabled = host.isPullEnabled();
            pullInterval = host.getPullInterval();
        }
        super.broadcastChanges();
    }

    public void setPullEnabled(boolean enabled) {
        pullEnabled = enabled;
        sendClientAction(SET_PULL_ENABLED, enabled);
    }

    private void handleSetPullEnabled(Boolean enabled) {
        if (isServerSide() && enabled != null) {
            host.setPullEnabled(enabled);
        }
    }
}
