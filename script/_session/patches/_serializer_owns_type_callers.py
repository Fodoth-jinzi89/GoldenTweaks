"""Each serializer knows which type it deserialises for.

A `RecipeSerializer` is handed no context when it parses a file, so it cannot ask "which type am I
currently being used as". Registering one serializer instance per type solves it: the instance
carries its type and stamps it onto every recipe it produces, both from JSON and from the wire.

That is what the two registries already do — they register separate serializer instances — so the
wiring is in place; the serializer just has to use it.
"""
import io

# ---- 1. the two registries tell their serializer which type it is -------------------------------
p1 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipes.java"
s1 = io.open(p1, encoding="utf-8").read()
old1 = '''                    GTChemicalReactionRecipe.Serializer::new);'''
new1 = '''                    () -> new GTChemicalReactionRecipe.Serializer(type()));'''
assert old1 in s1
s1 = s1.replace(old1, new1, 1)
io.open(p1, "w", encoding="utf-8", newline="\n").write(s1)
print("basic serializer knows its type")

p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactionRecipes.java"
s2 = io.open(p2, encoding="utf-8").read()
old2 = '''            SERIALIZERS.register(DIRECTORY, GTChemicalReactionRecipe.Serializer::new);'''
new2 = '''            SERIALIZERS.register(DIRECTORY,
                    () -> new GTChemicalReactionRecipe.Serializer(advancedType()));'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced serializer knows its type")
