package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Reaches scex's aspect ingredient through reflection, as a plain {@code Object}.
 *
 * <p>Everything here is deliberately untyped: this class has to load when neither scex, TMRV nor
 * EMI is installed, so no viewer type may appear in its signature. The caller — which lives in a
 * package that already depends on EMI — is what turns the result back into a stack.</p>
 *
 * <p>scex ({@code scex_aspect_jei}) registers the aspect as a JEI ingredient type with its own
 * helper and renderer. TMRV exposes {@code TMRVStack.create(type, helper, renderer, ingredient)},
 * the ordinary route from a JEI ingredient to an EMI stack. Using that route rather than drawing the
 * icon by hand is what makes the aspect answer search and recipe lookups.</p>
 */
public final class GTScexAspectBridge {

    private static final String SCEX_INGREDIENT = "dev.scex.aspectjei.AspectIngredient";
    private static final String SCEX_HELPER = "dev.scex.aspectjei.AspectIngredientHelper";
    private static final String SCEX_RENDERER = "dev.scex.aspectjei.AspectIngredientRenderer";
    private static final String ASPECT = "thaumcraft.api.aspects.Aspect";
    private static final String TMRV_STACK =
            "dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack";

    /** Resolved once; both mods are present from the start or not at all. */
    private static boolean resolved;

    private static boolean usable;

    @Nullable
    private static Object ingredientType;

    @Nullable
    private static Object ingredientHelper;

    @Nullable
    private static Object ingredientRenderer;

    @Nullable
    private static Method stackFactory;

    @Nullable
    private static Method aspectLookup;

    @Nullable
    private static java.lang.reflect.Constructor<?> ingredientConstructor;

    /** The ingredient an scex stack holds, so the aspect can be read back out of it. */
    @Nullable
    private static java.lang.reflect.Field stackField;

    /** The viewer's stack class, which is the only one {@link #stackField} can be read from. */
    @Nullable
    private static Class<?> stackClass;

    @Nullable
    private static Class<?> ingredientClass;

    @Nullable
    private static Method aspectOfMethod;

    @Nullable
    private static Method tagMethod;

    private GTScexAspectBridge() {
    }

    /** Whether scex is available, so an aspect can be a real viewer entry. */
    public static boolean available() {
        resolve();
        return usable;
    }

    /**
     * The aspect as whatever stack type the viewer supplies, or {@code null} when unavailable.
     *
     * @param tag the aspect tag, e.g. {@code aer}
     */
    @Nullable
    public static Object stackFor(String tag) {
        if (!available() || tag == null) {
            return null;
        }
        try {
            Object aspect = aspectLookup.invoke(null, tag);
            if (aspect == null) {
                return null;
            }
            Object ingredient = ingredientConstructor.newInstance(aspect);
            return stackFactory.invoke(null,
                    ingredientType, ingredientHelper, ingredientRenderer, ingredient);
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return null;
        }
    }

    /**
     * The aspect tag an scex stack was built from, or {@code null} when it is not one.
     *
     * <p>Needed in the other direction: an aspect dragged from a recipe page arrives as whatever stack
     * the viewer supplied, and AE2 has to be told which aspect it is. The stack keeps its ingredient
     * in a public field, and the ingredient names its aspect, so both are read reflectively.</p>
     *
     * @param stack any {@code EmiStack}
     */
    @Nullable
    public static String tagOf(Object stack) {
        if (!available() || stack == null) {
            return null;
        }
        // The field lives on the viewer's own stack class, and this method is offered every stack the
        // game draws — an item, a Mekanism chemical, anything. Reading it off a foreign stack throws,
        // so the type is checked first rather than relying on the catch below.
        if (!stackClass.isInstance(stack)) {
            return null;
        }
        try {
            Object ingredient = stackField.get(stack);
            if (ingredient == null || !ingredientClass.isInstance(ingredient)) {
                return null;
            }
            Object aspect = aspectOfMethod.invoke(ingredient);
            return aspect == null ? null : (String) tagMethod.invoke(aspect);
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return null;
        }
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            ClassLoader loader = GTScexAspectBridge.class.getClassLoader();

            Class<?> scexIngredient = Class.forName(SCEX_INGREDIENT, false, loader);
            Class<?> helperClass = Class.forName(SCEX_HELPER, false, loader);
            Class<?> aspectClass = Class.forName(ASPECT, false, loader);
            Class<?> tmrvClass = Class.forName(TMRV_STACK, false, loader);

            aspectLookup = aspectClass.getMethod("getAspect", String.class);
            ingredientConstructor = scexIngredient.getConstructor(aspectClass);
            ingredientType = scexIngredient.getField("TYPE").get(null);
            ingredientHelper = helperClass.getConstructor().newInstance();
            ingredientRenderer = renderer(loader);

            stackFactory = tmrvClass.getMethod("create",
                    Class.forName("mezz.jei.api.ingredients.IIngredientType", false, loader),
                    Class.forName("mezz.jei.api.ingredients.IIngredientHelper", false, loader),
                    Class.forName("mezz.jei.api.ingredients.IIngredientRenderer", false, loader),
                    Object.class);

            // The reverse direction: a viewer stack keeps its ingredient in a public field, and that
            // ingredient exposes the aspect it was built from.
            stackField = tmrvClass.getField("ingredient");
            stackClass = tmrvClass;
            ingredientClass = scexIngredient;
            aspectOfMethod = scexIngredient.getMethod("aspect");
            tagMethod = aspectClass.getMethod("getTag");

            usable = true;
        } catch (ReflectiveOperationException | LinkageError | SecurityException unavailable) {
            usable = false;
        }
    }

    /**
     * scex's renderer, or {@code null} to let TMRV use the helper's own.
     *
     * <p>Both draw the same thing; the explicit instance is preferred only because it is the one
     * scex registered.</p>
     */
    @Nullable
    private static Object renderer(ClassLoader loader) {
        try {
            Class<?> rendererClass = Class.forName(SCEX_RENDERER, false, loader);
            return rendererClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError absent) {
            return null;
        }
    }
}
