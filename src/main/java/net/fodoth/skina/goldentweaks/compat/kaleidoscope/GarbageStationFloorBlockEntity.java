package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.misc.TrashCanBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.EnumMap;
import java.util.Map;

public class GarbageStationFloorBlockEntity extends BlockEntity {

    private static final Map<VillageType, ResourceKey<LootTable>> LOOT_KEYS =
            new EnumMap<>(VillageType.class);

    private long lastCheckedDay = -1;

    public GarbageStationFloorBlockEntity(BlockPos pos, BlockState state) {
        super(KaleidoAdditionalBlockEntities.GARBAGE_STATION_FLOOR.get(), pos, state);
    }

    /* ------------------------------------------------------ */
    /* Tick                                                   */
    /* ------------------------------------------------------ */

    public static void tick(Level level, BlockPos pos, BlockState state, GarbageStationFloorBlockEntity be) {

        if (!(level instanceof ServerLevel serverLevel)) return;

        long gameTime = serverLevel.getGameTime();
        long currentDay = gameTime / 24000L;
        long timeOfDay = serverLevel.getDayTime() % 24000L;

        if (timeOfDay > 1) return;

        if (be.lastCheckedDay == currentDay) return;

        be.lastCheckedDay = currentDay;
        be.setChanged();

        be.refreshState();
        be.trySpawnGarbage(serverLevel, pos.above());
    }

    /* ------------------------------------------------------ */
    /* Loot Table Key                                         */
    /* ------------------------------------------------------ */

    private ResourceKey<LootTable> getLootTableKey(VillageType type) {
        return LOOT_KEYS.computeIfAbsent(type, t ->
                ResourceKey.create(
                        Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath(
                                GoldenTweaks.MODID,
                                "gameplay/garbage/" + t.getSerializedName()
                        )
                )
        );
    }

    private LootTable getLootTable(ServerLevel level, VillageType type) {
        return level.getServer()
                .reloadableRegistries()
                .getLootTable(getLootTableKey(type));
    }

    /* ------------------------------------------------------ */
    /* Spawn Logic                                            */
    /* ------------------------------------------------------ */

    private void trySpawnGarbage(ServerLevel level, BlockPos abovePos) {

        BlockEntity target = level.getBlockEntity(abovePos);
        if (!(target instanceof TrashCanBlockEntity trashCan)) return;

        VillageType type = getVillageType(level, worldPosition);

        LootTable lootTable = getLootTable(level, type);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(abovePos))
                .create(LootContextParamSets.EMPTY);

        ObjectArrayList<ItemStack> items = lootTable.getRandomItems(params);
        if (items.isEmpty()) return;

        ItemStackHandler storage = trashCan.getStorage();
        ObjectArrayList<ItemStack> candidates = new ObjectArrayList<>();

        for (ItemStack stack : items) {

            if (stack.isEmpty()) continue;

            boolean exists = false;

            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack in = storage.getStackInSlot(i);

                if (!in.isEmpty() && ItemStack.isSameItemSameComponents(in, stack)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                candidates.add(stack);
            }
        }

        if (candidates.isEmpty()) return;

        ItemStack chosen = candidates.get(level.random.nextInt(candidates.size()));

        trashCan.putItem(chosen.copy());
        setChanged();

        GoldenTweaks.LOGGER.debug(
                "Spawned garbage {} into trash can at {}",
                chosen,
                abovePos
        );
    }

    /* ------------------------------------------------------ */
    /* Village Type                                           */
    /* ------------------------------------------------------ */

    private VillageType getVillageType(ServerLevel level, BlockPos pos) {

        var biome = level.getBiome(pos);

        if (biome.is(BiomeTags.HAS_VILLAGE_DESERT)) return VillageType.DESERT;
        if (biome.is(BiomeTags.HAS_VILLAGE_SAVANNA)) return VillageType.SAVANNA;
        if (biome.is(BiomeTags.HAS_VILLAGE_SNOWY)) return VillageType.SNOWY;
        if (biome.is(BiomeTags.HAS_VILLAGE_TAIGA)) return VillageType.TAIGA;

        return VillageType.PLAINS;
    }

    /* ------------------------------------------------------ */
    /* Block State Sync                                       */
    /* ------------------------------------------------------ */

    public void refreshState() {

        if (!(level instanceof ServerLevel serverLevel)) return;

        VillageType type = getVillageType(serverLevel, worldPosition);
        boolean active = serverLevel.isVillage(worldPosition);

        BlockState current = getBlockState();

        BlockState target = current
                .setValue(GarbageStationFloorBlock.VILLAGE_TYPE, type)
                .setValue(GarbageStationFloorBlock.ACTIVE, active);

        if (!current.equals(target)) {
            level.setBlock(worldPosition, target, Block.UPDATE_ALL);
        }
    }

    /* ------------------------------------------------------ */
    /* Save / Load                                            */
    /* ------------------------------------------------------ */

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("LastCheckedDay", lastCheckedDay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        lastCheckedDay = tag.getLong("LastCheckedDay");
    }
}