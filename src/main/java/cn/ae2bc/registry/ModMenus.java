package cn.ae2bc.registry;

import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.menu.P2PPlacerMenu;
import cn.ae2bc.menu.PatternP2PTunnelEnergyMenu;
import cn.ae2bc.menu.PatternP2PTunnelInputMenu;
import cn.ae2bc.menu.PatternP2PTunnelOutputMenu;
import cn.ae2bc.menu.ProductExtractionMenu;
import cn.ae2bc.menu.PatternP2PUnitManagerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Ae2bcMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PatternP2PTunnelInputMenu>>
            PATTERN_P2P_TUNNEL_INPUT = MENUS.register(
                    "pattern_p2p_tunnel_input", () -> PatternP2PTunnelInputMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<PatternP2PTunnelOutputMenu>>
            PATTERN_P2P_TUNNEL_OUTPUT = MENUS.register(
                    "pattern_p2p_tunnel_output", () -> PatternP2PTunnelOutputMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<PatternP2PTunnelEnergyMenu>>
            PATTERN_P2P_TUNNEL_ENERGY = MENUS.register(
                    "pattern_p2p_tunnel_energy", () -> PatternP2PTunnelEnergyMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<P2PPlacerMenu>>
            COMPONENT_PLACER = MENUS.register(
                    ModContent.COMPONENT_PLACER_ID, () -> P2PPlacerMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<ProductExtractionMenu>>
            PRODUCT_EXTRACTION = MENUS.register("product_extraction", () -> ProductExtractionMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<PatternP2PUnitManagerMenu>>
            PATTERN_P2P_UNIT_MANAGER = MENUS.register("pp2p_unit_manager", () -> PatternP2PUnitManagerMenu.TYPE);

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    public static void verifyRegistrations() {
        verifyRegistration(PATTERN_P2P_TUNNEL_INPUT);
        verifyRegistration(PATTERN_P2P_TUNNEL_OUTPUT);
        verifyRegistration(PATTERN_P2P_TUNNEL_ENERGY);
        verifyRegistration(COMPONENT_PLACER);
        verifyRegistration(PRODUCT_EXTRACTION);
        verifyRegistration(PATTERN_P2P_UNIT_MANAGER);
        Ae2bcMod.LOGGER.info("Verified AE2 BatchCraft menu registrations");
    }

    private static void verifyRegistration(
            DeferredHolder<MenuType<?>, ? extends MenuType<?>> holder) {
        ResourceLocation actualId = BuiltInRegistries.MENU.getKey(holder.get());
        if (!holder.getId().equals(actualId)) {
            throw new IllegalStateException("Menu type " + holder.getId()
                    + " was registered as " + actualId);
        }
    }
}
