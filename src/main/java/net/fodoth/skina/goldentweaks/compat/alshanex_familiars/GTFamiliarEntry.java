package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;

import java.util.UUID;

public record GTFamiliarEntry(UUID id, AbstractSpellCastingPet familiar, net.minecraft.network.chat.Component displayName, float health, int armor,
                              int enraged, boolean canBlock, float baseMaxHealth) {
}
