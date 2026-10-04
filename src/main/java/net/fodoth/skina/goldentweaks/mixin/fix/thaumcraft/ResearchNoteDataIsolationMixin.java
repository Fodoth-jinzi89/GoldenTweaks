package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.research.HexUtils;
import thaumcraft.common.research.ResearchNoteData;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The research note generator carves a hex grid by randomly deleting blank hexes.
 * Its only guard ({@code canRemoveBlank}) checks that no single target aspect
 * (type 1) loses its last neighbour, which still allows a target to end up alone
 * on an island that can never be linked to the others — making the note
 * impossible to complete (e.g. "贸易"/permutatio on 高级源质存储).
 *
 * <p>The removal is therefore additionally rejected whenever it would split the
 * target aspects across two or more connected components of the remaining grid.
 */
@Mixin(value = ResearchNoteData.class, remap = false)
public class ResearchNoteDataIsolationMixin {

    @Inject(
            method = "canRemoveBlank(Ljava/util/Map;Lthaumcraft/common/research/HexUtils$Hex;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void gt$disallowIsolation(Map<String, ResearchNoteData.HexEntry> entries,
                                             HexUtils.Hex removed,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && gt$splitsTargets(entries, removed)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean gt$splitsTargets(Map<String, ResearchNoteData.HexEntry> entries, HexUtils.Hex removed) {
        String removedKey = removed.key();
        Set<String> targets = new HashSet<>();
        for (Map.Entry<String, ResearchNoteData.HexEntry> entry : entries.entrySet()) {
            if (!entry.getKey().equals(removedKey) && entry.getValue().type() == 1) {
                targets.add(entry.getKey());
            }
        }
        if (targets.size() < 2) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        ArrayDeque<HexUtils.Hex> queue = new ArrayDeque<>();
        String start = targets.iterator().next();
        visited.add(start);
        queue.add(entries.get(start).hex());
        while (!queue.isEmpty()) {
            HexUtils.Hex current = queue.removeFirst();
            for (int direction = 0; direction < 6; ++direction) {
                HexUtils.Hex neighbour = current.neighbour(direction);
                String key = neighbour.key();
                if (key.equals(removedKey) || !entries.containsKey(key) || !visited.add(key)) {
                    continue;
                }
                queue.addLast(neighbour);
            }
        }

        for (String target : targets) {
            if (!visited.contains(target)) {
                return true;
            }
        }
        return false;
    }
}
