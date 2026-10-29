package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.OreDataAttachment;
import com.tom.createores.OreVeinGenerator;
import com.tom.createores.components.OreVeinAtlasDataComponent;
import com.tom.createores.item.OreVeinAtlasItem;
import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.DimChunkPos;
import com.tom.createores.util.RandomSpreadGenerator;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 黄金探矿杖：右键在聊天栏报出配置半径内所有矿脉的精确坐标，携带矿脉图鉴时同时记入图鉴。
 *
 * <p>矿脉位置由 Create Ore Excavation 的 {@link RandomSpreadGenerator} 算出：每条矿脉配方自带一个
 * {@link RandomSpreadStructurePlacement}，把世界切成一片片生成区域，每个区域最多生成一条该种矿脉。
 * 这里按区域网格逐格调用 {@code locate(..., 0)}（只检查传入坐标所在的那一个区域），
 * 因此能拿到半径内每一条矿脉的精确位置，而不是只有一个「最近的」。</p>
 */
public class GoldenVeinFinderItem extends Item {

    /** 使用冷却：20 秒。 */
    private static final int COOLDOWN_TICKS = 20 * 20;

    private static final String MESSAGE_KEY = "message.goldentweaks.golden_vein_finder";

    public GoldenVeinFinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            detect(level, player);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide) {
            detect(context.getLevel(), player);
        }
        return InteractionResult.SUCCESS;
    }

    private void detect(Level level, Player player) {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos origin = player.blockPosition();
        int radius = GoldenTweaksCommonConfig.getVeinFinderRadius();
        List<VeinHit> hits = findVeins(serverLevel, origin, radius);

        if (hits.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(MESSAGE_KEY + ".none", radius).withStyle(ChatFormatting.GRAY), false);
        } else {
            player.displayClientMessage(
                    Component.translatable(MESSAGE_KEY + ".header", hits.size(), radius)
                            .withStyle(ChatFormatting.GOLD), false);
            hits.forEach(hit -> player.displayClientMessage(describe(hit), false));
            recordToAtlas(serverLevel, player, hits);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    private static List<VeinHit> findVeins(ServerLevel level, BlockPos origin, int radius) {

        RecipeManager recipeManager = level.getRecipeManager();
        RandomSpreadGenerator picker = OreVeinGenerator.getPicker(level);
        ChunkPos originChunk = new ChunkPos(origin);
        List<VeinHit> hits = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (RecipeHolder<VeinRecipe> holder : recipeManager.getAllRecipesFor(
                CreateOreExcavation.VEIN_RECIPES.getRecipeType())) {

            RandomSpreadStructurePlacement placement = holder.value().getPlacement();
            if (placement == null) {
                continue;
            }
            int spacing = placement.spacing();
            if (spacing <= 0) {
                continue;
            }

            // 每个区域横跨 spacing 个区块；多算一圈保证边角上的矿脉不会被漏掉。
            int steps = (int) Math.ceil(radius / (double) (spacing * 16)) + 1;
            for (int dx = -steps; dx <= steps; dx++) {
                for (int dz = -steps; dz <= steps; dz++) {
                    BlockPos probe = new BlockPos(
                            (originChunk.x + spacing * dx) << 4, origin.getY(), (originChunk.z + spacing * dz) << 4);
                    BlockPos vein = picker.locate(holder.id(), probe, level, 0);
                    if (vein == null || !seen.add(new ChunkPos(vein).toLong())) {
                        continue;
                    }
                    int distance = (int) Math.floor(RandomSpreadGenerator.distance2d(vein, origin));
                    if (distance <= radius) {
                        hits.add(new VeinHit(holder, vein, distance));
                    }
                }
            }
        }

        hits.sort(Comparator.comparingInt(VeinHit::distance));
        return hits;
    }

    private static Component describe(VeinHit hit) {

        return Component.translatable(MESSAGE_KEY + ".line",
                hit.holder().value().getName().copy().withStyle(ChatFormatting.YELLOW),
                Component.literal(Integer.toString(hit.pos().getX())).withStyle(ChatFormatting.GREEN),
                Component.literal(Integer.toString(hit.pos().getZ())).withStyle(ChatFormatting.GREEN),
                Component.literal(Integer.toString(hit.distance())).withStyle(ChatFormatting.AQUA));
    }

    private static void recordToAtlas(ServerLevel level, Player player, List<VeinHit> hits) {

        ItemStack atlas = findAtlas(player);
        if (!(atlas.getItem() instanceof OreVeinAtlasItem atlasItem)) {
            return;
        }

        Set<DimChunkPos> known = new HashSet<>();
        OreVeinAtlasDataComponent data = atlas.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT);
        if (data != null) {
            data.veins().forEach(entry -> known.add(entry.getFirst()));
        }

        // addVein 每写入一条都会发一句提示，成批写入时按批静音（见 OreVeinAtlasItemSilentAddMixin）
        GTVeinAtlasMessages.beginBatch();
        try {
            for (VeinHit hit : hits) {
                DimChunkPos pos = new DimChunkPos(level, hit.pos());
                if (known.add(pos)) {
                    atlasItem.addVein(player, atlas, hit.holder(), pos, getRandomMul(level, hit.pos()));
                }
            }
        } finally {
            GTVeinAtlasMessages.endBatch();
        }
    }

    private static ItemStack findAtlas(Player player) {

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof OreVeinAtlasItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 矿脉的储量随机倍率。区块已加载时读它的实际数据（可能被 {@code /coe setvein} 改过），
     * 否则按 {@code OreData#populate} 的算法用世界种子与区块坐标复现。
     */
    private static float getRandomMul(ServerLevel level, BlockPos veinPos) {

        if (level.isLoaded(veinPos)) {
            return OreDataAttachment.getData(level.getChunkAt(veinPos)).getRandomMul();
        }
        return RandomSource.create(level.getSeed() ^ new ChunkPos(veinPos).toLong()).nextFloat();
    }

    private record VeinHit(RecipeHolder<VeinRecipe> holder, BlockPos pos, int distance) {
    }
}
