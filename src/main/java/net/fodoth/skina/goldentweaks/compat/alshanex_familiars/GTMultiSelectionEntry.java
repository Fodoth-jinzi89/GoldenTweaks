package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public record GTMultiSelectionEntry(
        UUID id,
        AbstractSpellCastingPet familiar,
        Component displayName,
        float health,
        int armor,
        int enraged,
        boolean canBlock,
        float baseMaxHealth
) {}
