package cn.blockforge.generated.generatedmod.network.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanSubmitPacket;
import cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanRubPacket;
import cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanSelectPacket;


public final class TalismanService {
        private static boolean menu(ServerPlayer player,net.minecraft.core.BlockPos pos){
            return player.isAlive()&&!player.isSpectator()&&player.containerMenu instanceof TalismanPlatformMenu menu
                    &&menu.blockPos().equals(pos)&&menu.stillValid(player);
        }

        public static void handleTalismanSubmitPacket(TalismanSubmitPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !TalismanCatalog.has(packet.talismanId())||!menu(player,packet.pos())) return;
                if (!player.blockPosition().closerThan(packet.pos(), 8.0D)) return;
                if (!(player.level() instanceof ServerLevel level)
                        || !(level.getBlockEntity(packet.pos()) instanceof TalismanPlatformBlockEntity be)) return;
                if (!TalismanKnowledge.has(player)) {
                    player.displayClientMessage(Component.translatable(
                            "message.wuxingxiantu.need_wanfa"), true);
                    return;
                }
                boolean[][] drawn = TalismanCatalog.decode(packet.bits());
                float score = TalismanCatalog.score(packet.talismanId(), drawn);
                score=cn.blockforge.generated.generatedmod.progression.PlayerCrafts.talismanScore(score,cn.blockforge.generated.generatedmod.progression.PlayerCrafts.bonus(player,"talisman"));
                TalismanCatalog.Quality quality = TalismanCatalog.qualityOf(score);
                boolean ok = quality != TalismanCatalog.Quality.WASTE;
                handlePlatformSubmit(level, be, packet.talismanId(), quality, ok, player);
            });
            context.setPacketHandled(true);
        }

        public static void handleTalismanRubPacket(TalismanRubPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || packet.copies() < 1 || packet.copies() > 64||!menu(player,packet.pos())) return;
                if (!player.blockPosition().closerThan(packet.pos(), 8.0D)) return;
                if (!(player.level() instanceof ServerLevel level)
                        || !(level.getBlockEntity(packet.pos()) instanceof TalismanPlatformBlockEntity be)
                        || be.kind() != TalismanPlatformBlockEntity.Kind.RUB) return;
                handleRub(level, be, packet.copies(), player);
            });
            context.setPacketHandled(true);
        }

        public static void handleTalismanSelectPacket(TalismanSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || packet.index() < 0||packet.index()>=TalismanCatalog.ids().size()||!menu(player,packet.pos())) return;
                if (!player.blockPosition().closerThan(packet.pos(), 8.0D)) return;
                if (player.level().getBlockEntity(packet.pos()) instanceof TalismanPlatformBlockEntity be
                        &&be.kind()!=TalismanPlatformBlockEntity.Kind.RUB) {
                    be.setSelectedIndex(packet.index());
                }
            });
            context.setPacketHandled(true);
        }

    private static void handlePlatformSubmit(ServerLevel level, TalismanPlatformBlockEntity be,
                                             String talismanId, TalismanCatalog.Quality quality,
                                             boolean success, ServerPlayer player) {
        if(be.kind()==TalismanPlatformBlockEntity.Kind.RUB)return;
        net.minecraft.world.Container c = be;
        if (be.kind() == TalismanPlatformBlockEntity.Kind.ENGRAVE) {
            ItemStack knife = c.getItem(TalismanPlatformBlockEntity.ENGRAVE_KNIFE);
            ItemStack gold = c.getItem(TalismanPlatformBlockEntity.ENGRAVE_GOLD);
            if (!(knife.getItem() instanceof cn.blockforge.generated.generatedmod.items.CarvingKnifeItem)
                    || !gold.is(net.minecraft.world.item.Items.GOLD_INGOT)||gold.getCount()<TalismanCatalog.drawCost(talismanId)) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.platform_missing_materials"), true);
                return;
            }
            if (success) {
                if (!placeOutput(c, sealWith(talismanId, quality))) return;
                gold.shrink(TalismanCatalog.drawCost(talismanId));
                damageKnife(knife, player);
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.seal_engraved", TalismanCatalog.title(talismanId),
                        quality.title()), true);
            } else {
                // 废符：金锭损毁、刻刀耐久下降。
                gold.shrink(TalismanCatalog.drawCost(talismanId));
                damageKnife(knife, player);
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.glyph_failed"), true);
            }
        } else if (be.kind() == TalismanPlatformBlockEntity.Kind.DRAW) {
            ItemStack paper = c.getItem(TalismanPlatformBlockEntity.DRAW_PAPER);
            int cost = TalismanCatalog.drawCost(talismanId);
            if (!paper.is(WuXingItems.TALISMAN_PAPER.get())
                    || !hasSolidInk(c, TalismanPlatformBlockEntity.Kind.DRAW)
                    || inkTotal(c, TalismanPlatformBlockEntity.Kind.DRAW) < cost) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.platform_missing_materials"), true);
                return;
            }
            if (success) {
                if (!placeOutput(c, talismanWith(talismanId, quality))) return;
                paper.shrink(1);
                consumeInk(c, TalismanPlatformBlockEntity.Kind.DRAW, cost);
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.talisman_drawn", quality.title()), true);
            } else {
                paper.shrink(1);
                consumeInk(c, TalismanPlatformBlockEntity.Kind.DRAW, cost);
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.glyph_failed"), true);
            }
        }
        if(success)cn.blockforge.generated.generatedmod.progression.PlayerCrafts.get(player.server).receipt(player.getUUID(),"talisman",talismanWith(talismanId,quality),quality.ordinal());
        be.setChanged();
    }

    private static void handleRub(ServerLevel level, TalismanPlatformBlockEntity be, int copies, ServerPlayer player) {
        net.minecraft.world.Container c = be;
        ItemStack seal = c.getItem(TalismanPlatformBlockEntity.RUB_SEAL);
        ItemStack paper = c.getItem(TalismanPlatformBlockEntity.RUB_PAPER);
        String id = TalismanData.getTalisman(seal);
        if (!TalismanCatalog.has(id)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.seal_empty"), true);
            return;
        }
        if (!paper.is(WuXingItems.TALISMAN_PAPER.get())
                || !hasSolidInk(c, TalismanPlatformBlockEntity.Kind.RUB)) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.platform_missing_materials"), true);
            return;
        }
        int cost = TalismanCatalog.drawCost(id);
        int maxByPaper = paper.getCount();
        int maxByInk = inkTotal(c, TalismanPlatformBlockEntity.Kind.RUB) / cost;
        int max = Math.min(copies, Math.min(maxByPaper, maxByInk));
        if (max <= 0) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.platform_missing_materials"), true);
            return;
        }
        max = Math.min(max, 64);
        TalismanCatalog.Quality q = sealQuality(seal);
        if (!placeOutput(c, talismanStack(id, max, q))) return;
        paper.shrink(max);
        consumeInk(c, TalismanPlatformBlockEntity.Kind.RUB, max * cost);
        be.setChanged();
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.rubbed", max), true);
    }

    private static int selectedInk(net.minecraft.world.Container c){return c instanceof TalismanPlatformBlockEntity be?be.selectedInk():0;}

    private static boolean isInk(ItemStack stack) {
        return stack.is(WuXingItems.SPIRIT_INK.get()) || stack.is(WuXingItems.CINNABAR_INK.get());
    }

    private static int inkRemaining(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
    }

    private static int inkTotal(net.minecraft.world.Container c, TalismanPlatformBlockEntity.Kind kind) {
        int start = TalismanPlatformBlockEntity.inkStart(kind);
        if (start < 0) return 0;
        int total = 0;
        for (int i = selectedInk(c); i <= selectedInk(c); i++) {
            ItemStack ink = c.getItem(start + i);
            if (isInk(ink)) total += inkRemaining(ink);
        }
        return total;
    }

    private static boolean hasSolidInk(net.minecraft.world.Container c, TalismanPlatformBlockEntity.Kind kind) {
        int start = TalismanPlatformBlockEntity.inkStart(kind);
        if (start < 0) return false;
        for (int i = selectedInk(c); i <= selectedInk(c); i++) {
            ItemStack ink = c.getItem(start + i);
            if (isInk(ink) && inkRemaining(ink) > 0) return true;
        }
        return false;
    }

    private static void consumeInk(net.minecraft.world.Container c, TalismanPlatformBlockEntity.Kind kind, int amount) {
        int start = TalismanPlatformBlockEntity.inkStart(kind);
        if (start < 0 || amount <= 0) return;
        for (int i = selectedInk(c); i <= selectedInk(c) && amount > 0; i++) {
            ItemStack ink = c.getItem(start + i);
            if (!isInk(ink)) continue;
            int remaining = inkRemaining(ink);
            if (remaining <= 0) {
                c.setItem(start + i, ItemStack.EMPTY);
                continue;
            }
            int take = Math.min(amount, remaining);
            amount -= take;
            int damage = ink.getDamageValue() + take;
            if (damage >= ink.getMaxDamage()) {
                c.setItem(start + i, ItemStack.EMPTY);
            } else {
                ink.setDamageValue(damage);
                c.setChanged();
            }
        }
    }

    private static boolean placeOutput(net.minecraft.world.Container c, ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack existing = c.getItem(TalismanPlatformBlockEntity.OUTPUT);
        if (existing.isEmpty()) {
            c.setItem(TalismanPlatformBlockEntity.OUTPUT, stack);
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, stack)
                && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
            existing.grow(stack.getCount());
            return true;
        }
        return false;
    }

    private static ItemStack sealWith(String talismanId, TalismanCatalog.Quality quality) {
        ItemStack stack = new ItemStack(WuXingItems.SEAL.get());
        TalismanData.setTalisman(stack, talismanId);
        TalismanData.setQuality(stack, quality == null ? TalismanCatalog.Quality.PERFECT : quality);
        return stack;
    }

    private static ItemStack talismanWith(String talismanId, TalismanCatalog.Quality quality) {
        return talismanStack(talismanId, 1, quality);
    }

    private static ItemStack talismanStack(String talismanId, int count, TalismanCatalog.Quality quality) {
        return TalismanData.create(talismanId,count,quality);
    }

    private static TalismanCatalog.Quality sealQuality(ItemStack seal) {
        int ordinal = TalismanData.getQuality(seal);
        if (ordinal < 0 && TalismanData.hasTalisman(seal)) return TalismanCatalog.Quality.PERFECT;
        TalismanCatalog.Quality[] values = TalismanCatalog.Quality.values();
        int clamped = Math.max(0, Math.min(values.length - 1, ordinal));
        TalismanCatalog.Quality q = values[clamped];
        return q == TalismanCatalog.Quality.WASTE ? TalismanCatalog.Quality.LOW : q;
    }

    private static void damageKnife(ItemStack knife, ServerPlayer player) {
        if (knife.hurt(1, player.level().random, null)) {
            knife.shrink(1);
        }
    }
    private TalismanService() {}
}
