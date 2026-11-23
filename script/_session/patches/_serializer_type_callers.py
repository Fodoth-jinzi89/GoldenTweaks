"""Point both registries at serializers that know their type."""
import io

p1 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipes.java"
s1 = io.open(p1, encoding="utf-8").read()
old1 = '''            SERIALIZERS.register(GTChemicalReactionRecipe.ID,
                    GTChemicalReactionRecipe.Serializer::new);'''
new1 = '''            SERIALIZERS.register(GTChemicalReactionRecipe.ID,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));'''
assert old1 in s1
s1 = s1.replace(old1, new1, 1)
io.open(p1, "w", encoding="utf-8", newline="\n").write(s1)
print("basic serializer <- basic type")

p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactionRecipes.java"
s2 = io.open(p2, encoding="utf-8").read()
old2 = '''            SERIALIZERS.register(DIRECTORY, GTChemicalReactionRecipe.Serializer::new);'''
new2 = '''            SERIALIZERS.register(DIRECTORY,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced serializer <- advanced type")
