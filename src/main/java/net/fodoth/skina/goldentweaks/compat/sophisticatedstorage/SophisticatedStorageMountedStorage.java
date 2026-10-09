package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

/**
 * 移动结构上的精致存储容器：以真实 SS 存储包装器（物品 + 升级 + 设置）为数据源，
 * 既支持 I/O 与渲染同步，也让升级栏/设置栏与物品升级在移动中可用。
 */
public class SophisticatedStorageMountedStorage extends MountedItemStorage implements SyncedMountedStorage {

    public static final MapCodec<SophisticatedStorageMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(SophisticatedStorageMountedStorage::getState),
            CompoundTag.CODEC.fieldOf("be").forGetter(SophisticatedStorageMountedStorage::getBlockEntityTag)
    ).apply(instance, SophisticatedStorageMountedStorage::new));

    private final BlockState state;
    private final CompoundTag blockEntityTag;

    private StorageBlockEntity shadow;
    private HolderLookup.Provider registries;
    private boolean dirty;
    private boolean renderDirty;

    public SophisticatedStorageMountedStorage(BlockState state, CompoundTag blockEntityTag) {
        super(SSCreateCompat.SOPHISTICATED_STORAGE.get());
        this.state = state;
        this.blockEntityTag = blockEntityTag;
    }

    public BlockState getState() {
        return state;
    }

    /**
     * CODEC 编码用它。影子 BE 就绪后返回实时 NBT，否则退回装配时的快照——
     * 这样 Create 的挂载存储同步（SyncedMountedStorage 的 dirty 同步）才会把当前内容/设置/升级
     * 带给客户端，而不是装配那一刻的旧内容。
     */
    public CompoundTag getBlockEntityTag() {
        if (shadow != null && registries != null) {
            return shadow.saveWithoutMetadata(registries);
        }
        return blockEntityTag;
    }

    /** 需要 HolderLookup.Provider 才能把 SS 的升级/设置 NBT 装回 BE；由 Mixin 在 read 时提供。 */
    public void ensureShadow(HolderLookup.Provider registries) {
        if (shadow != null) {
            return;
        }
        try {
            BlockEntity created = state.getBlock() instanceof EntityBlock entityBlock
                    ? entityBlock.newBlockEntity(BlockPos.ZERO, state)
                    : null;
            if (!(created instanceof StorageBlockEntity storageBlockEntity)) {
                return;
            }
            storageBlockEntity.loadWithComponents(blockEntityTag, registries);
            storageBlockEntity.getStorageWrapper().getInventoryHandler().addListener(slot -> {
                dirty = true;
                renderDirty = true;
            });
            shadow = storageBlockEntity;
            this.registries = registries;
            // 首次把外观推给客户端（否则客户端在第一次内容变化前没有 renderInfo）
            renderDirty = true;
        } catch (Throwable ignored) {
        }
    }

    public IStorageWrapper getStorageWrapper() {
        return shadow == null ? null : shadow.getStorageWrapper();
    }

    public StorageBlockEntity getShadow() {
        return shadow;
    }

    // 不缓存 handler：SS 的 wrapper 在菜单打开/设置变更后可能被重建，缓存到的是孤立的旧 handler，
    // 会让自动化接口（Create 漏斗等）的写入落到失效对象上，表现为「存不进去」。
    private IItemHandlerModifiable inventory() {
        return shadow == null ? null : shadow.getStorageWrapper().getInventoryForInputOutput();
    }

    @Override
    public int getSlots() {
        IItemHandlerModifiable handler = inventory();
        return handler == null ? 0 : handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandlerModifiable handler = inventory();
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        IItemHandlerModifiable handler = inventory();
        if (handler == null) {
            return stack;
        }
        ItemStack remainder = handler.insertItem(slot, stack, simulate);
        if (!simulate && remainder.getCount() != stack.getCount()) {
            dirty = true;
            renderDirty = true;
        }
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandlerModifiable handler = inventory();
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = handler.extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            dirty = true;
            renderDirty = true;
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandlerModifiable handler = inventory();
        return handler == null ? 0 : handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        IItemHandlerModifiable handler = inventory();
        return handler != null && handler.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        IItemHandlerModifiable handler = inventory();
        if (handler != null) {
            handler.setStackInSlot(slot, stack);
            dirty = true;
            renderDirty = true;
        }
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, BlockEntity blockEntity) {
        if (shadow == null || !(blockEntity instanceof StorageBlockEntity target)) {
            return;
        }
        target.loadWithComponents(shadow.saveWithoutMetadata(level.registryAccess()), level.registryAccess());
        target.setChanged();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    /** 供 tier 升级等外部改动后主动触发一次同步。 */
    public void markDirty() {
        dirty = true;
        renderDirty = true;
    }

    public boolean isRenderDirty() {
        return renderDirty;
    }

    public void markRenderClean() {
        renderDirty = false;
    }

    /**
     * 服务端把当前外观打包成 renderInfo NBT，供同步给客户端 BE。外观刷新统一走这条推送，
     * 客户端 afterSync 不再改 renderInfo，避免两路互写造成闪动。
     *
     * <p>这里<b>不调用</b> {@code itemDisplay.itemsChanged()} / {@code renderInfo.refresh...}：它们会触发
     * wrapper 的 save 与渲染更新回调，作用在没有世界方块的影子 BE 上会破坏挂载存储的状态（存取会失效）。
     * 改为直接把显示物/数量写进 renderInfo 的 itemDisplay 子标签（沿用 SS 的默认行为：未勾选显示槽位时
     * 显示全部槽位）。</p>
     */
    public CompoundTag buildRenderInfoTag() {
        if (shadow == null || registries == null) {
            return new CompoundTag();
        }
        try {
            StorageWrapper wrapper = shadow.getStorageWrapper();
            InventoryHandler inventory = wrapper.getInventoryHandler();
            ItemDisplaySettingsCategory itemDisplay =
                    wrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class);
            if (itemDisplay == null) {
                return new CompoundTag();
            }
            CompoundTag renderInfo = wrapper.save(new CompoundTag()).getCompound("renderInfo");
            renderInfo.put("itemDisplay", buildItemDisplayTag(inventory, itemDisplay));
            return renderInfo;
        } catch (Throwable ignored) {
            return new CompoundTag();
        }
    }

    private CompoundTag buildItemDisplayTag(InventoryHandler inventory, ItemDisplaySettingsCategory itemDisplay) {
        int slots = inventory.getSlots();
        List<Integer> selected = itemDisplay.getSlots();

        ListTag items = new ListTag();
        if (selected.isEmpty()) {
            for (int slotIndex = 0; slotIndex < slots; slotIndex++) {
                items.add(buildDisplayItemTag(inventory, itemDisplay, slotIndex));
            }
        } else {
            for (int slotIndex : selected) {
                if (slotIndex >= 0 && slotIndex < slots) {
                    items.add(buildDisplayItemTag(inventory, itemDisplay, slotIndex));
                }
            }
        }

        int[] counts = new int[slots];
        ListTag fillRatios = new ListTag();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            counts[i] = stack.getCount();
            fillRatios.add(FloatTag.valueOf(stack.isEmpty() ? 0.0F : (float) stack.getCount() / Math.max(1, stack.getMaxStackSize())));
        }

        CompoundTag itemDisplayTag = new CompoundTag();
        itemDisplayTag.put("items", items);
        itemDisplayTag.putIntArray("inaccessibleSlots", new int[0]);
        itemDisplayTag.putIntArray("infiniteSlots", new int[0]);
        itemDisplayTag.putIntArray("slotCounts", counts);
        itemDisplayTag.put("slotFillRatios", fillRatios);
        return itemDisplayTag;
    }

    private CompoundTag buildDisplayItemTag(InventoryHandler inventory, ItemDisplaySettingsCategory itemDisplay, int slotIndex) {
        ItemStack stack = inventory.getStackInSlot(slotIndex);
        ItemStack copy = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        CompoundTag itemTag = new CompoundTag();
        itemTag.put("item", copy.saveOptional(registries));
        itemTag.putInt("rotation", itemDisplay.getRotation(slotIndex));
        itemTag.putInt("slotIndex", slotIndex);
        itemTag.putString("displaySide", itemDisplay.getDisplaySide().getSerializedName());
        itemTag.putInt("zOffset", itemDisplay.getZOffset(slotIndex));
        return itemTag;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity blockEntity = contraption.getBlockEntityClientSide(localPos);
        if (!(blockEntity instanceof StorageBlockEntity storageBlockEntity)) {
            return;
        }
        try {
            StorageWrapper wrapper = storageBlockEntity.getStorageWrapper();
            InventoryHandler inventory = wrapper.getInventoryHandler();
            int slots = Math.min(inventory.getSlots(), getSlots());
            for (int i = 0; i < slots; i++) {
                inventory.setStackInSlot(i, getStackInSlot(i).copy());
            }
            // 外观（renderInfo）统一由服务端 S2CSsStorageRenderPacket 推送；
            // 这里不再重算，避免与推送两路互写导致闪动。
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected Component getMenuName(StructureBlockInfo info, Contraption contraption) {
        BlockState state = info.state();
        CompoundTag nbt = info.nbt();
        WoodType woodType = null;
        if (nbt != null && nbt.contains("woodType")) {
            String woodName = nbt.getString("woodType");
            woodType = WoodType.values().filter(wt -> wt.name().equals(woodName)).findFirst().orElse(null);
        }
        Component blockName = WoodStorageBlockItem.getDisplayName(state.getBlock().getDescriptionId(), woodType);
        return CreateLang.translateDirect("contraptions.moving_container", blockName);
    }

    @Override
    public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
        if (shadow == null) {
            ensureShadow(player.serverLevel().registryAccess());
            if (shadow == null) {
                return false;
            }
        }
        ServerLevel level = player.serverLevel();
        BlockPos localPos = info.pos();
        Vec3 localPosVec = Vec3.atCenterOf(localPos);
        Predicate<Player> stillValid = p -> isMenuValid(player, contraption, contraption.entity.toGlobalVector(localPosVec, 0.0F));
        Component menuName = getMenuName(info, contraption);
        OptionalInt opened = player.openMenu(
                new SimpleMenuProvider(
                        (windowId, inventory, menuPlayer) -> new SsStorageMenu(SsStorageMenus.storageType(), windowId, menuPlayer,
                                shadow.getStorageWrapper(), contraption.entity.getId(), localPos),
                        menuName),
                buffer -> {
                    buffer.writeVarInt(contraption.entity.getId());
                    buffer.writeBlockPos(localPos);
                });
        if (opened.isPresent()) {
            playOpeningSound(level, contraption.entity.toGlobalVector(localPosVec, 0.0F));
            return true;
        }
        return false;
    }
}
