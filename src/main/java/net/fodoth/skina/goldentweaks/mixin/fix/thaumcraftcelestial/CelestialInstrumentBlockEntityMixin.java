package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraftcelestial;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.celestial.common.blockentity.CelestialInstrumentBlockEntity;

@Mixin(value = CelestialInstrumentBlockEntity.class, remap = false)
public class CelestialInstrumentBlockEntityMixin {

    @ModifyArg(
            method = "processHeldCatalyst",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private Item gt$replaceCelestialCatalyst(Item original) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(original);

        return switch (id.toString()) {
            case "minecraft:quartz" -> gt$item("avaritia:blaze_cube");
            case "minecraft:amethyst_shard" -> gt$item("spectrum:moonstruck_nectar");
            case "thaumcraft:thaumium_ingot" -> gt$item("thaumic_tinkerer:ichorium_ingot");
            default -> original;
        };
    }

    private static Item gt$item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    @Redirect(
            method = "processHeldCatalyst",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean gt$dropFakePlayerOutput(Inventory inventory, ItemStack output, Player player) {
        if (!(player instanceof FakePlayer)) {
            return inventory.add(output);
        }

        BlockEntity instrument = (BlockEntity) (Object) this;
        if (instrument.getLevel() != null) {
            Containers.dropItemStack(
                    instrument.getLevel(),
                    instrument.getBlockPos().getX() + 0.5,
                    instrument.getBlockPos().getY() + 1.2,
                    instrument.getBlockPos().getZ() + 0.5,
                    output
            );
        }
        return true;
    }
}
