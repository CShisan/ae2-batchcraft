package cn.ae2bc.placer;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.PlayerSource;
import appeng.me.service.P2PService;
import cn.ae2bc.item.WirelessPatternP2PPlacerItem;
import cn.ae2bc.part.PatternP2PTunnelPart;
import cn.ae2bc.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

/** Server-side implementation of the placer. One target is an all-or-nothing operation. */
public final class P2PPlacementService {
    private P2PPlacementService() {
    }

    public static Result place(ServerPlayer player, P2PPlacerMenuHost menuHost) {
        if (!(player.level() instanceof ServerLevel level)) {
            return Result.empty();
        }

        ItemStack placer = menuHost.getItemStack();
        P2PPlacerSelection selection = placer.get(ModContent.PLACER_SELECTION.get());
        P2PPlacerSettings settings = placer.getOrDefault(
                ModContent.PLACER_SETTINGS.get(), P2PPlacerSettings.DEFAULT);
        ItemStack cable = WirelessPatternP2PPlacerItem.getMarkedCable(placer);
        short frequency = placer.getOrDefault(ModContent.PLACER_FREQUENCY.get(), (short) 0);

        if (selection == null || !selection.dimension().equals(level.dimension().location())
                || !WirelessPatternP2PPlacerItem.isUsableCable(cable)) {
            return Result.empty();
        }
        var positions = selection.positions(settings);
        if (positions.isEmpty()) {
            return Result.empty();
        }

        IPartItem<?> cableItem = (IPartItem<?>) cable.getItem();
        IPartItem<?> endpointItem = settings.mode() == P2PPlacerMode.INPUT
                ? ModContent.PATTERN_P2P_TUNNEL_INPUT.get()
                : ModContent.PATTERN_P2P_TUNNEL_OUTPUT.get();
        ItemStack endpointStack = endpointItem.asItem().getDefaultInstance();
        PlayerSource actionSource = new PlayerSource(player);

        int placed = 0;
        int occupied = 0;
        int materialFailed = 0;
        int placementFailed = 0;
        for (BlockPos pos : positions) {
            if (!level.isLoaded(pos) || !level.mayInteract(player, pos)) {
                placementFailed++;
                continue;
            }
            if (!level.isEmptyBlock(pos)) {
                occupied++;
                continue;
            }

            Reservation cableReservation = extract(menuHost, actionSource, cable);
            if (cableReservation == null) {
                materialFailed++;
                continue;
            }

            Reservation endpointReservation = extract(menuHost, actionSource, endpointStack);
            if (endpointReservation == null) {
                refund(menuHost, player, actionSource, cableReservation);
                materialFailed++;
                continue;
            }

            if (placeAt(level, player, pos, cableItem, endpointItem, settings.direction(), frequency)) {
                placed++;
            } else {
                refund(menuHost, player, actionSource, endpointReservation);
                refund(menuHost, player, actionSource, cableReservation);
                placementFailed++;
            }
        }

        return new Result(placed, occupied, materialFailed, placementFailed);
    }

    private static boolean placeAt(ServerLevel level, ServerPlayer player, BlockPos pos,
                                   IPartItem<?> cableItem, IPartItem<?> endpointItem, Direction direction,
                                   short frequency) {
        IPartHost host = PartHelper.getOrPlacePartHost(level, pos, false, player);
        if (host == null) {
            return false;
        }

        IPart cablePart = addPart(host, cableItem, null, player);
        if (cablePart == null || !isUnobstructed(level, pos, host)) {
            removePart(host, cablePart);
            return false;
        }

        IPart endpointPart = addPart(host, endpointItem, direction, player);
        if (endpointPart == null || !isUnobstructed(level, pos, host)) {
            removePart(host, endpointPart);
            removePart(host, cablePart);
            return false;
        }
        if (!(endpointPart instanceof PatternP2PTunnelPart endpoint)) {
            removePart(host, endpointPart);
            removePart(host, cablePart);
            return false;
        }
        if (frequency != 0) {
            var grid = endpoint.getMainNode().getGrid();
            if (grid == null) {
                endpoint.setFrequency(frequency);
                endpoint.onTunnelNetworkChange();
            } else {
                P2PService.get(grid).updateFreq(endpoint, frequency);
            }
            endpoint.onTunnelConfigChange();
        }
        return true;
    }

    private static IPart addPart(IPartHost host, IPartItem<?> partItem, Direction side, ServerPlayer player) {
        return host.addPart(partItem, side, player);
    }

    private static boolean isUnobstructed(ServerLevel level, BlockPos pos, IPartHost host) {
        VoxelShape shape = host.getCollisionShape(null);
        return shape.isEmpty() || level.isUnobstructed(null, shape.move(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static void removePart(IPartHost host, IPart part) {
        if (part != null) {
            host.removePart(part);
        }
        if (host.isEmpty()) {
            host.cleanup();
        }
    }

    private static Reservation extract(P2PPlacerMenuHost host, PlayerSource actionSource, ItemStack requested) {
        if (requested.isEmpty()) {
            return null;
        }

        if (host.getLinkStatus().connected()) {
            AEItemKey key = AEItemKey.of(requested);
            if (key != null && StorageHelper.poweredExtraction(
                    host, host.getInventory(), key, 1, actionSource, Actionable.MODULATE) == 1) {
                return new Reservation(key.toStack(), Source.AE);
            }
        }

        ItemStack local = host.getMaterials().removeItems(1, requested, null);
        if (!local.isEmpty()) {
            return new Reservation(local.copyWithCount(1), Source.LOCAL);
        }
        return null;
    }

    private static void refund(P2PPlacerMenuHost host, ServerPlayer player, PlayerSource actionSource,
                               Reservation reservation) {
        ItemStack refund = reservation.stack().copyWithCount(1);
        if (reservation.source() == Source.AE) {
            AEItemKey key = AEItemKey.of(refund);
            if (key != null && StorageHelper.poweredInsert(
                    host, host.getInventory(), key, 1, actionSource, Actionable.MODULATE) == 1) {
                return;
            }
        } else {
            ItemStack remainder = host.getMaterials().addItems(refund);
            if (remainder.isEmpty()) {
                return;
            }
            refund = remainder;
        }

        // A full local inventory or a network that went offline must never destroy a reservation.
        player.getInventory().placeItemBackInInventory(refund);
    }

    private enum Source {
        AE,
        LOCAL
    }

    private record Reservation(ItemStack stack, Source source) {
        private Reservation {
            Objects.requireNonNull(stack);
            Objects.requireNonNull(source);
        }
    }

    public record Result(int placed, int occupied, int materialFailed, int placementFailed) {
        public static Result empty() {
            return new Result(0, 0, 0, 0);
        }
    }
}
