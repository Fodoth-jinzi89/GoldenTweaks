package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Our own multiblock structure format, described the way a player sees the structure:
 * bottom layer first, rows north to south, characters west to east.
 *
 * <pre>
 * {
 *   "layers": [
 *     ["xxx", "xxx", "xxx"],        // layer 1, the bottom one
 *     ["xxx", "xox", "xax"],
 *     ["xxx", "xxx", "xxx"],
 *     ["xxo", "xxx", "oxx"]         // layer 4, the top one
 *   ],
 *   "keys": {
 *     "a": { "block": "goldentweaks:chemical_plant_controller" },
 *     "x": "#goldentweaks:multiblock/ports"
 *   },
 *   "master": "a",
 *   "any": "o"
 * }
 * </pre>
 *
 * <p>{@code avaritia_more_items} indexes its pattern as {@code [depth][height][width]} with
 * depth counted northward and height counted downward, which cannot be read while editing.
 * Rather than constructing its objects directly, {@link #toAmiJson()} rewrites our layers into
 * that layout and hands the result to {@code AMiBlockPatternReloadListener.parsePattern}. That
 * entry point is stable across AMI builds — the pattern class' own constructors changed shape
 * between the jar we compile against and the one in the pack — and it keeps AMI in charge of
 * building its own predicates.</p>
 */
public final class GTMultiblockPattern {

    /** The wildcard character AMI's builder reserves: matches anything and is not checked. */
    public static final char AMI_ANY = '?';

    /** A master cell in our own coordinates: column west-east, layer bottom-up, row north-south. */
    public record Master(int column, int layer, int row) {
    }

    private final List<List<String>> layers;
    private final Map<Character, String> keys;
    private final char wildcard;
    private final Master master;

    private GTMultiblockPattern(List<List<String>> layers,
                                Map<Character, String> keys,
                                char wildcard,
                                Master master) {
        this.layers = layers;
        this.keys = keys;
        this.wildcard = wildcard;
        this.master = master;
    }

    public int width() {
        return layers.getFirst().getFirst().length();
    }

    public int depth() {
        return layers.getFirst().size();
    }

    public int height() {
        return layers.size();
    }

    public Master master() {
        return master;
    }

    /** Symbol at the given cell, in our own coordinates: column, layer (bottom-up), row. */
    public char symbolAt(int column, int layer, int row) {
        return layers.get(layer).get(row).charAt(column);
    }

    /** Whether the given symbol is the "match anything, check nothing" wildcard. */
    public boolean isWildcard(char symbol) {
        return symbol == wildcard;
    }

    /**
     * Resolves a symbol to the block or tag it stands for, or {@code null} for the wildcard.
     *
     * <p>Returned as the raw string from the structure file, so the caller decides whether it is
     * a block id or a {@code #tag}.</p>
     */
    @Nullable
    public String keyAt(char symbol) {
        return keys.get(symbol);
    }

    /** Every distinct non-wildcard symbol in the structure. */
    public Set<Character> symbols() {
        return keys.keySet();
    }

    public static GTMultiblockPattern parse(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            throw new JsonParseException("structure root must be an object");
        }
        JsonObject object = root.getAsJsonObject();

        char wildcard = 'o';
        if (object.has("any")) {
            wildcard = singleCharacter(object.get("any").getAsString(), "'any'");
        }

        Map<Character, String> keys = parseKeys(object);
        List<List<String>> layers = parseLayers(object, keys, wildcard);

        JsonElement masterElement = object.get("master");
        if (masterElement == null) {
            throw new JsonParseException("missing 'master' character");
        }
        char masterSymbol = singleCharacter(masterElement.getAsString(), "'master'");
        if (!keys.containsKey(masterSymbol)) {
            throw new JsonParseException(
                    "'master' symbol '" + masterSymbol + "' is not declared in 'keys'");
        }

        return new GTMultiblockPattern(layers, keys, wildcard, locate(layers, masterSymbol));
    }

    private static char singleCharacter(String value, String field) {
        if (value.length() != 1) {
            throw new JsonParseException(field + " must be a single character, got '" + value + "'");
        }
        return value.charAt(0);
    }

    private static Master locate(List<List<String>> layers, char symbol) {
        Master found = null;
        for (int layer = 0; layer < layers.size(); layer++) {
            List<String> rows = layers.get(layer);
            for (int row = 0; row < rows.size(); row++) {
                int column = rows.get(row).indexOf(symbol);
                if (column < 0) {
                    continue;
                }
                if (found != null) {
                    throw new JsonParseException(
                            "the master block appears more than once; layer " + (layer + 1)
                                    + " row " + (row + 1) + " duplicates the earlier cell");
                }
                found = new Master(column, layer, row);
            }
        }
        if (found == null) {
            throw new JsonParseException("the master block '" + symbol + "' is not in any layer");
        }
        return found;
    }

    private static Map<Character, String> parseKeys(JsonObject object) {
        JsonElement element = object.get("keys");
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException("missing 'keys' object");
        }

        Map<Character, String> keys = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            char symbol = singleCharacter(entry.getKey(), "key");
            if (symbol == AMI_ANY || symbol == ' ') {
                throw new JsonParseException("key '" + entry.getKey() + "' is reserved");
            }
            keys.put(symbol, parseKeyValue(entry.getValue(), entry.getKey()));
        }
        return keys;
    }

    /**
     * Reduces a key to the block id or {@code #tag} AMI accepts, validating it eagerly so a typo
     * is reported against the structure file rather than surfacing later as a silent mismatch.
     */
    private static String parseKeyValue(JsonElement element, String name) {
        if (!element.isJsonPrimitive()) {
            if (element.isJsonObject()) {
                JsonElement block = element.getAsJsonObject().get("block");
                if (block == null) {
                    throw new JsonParseException("key '" + name + "' needs a 'block' field");
                }
                return validate(block.getAsString(), name);
            }
            throw new JsonParseException("key '" + name + "' must be a block id, #tag or object");
        }
        return validate(element.getAsString(), name);
    }

    private static String validate(String id, String name) {
        if (id.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(id.substring(1));
            if (location == null) {
                throw new JsonParseException("key '" + name + "' has an invalid tag '" + id + "'");
            }
            // Resolving the tag key proves the id parses; membership is checked at match time.
            TagKey.create(Registries.BLOCK, location);
            return id;
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new JsonParseException("key '" + name + "' has an invalid block id '" + id + "'");
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(location)
                .orElseThrow(() -> new JsonParseException(
                        "key '" + name + "' refers to unknown block '" + id + "'"));
        // Keep the id AMI will look up, so both sides agree on the canonical spelling.
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static List<List<String>> parseLayers(JsonObject object,
                                                  Map<Character, String> keys,
                                                  char wildcard) {
        JsonElement element = object.get("layers");
        if (element == null || !element.isJsonArray()) {
            throw new JsonParseException("missing 'layers' array");
        }

        JsonArray layerArray = element.getAsJsonArray();
        if (layerArray.isEmpty()) {
            throw new JsonParseException("'layers' must not be empty");
        }

        List<List<String>> layers = new ArrayList<>(layerArray.size());
        int depth = -1;
        int width = -1;

        for (int index = 0; index < layerArray.size(); index++) {
            JsonElement layerElement = layerArray.get(index);
            if (!layerElement.isJsonArray()) {
                throw new JsonParseException("layer " + (index + 1) + " must be an array of rows");
            }
            JsonArray rowArray = layerElement.getAsJsonArray();
            if (rowArray.isEmpty()) {
                throw new JsonParseException("layer " + (index + 1) + " must not be empty");
            }

            List<String> rows = new ArrayList<>(rowArray.size());
            for (JsonElement rowElement : rowArray) {
                String row = rowElement.getAsString();
                if (row.isEmpty()) {
                    throw new JsonParseException("layer " + (index + 1) + " contains an empty row");
                }
                rows.add(row);
            }

            if (depth < 0) {
                depth = rows.size();
                width = rows.getFirst().length();
            }
            if (rows.size() != depth) {
                throw new JsonParseException(
                        "layer " + (index + 1) + " has " + rows.size() + " rows, expected " + depth);
            }
            for (int row = 0; row < rows.size(); row++) {
                if (rows.get(row).length() != width) {
                    throw new JsonParseException(
                            "layer " + (index + 1) + " row " + (row + 1) + " is "
                                    + rows.get(row).length() + " wide, expected " + width);
                }
            }
            layers.add(rows);
        }

        for (int layer = 0; layer < layers.size(); layer++) {
            List<String> rows = layers.get(layer);
            for (int row = 0; row < rows.size(); row++) {
                String line = rows.get(row);
                for (int column = 0; column < line.length(); column++) {
                    char symbol = line.charAt(column);
                    if (symbol != wildcard && !keys.containsKey(symbol)) {
                        throw new JsonParseException(
                                "layer " + (layer + 1) + " row " + (row + 1)
                                        + " column " + (column + 1) + " uses '" + symbol
                                        + "', which is not declared in 'keys'");
                    }
                }
            }
        }
        return layers;
    }

    /**
     * Rewrites our layers into the document {@code AMiBlockPatternReloadListener.parsePattern}
     * expects.
     *
     * <p>AMI walks {@code pattern[d][h][w]} at offsets {@code (w, h, d)} along
     * {@code (right, up, forwards)}, so depth counts northward from the front row and height
     * counts downward from the top layer. Our layers run bottom-up and our rows run north to
     * south, hence both indices are reversed here.</p>
     */
    public JsonObject toAmiJson() {
        JsonArray pattern = new JsonArray();
        for (int d = 0; d < depth(); d++) {
            int row = depth() - 1 - d;
            JsonArray element = new JsonArray();
            for (int h = 0; h < height(); h++) {
                int layer = height() - 1 - h;
                element.add(new JsonPrimitive(
                        layers.get(layer).get(row).replace(wildcard, AMI_ANY)));
            }
            pattern.add(element);
        }

        JsonObject keysObject = new JsonObject();
        for (Map.Entry<Character, String> entry : keys.entrySet()) {
            keysObject.add(new JsonPrimitive(entry.getKey()).getAsString(),
                    new JsonPrimitive(entry.getValue()));
        }

        JsonArray masterArray = new JsonArray();
        masterArray.add(master.column());
        masterArray.add(height() - 1 - master.layer());
        masterArray.add(depth() - 1 - master.row());

        JsonObject document = new JsonObject();
        document.add("pattern", pattern);
        document.add("keys", keysObject);
        document.add("master", masterArray);
        return document;
    }

    /** Describes the parsed structure; used for the reload log line. */
    public String describe() {
        return width() + "x" + depth() + "x" + height()
                + " (w x d x h), master at column " + (master.column() + 1)
                + ", layer " + (master.layer() + 1) + ", row " + (master.row() + 1);
    }
}
