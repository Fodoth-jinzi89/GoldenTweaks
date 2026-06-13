package net.fodoth.skina.goldentweaks.util;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class FamiliarCompat {

    private FamiliarCompat() {
    }

    @Nullable
    public static LivingEntity getOwner(Entity entity) {

        switch (entity) {
            case null -> {
                return null;
            }


            // 魔灵
            case AbstractSpellCastingPet pet -> {
                return pet.getSummoner();
            }


            // Iron召唤物
            case IMagicSummon summon -> {
                Entity owner = summon.getSummoner();
                return owner instanceof LivingEntity living ? living : null;
            }
            default -> {
            }
        }

        return null;
    }

    /**
     * 获取实体所属阵营主人
     * 玩家 -> 自己
     * 召唤物 -> 主人
     * 普通生物 -> 自己
     */
    @Nullable
    public static LivingEntity getTeamOwner(Entity entity) {

        if (!(entity instanceof LivingEntity living)) {
            return getOwner(entity);
        }

        LivingEntity owner = getOwner(entity);

        return owner != null ? owner : living;
    }

    public static boolean areFriendly(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        if (a == b) {
            return true;
        }

        LivingEntity teamA = getTeamOwner(a);
        LivingEntity teamB = getTeamOwner(b);

        if (teamA == null || teamB == null) {
            return false;
        }

        return teamA == teamB
                || teamA.isAlliedTo(teamB)
                || teamB.isAlliedTo(teamA);
    }

    public static boolean isPetOrSummon(Entity entity) {
        return entity instanceof AbstractSpellCastingPet
                || entity instanceof IMagicSummon;
    }

    public static boolean sharesOwner(Entity a, Entity b) {

        LivingEntity ownerA = getOwner(a);
        LivingEntity ownerB = getOwner(b);

        return ownerA != null
                && ownerA == ownerB;
    }

    public static boolean areFriendlyIncludingOwners(Entity a, Entity b) {

        if (areFriendly(a, b)) {
            return true;
        }

        LivingEntity ownerA = getOwner(a);
        LivingEntity ownerB = getOwner(b);

        if (areFriendly(ownerA, b)) {
            return true;
        }

        if (ownerB != null && areFriendly(a, ownerB)) {
            return true;
        }

        return ownerB != null
                && areFriendly(ownerA, ownerB);
    }
}