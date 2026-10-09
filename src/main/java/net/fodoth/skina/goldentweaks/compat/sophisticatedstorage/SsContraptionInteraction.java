package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.content.contraptions.Contraption;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.VerticalFacing;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;
import org.joml.Vector3f;

/**
 * 移动结构上 SS 容器的手持物品交互，复刻 SS 方块本身的语义：
 * 开口面（facing）右键 → 装升级 / 存入对应槽位；其它面或空手 → 走默认（打开界面）。
 */
public final class SsContraptionInteraction {

    private SsContraptionInteraction() {
    }

    public static boolean handleItemInteraction(ServerPlayer player, Contraption contraption,
                                                SophisticatedStorageMountedStorage mounted, StructureBlockInfo info,
                                                Direction side, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        BlockState state = mounted.getState();
        if (!(state.getBlock() instanceof StorageBlockBase storageBlock)) {
            return false;
        }
        IStorageWrapper wrapper = mounted.getStorageWrapper();
        if (wrapper == null) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }

        // 升级安装最优先：任意面、任意潜行状态（存储升级标签内的升级物品）
        if (held.getItem() instanceof UpgradeItemBase<?> && held.is(ModItems.STORAGE_UPGRADE_TAG)
                && StorageBlockBase.tryAddSingleUpgrade(player, hand, held, wrapper)) {
            return true;
        }

        // tier 升级（基础→铁 等）：在装置上直接替换方块并迁移存储，而不是被当作物品塞入
        if (held.getItem() instanceof StorageTierUpgradeItem) {
            return SsTierUpgrade.tryUpgrade(player, contraption.entity, contraption, mounted, info, hand);
        }

        if (storageBlock.getFacing(state) != side) {
            return false;
        }

        // 限类桶：开口面右键把手持物品存入对应槽位（按命中位置选槽）
        if (storageBlock instanceof LimitedBarrelBlock limitedBarrel
                && !player.isShiftKeyDown()
                && mounted.getShadow() instanceof LimitedBarrelBlockEntity barrel) {
            int slot = interactionSlot(limitedBarrel, state, contraption, info.pos(), player);
            barrel.depositItem(player, hand, held, slot);
            return true;
        }
        return false;
    }

    /** 左键取出：与 SS 的 LimitedBarrelBlock.attack 一致（仅开口面、取一份 / Shift 取整栈）。 */
    public static void takeItem(ServerPlayer player, Contraption contraption,
                                SophisticatedStorageMountedStorage mounted, BlockPos localPos, Direction side) {
        BlockState state = mounted.getState();
        if (!(state.getBlock() instanceof LimitedBarrelBlock limitedBarrel)) {
            return;
        }
        if (limitedBarrel.getFacing(state) != side) {
            return;
        }
        IStorageWrapper wrapper = mounted.getStorageWrapper();
        if (wrapper == null) {
            return;
        }
        int slot = interactionSlot(limitedBarrel, state, contraption, localPos, player);
        InventoryHandler inventory = wrapper.getInventoryHandler();
        ItemStack stackInSlot = inventory.getStackInSlot(slot);
        if (stackInSlot.isEmpty()) {
            return;
        }
        int countToTake = player.isShiftKeyDown() ? Math.min(stackInSlot.getMaxStackSize(), stackInSlot.getCount()) : 1;
        ItemStack taken = inventory.extractItem(slot, countToTake, false);
        if (!taken.isEmpty() && !player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
    }

    /**
     * 复刻 SS {@code LimitedBarrelBlock#getInteractionSlot}：槽位由**命中点**在方块内的位置决定，
     * 而不是朝向。Create 的装置交互不传命中点，所以这里把玩家的视线射线变换到装置本地坐标系，
     * 与该方块的 AABB 求交，得到本地命中点后再套用 SS 的槽位判定。
     */
    private static int interactionSlot(LimitedBarrelBlock block, BlockState state, Contraption contraption,
                                       BlockPos localPos, ServerPlayer player) {
        int slots = block.getNumberOfInventorySlots();
        if (slots <= 1) {
            return 0;
        }
        Optional<Vec3> hitLocal = rayHitBlockLocal(contraption, localPos, player);
        if (hitLocal.isEmpty()) {
            return 0;
        }
        Vec3 localHit = hitLocal.get();
        Vector3f coords = new Vector3f(
                (float) (localHit.x - localPos.getX()),
                (float) (localHit.y - localPos.getY()),
                (float) (localHit.z - localPos.getZ()));
        coords.add(-0.5F, -0.5F, -0.5F);
        VerticalFacing verticalFacing = block.getVerticalFacing(state);
        if (verticalFacing != VerticalFacing.NO) {
            coords.rotate(LimitedBarrelBlock.getNorthBasedRotation(state.getValue(LimitedBarrelBlock.HORIZONTAL_FACING)));
            coords.rotate(LimitedBarrelBlock.getNorthBasedRotation(verticalFacing.getDirection().getOpposite()));
        } else {
            coords.rotate(LimitedBarrelBlock.getNorthBasedRotation(state.getValue(LimitedBarrelBlock.HORIZONTAL_FACING).getOpposite()));
        }
        coords.add(0.5F, 0.5F, 0.5F);
        boolean top = coords.y() > 0.5F;
        boolean right = coords.x() > 0.5F;
        if (slots == 2) {
            return top ? 0 : 1;
        }
        if (slots == 3) {
            return top ? 0 : (right ? 2 : 1);
        }
        return top ? (right ? 1 : 0) : (right ? 3 : 2);
    }

    private static Optional<Vec3> rayHitBlockLocal(Contraption contraption, BlockPos localPos, ServerPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 from = contraption.entity.toLocalVector(eye, 1.0F);
        Vec3 ahead = contraption.entity.toLocalVector(eye.add(player.getLookAngle()), 1.0F);
        Vec3 dir = ahead.subtract(from);
        if (dir.lengthSqr() < 1.0E-6) {
            return Optional.empty();
        }
        dir = dir.normalize();

        double[] mins = {localPos.getX(), localPos.getY(), localPos.getZ()};
        double[] maxs = {localPos.getX() + 1.0, localPos.getY() + 1.0, localPos.getZ() + 1.0};
        double[] origins = {from.x, from.y, from.z};
        double[] dirs = {dir.x, dir.y, dir.z};

        double tMin = 0.0;
        double tMax = 10.0;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(dirs[i]) < 1.0E-8) {
                if (origins[i] < mins[i] || origins[i] > maxs[i]) {
                    return Optional.empty();
                }
                continue;
            }
            double t1 = (mins[i] - origins[i]) / dirs[i];
            double t2 = (maxs[i] - origins[i]) / dirs[i];
            if (t1 > t2) {
                double swap = t1;
                t1 = t2;
                t2 = swap;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) {
                return Optional.empty();
            }
        }
        return Optional.of(from.add(dir.scale(tMin)));
    }
}
