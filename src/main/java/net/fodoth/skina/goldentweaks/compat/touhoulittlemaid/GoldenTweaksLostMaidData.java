package net.fodoth.skina.goldentweaks.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.github.tartaricacid.touhoulittlemaid.inventory.chest.ChestManager;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

@LittleMaidExtension
public class GoldenTweaksLostMaidData implements ILittleMaid {
    public static TaskDataKey<Boolean> TAMED_LOST_MAID;

    public GoldenTweaksLostMaidData() {
    }

    public void registerTaskData(TaskDataRegister register) {
        TAMED_LOST_MAID = register.register(ResourceLocation.fromNamespaceAndPath("touhou_lost_maid", "tamed_lost_maid"), Codec.BOOL.fieldOf("value").codec());
    }

    @Override
    public void addChestType(ChestManager manager) {
        if (ModList.get().isLoaded("sophisticatedstorage")) {
            manager.add(new SophisticatedStorageChestType());
        }
    }
}
