package cn.ae2bc.registry;

import appeng.core.AEConfig;
import appeng.api.config.Actionable;
import appeng.items.parts.PartItem;
import cn.ae2bc.item.WirelessPatternP2PPlacerItem;
import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.part.PatternP2PTunnelPart;
import cn.ae2bc.part.PatternP2PTunnelEnergyPart;
import cn.ae2bc.pattern.InputDirectionData;
import cn.ae2bc.placer.P2PPlacerSelection;
import cn.ae2bc.placer.P2PPlacerSettings;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModContent {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Ae2bcMod.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ae2bcMod.MOD_ID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Ae2bcMod.MOD_ID);

    public static final Supplier<DataComponentType<InputDirectionData>> INPUT_DIRECTIONS =
            COMPONENTS.register("input_directions", () -> DataComponentType.<InputDirectionData>builder()
                    .persistent(InputDirectionData.CODEC)
                    .networkSynchronized(InputDirectionData.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<P2PPlacerSettings>> PLACER_SETTINGS =
            COMPONENTS.register("wp2pp_placer_settings", () -> DataComponentType.<P2PPlacerSettings>builder()
                    .persistent(P2PPlacerSettings.CODEC)
                    .networkSynchronized(P2PPlacerSettings.STREAM_CODEC)
                    .build());
    public static final Supplier<DataComponentType<P2PPlacerSelection>> PLACER_SELECTION =
            COMPONENTS.register("wp2pp_placer_selection", () -> DataComponentType.<P2PPlacerSelection>builder()
                    .persistent(P2PPlacerSelection.CODEC)
                    .networkSynchronized(P2PPlacerSelection.STREAM_CODEC)
                    .build());
    public static final Supplier<DataComponentType<ItemContainerContents>> PLACER_CABLE =
            COMPONENTS.register("wp2pp_placer_cable", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());
    public static final Supplier<DataComponentType<Short>> PLACER_FREQUENCY =
            COMPONENTS.register("wp2pp_placer_frequency", () -> DataComponentType.<Short>builder()
                    .persistent(com.mojang.serialization.Codec.SHORT)
                    .networkSynchronized(ByteBufCodecs.SHORT)
                    .build());

    public static final DeferredHolder<Item, PartItem<PatternP2PTunnelPart>> PATTERN_P2P_TUNNEL_INPUT =
            ITEMS.register("pattern_p2p_tunnel_input", () -> new PartItem<>(new Item.Properties(),
                    PatternP2PTunnelPart.class, item -> new PatternP2PTunnelPart(item, false)));
    public static final DeferredHolder<Item, PartItem<PatternP2PTunnelPart>> PATTERN_P2P_TUNNEL_OUTPUT =
            ITEMS.register("pattern_p2p_tunnel_output", () -> new PartItem<>(new Item.Properties(),
                    PatternP2PTunnelPart.class, item -> new PatternP2PTunnelPart(item, true)));
    public static final DeferredHolder<Item, PartItem<PatternP2PTunnelEnergyPart>> PATTERN_P2P_TUNNEL_ENERGY =
            ITEMS.register("pattern_p2p_tunnel_energy", () -> new PartItem<>(new Item.Properties(),
                    PatternP2PTunnelEnergyPart.class, PatternP2PTunnelEnergyPart::new));
    public static final DeferredHolder<Item, WirelessPatternP2PPlacerItem> WIRELESS_PATTERN_P2P_PLACER =
            ITEMS.register("wp2pp_placer", () -> new WirelessPatternP2PPlacerItem(
                    AEConfig.instance().getWirelessTerminalBattery(), new Item.Properties().stacksTo(1)));

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ae2_batchcraft"))
            .icon(() -> PATTERN_P2P_TUNNEL_INPUT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(PATTERN_P2P_TUNNEL_INPUT.get());
                output.accept(PATTERN_P2P_TUNNEL_OUTPUT.get());
                output.accept(PATTERN_P2P_TUNNEL_ENERGY.get());
                var placer = WIRELESS_PATTERN_P2P_PLACER.get();
                ItemStack chargedPlacer = placer.getDefaultInstance();
                placer.injectAEPower(chargedPlacer, placer.getAEMaxPower(chargedPlacer), Actionable.MODULATE);
                output.accept(chargedPlacer);
            })
            .build());

    private ModContent() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
        COMPONENTS.register(bus);
    }
}
