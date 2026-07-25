package cn.ae2bc.client;

import appeng.client.gui.style.StyleManager;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.menu.PatternP2PTunnelInputMenu;
import cn.ae2bc.menu.PatternP2PTunnelOutputMenu;
import cn.ae2bc.menu.PatternP2PTunnelEnergyMenu;
import cn.ae2bc.menu.P2PPlacerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Ae2bcMod.MOD_ID, dist = Dist.CLIENT)
public final class Ae2bcClient {
    private static final String INPUT_SCREEN_STYLE = "/screens/ae2_batchcraft/pattern_p2p_tunnel_input.json";
    private static final String OUTPUT_SCREEN_STYLE = "/screens/ae2_batchcraft/pattern_p2p_tunnel_output.json";
    private static final String PLACER_SCREEN_STYLE = "/screens/ae2_batchcraft/component_placer.json";
    private static final String ENERGY_SCREEN_STYLE = "/screens/ae2_batchcraft/pattern_p2p_tunnel_energy.json";

    public Ae2bcClient(IEventBus modBus) {
        modBus.addListener(Ae2bcClient::registerScreens);
        NeoForge.EVENT_BUS.addListener(P2PPlacerSelectionRenderer::render);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.<PatternP2PTunnelOutputMenu, PatternP2PTunnelOutputScreen>register(
                PatternP2PTunnelOutputMenu.TYPE, (menu, inventory, title) ->
                        new PatternP2PTunnelOutputScreen(menu, inventory, title,
                                StyleManager.loadStyleDoc(OUTPUT_SCREEN_STYLE)));
        event.<PatternP2PTunnelInputMenu, PatternP2PTunnelInputScreen>register(
                PatternP2PTunnelInputMenu.TYPE, (menu, inventory, title) ->
                        new PatternP2PTunnelInputScreen(menu, inventory, title,
                                StyleManager.loadStyleDoc(INPUT_SCREEN_STYLE)));
        event.<PatternP2PTunnelEnergyMenu, PatternP2PTunnelEnergyScreen>register(
                PatternP2PTunnelEnergyMenu.TYPE, (menu, inventory, title) ->
                        new PatternP2PTunnelEnergyScreen(menu, inventory, title,
                                StyleManager.loadStyleDoc(ENERGY_SCREEN_STYLE)));
        event.<P2PPlacerMenu, P2PPlacerScreen>register(
                P2PPlacerMenu.TYPE, (menu, inventory, title) ->
                        new P2PPlacerScreen(menu, inventory, title,
                                StyleManager.loadStyleDoc(PLACER_SCREEN_STYLE)));
    }
}
