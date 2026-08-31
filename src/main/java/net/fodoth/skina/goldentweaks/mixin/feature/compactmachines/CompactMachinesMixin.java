package net.fodoth.skina.goldentweaks.mixin.feature.compactmachines;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.fodoth.skina.goldentweaks.compat.compactmachines.CompactMachinesAdditionalBlocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(CompactMachines.class)
public class CompactMachinesMixin {

    @Redirect(
            method = "newRoom",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/compactmods/machines/api/room/CompactRoomGenerator;generateRoom(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/phys/AABB;)V"
            )
    )
    private static void gt$generateCustomWalls(
            LevelAccessor level, AABB boundaries, MinecraftServer server, RoomTemplate template, UUID owner
    ) {
        if (template.internalDimensions().width() == 64 && template.internalDimensions().depth() == 64) {
            BlockState wall = template.defaultMachineColor().red() == 0x87
                    && template.defaultMachineColor().green() == 0xCE
                    && template.defaultMachineColor().blue() == 0xEB
                    ? CompactMachinesAdditionalBlocks.SKY_BLOCK.get().defaultBlockState()
                    : CompactMachinesAdditionalBlocks.STARRY_BLOCK.get().defaultBlockState();
            CompactRoomGenerator.generateRoom(level, boundaries, wall);
            return;
        }
        CompactRoomGenerator.generateRoom(level, boundaries);
    }
}
