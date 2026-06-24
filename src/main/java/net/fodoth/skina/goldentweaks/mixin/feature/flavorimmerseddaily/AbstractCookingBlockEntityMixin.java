package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.entity.AbstractCookingBlockEntity;
import com.fidtest.recipe.ApplianceType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCookingBlockEntity.class)
public abstract class AbstractCookingBlockEntityMixin {

    @Shadow
    public abstract ApplianceType getApplianceType();

    // =========================================================
    // DOWN：只允许输出 + 容器输出
    // =========================================================
    @Inject(method = "canTakeItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canTake(int index, ItemStack stack, Direction dir,
                            CallbackInfoReturnable<Boolean> cir) {

        ApplianceType type = this.getApplianceType();

        if (dir != Direction.DOWN) {
            cir.setReturnValue(false);
            return;
        }

        if (index == type.getOutputSlot()) {
            cir.setReturnValue(true);
            return;
        }

        if (type.hasContainer() && index == type.getContainerSlot()) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(false);
    }

    // =========================================================
    // INPUT / OUTPUT SLOT VISIBILITY
    // =========================================================
    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void gt$getSlots(Direction side, CallbackInfoReturnable<int[]> cir) {

        ApplianceType type = this.getApplianceType();
        int inputCount = type.getInputSlots();

        if (side == Direction.UP) {
            // UP：全部输入槽
            cir.setReturnValue(gt$allInput(type));
            return;
        }

        if (side == Direction.DOWN) {
            // DOWN：输出 + 容器
            if (type.hasContainer()) {
                cir.setReturnValue(new int[]{
                        type.getOutputSlot(),
                        type.getContainerSlot()
                });
            } else {
                cir.setReturnValue(new int[]{
                        type.getOutputSlot()
                });
            }
            return;
        }

        // SIDE：输入规则
        cir.setReturnValue(gt$sideInputSlots(type, side));
    }

    // =========================================================
    // INPUT PERMISSION（关键：必须同步 slots 逻辑）
    // =========================================================
    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canPlace(int index, ItemStack stack, Direction dir,
                             CallbackInfoReturnable<Boolean> cir) {

        ApplianceType type = this.getApplianceType();

        int start = type.getInputStartSlot();
        int count = type.getInputSlots();

        if (dir == Direction.UP) {
            cir.setReturnValue(index >= start && index < start + count);
            return;
        }

        if (dir == Direction.DOWN) {
            cir.setReturnValue(false);
            return;
        }

        // side：必须匹配规则
        int[] allowed = gt$sideInputSlots(type, dir);
        for (int s : allowed) {
            if (s == index) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }

    // =========================================================
    // helper
    // =========================================================
    @Unique
    private static int[] gt$allInput(ApplianceType type) {
        int start = type.getInputStartSlot();
        int count = type.getInputSlots();

        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    @Unique
    private static int[] gt$sideInputSlots(ApplianceType type, Direction side) {

        int start = type.getInputStartSlot();
        int count = type.getInputSlots();

        // 1~2个输入：所有侧面都能进
        // >=6：所有侧面都能进
        if (count <= 2 || count >= 6) {
            return gt$allInput(type);
        }


        return switch (count) {

            // 3 slots: 1→NS, 2→EW
            case 3 -> switch (side) {
                case NORTH, SOUTH -> new int[]{start + 1};
                case EAST, WEST -> new int[]{start + 2};
                default -> new int[0];
            };

            // 4 slots: 1NS 2E 3W
            case 4 -> switch (side) {
                case NORTH,SOUTH -> new int[]{start + 1};
                case EAST -> new int[]{start + 2};
                case WEST -> new int[]{start + 3};
                default -> new int[0];
            };

            // 5 slots: 1N 2E 3W 4S
            case 5 -> switch (side) {
                case NORTH -> new int[]{start + 1};
                case EAST -> new int[]{start + 2};
                case WEST -> new int[]{start + 3};
                case SOUTH -> new int[]{start + 4};
                default -> new int[0];
            };

            default -> gt$allInput(type);
        };
    }
}