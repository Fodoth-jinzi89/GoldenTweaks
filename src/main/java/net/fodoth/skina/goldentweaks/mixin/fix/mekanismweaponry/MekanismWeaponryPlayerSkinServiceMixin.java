package net.fodoth.skina.goldentweaks.mixin.fix.mekanismweaponry;

import com.github.x3r.mekanism_weaponry.client.skin.PlayerSkinService;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(PlayerSkinService.class)
public class MekanismWeaponryPlayerSkinServiceMixin {

    /**
     * @author Fodoth_jinzi89
     * @reason Disable auth fetch, use dummy profile to avoid auth/thread crash
     */
    @Overwrite
    public static void reloadCachedSkins() {

        Minecraft mc = Minecraft.getInstance();

        UUID uuid = mc.getUser().getProfileId();

        GameProfile dummy = new GameProfile(
                uuid,
                mc.getUser().getName()
        );

        PlayerSkinService.CACHED_PROFILES.put(uuid, dummy);
    }
}