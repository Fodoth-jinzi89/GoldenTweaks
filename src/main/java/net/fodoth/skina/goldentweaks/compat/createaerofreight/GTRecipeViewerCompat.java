package net.fodoth.skina.goldentweaks.compat.createaerofreight;

public final class GTRecipeViewerCompat {
    private GTRecipeViewerCompat() { }

    public static boolean supportsRuntimeChanges(Object manager) {
        // TMRV 0.9 locks its JEI registry permanently after baking recipes.
        return !manager.getClass().getName().startsWith("dev.nolij.toomanyrecipeviewers.");
    }
}
