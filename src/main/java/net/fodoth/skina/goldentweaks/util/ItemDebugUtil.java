package net.fodoth.skina.goldentweaks.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class ItemDebugUtil {

    private ItemDebugUtil() {
    }

    public static void dumpItemInfo(ItemStack stack, StringBuilder sb) {
        if (stack.isEmpty()) {
            sb.append("[ITEM] empty stack\n");
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        sb.append("\n[ITEM CONTEXT]\n");
        sb.append("displayName=").append(stack.getHoverName().getString()).append("\n");
        sb.append("registry=").append(id).append("\n");
        sb.append("key=").append(stack.getDescriptionId()).append("\n");

        dumpTooltip(stack, sb);
        dumpComponents(stack, sb);
        dumpTags(stack, sb);
        dumpFluidInfo(stack, sb);
        dumpModelInfo(stack, sb);
    }

    // ================= MODEL =================
    private static void dumpModelInfo(ItemStack stack, StringBuilder sb) {
        try {
            Minecraft mc = Minecraft.getInstance();
            ItemModelShaper shaper = mc.getItemRenderer().getItemModelShaper();

            BakedModel model = shaper.getItemModel(stack.getItem());

            if (model != null) {
                @SuppressWarnings("deprecation")
                TextureAtlasSprite particle = model.getParticleIcon();

                sb.append("\n[Model]\n");
                sb.append("model=").append(model).append("\n");
                sb.append("modelClass=").append(model.getClass().getSimpleName()).append("\n");

                ResourceLocation particleName = particle.contents().name();
                sb.append("texture(particle)=").append(particleName).append("\n");
                sb.append("atlas=").append(particle.atlasLocation()).append("\n");
                sb.append("sprite=").append(particle.contents().name()).append("\n");
                sb.append("gui3d=").append(model.isGui3d()).append("\n");
                sb.append("usesBlockLight=").append(model.usesBlockLight()).append("\n");
            }
        } catch (Exception e) {
            sb.append("model=error: ").append(e.getMessage()).append("\n");
        }
    }

    // ================= DATA COMPONENTS =================
    private static void dumpComponents(ItemStack stack, StringBuilder sb) {
        sb.append("\n[DATA]\n");
        try {
            sb.append("count=").append(stack.getCount()).append("\n");
            sb.append("damage=").append(stack.getDamageValue()).append("\n");
            dumpComponentsPretty(stack, sb);
        } catch (Exception e) {
            sb.append("components=error: ").append(e.getMessage()).append("\n");
        }
    }

    private static void dumpComponentsPretty(ItemStack stack, StringBuilder sb) {
        sb.append("\n[COMPONENTS]\n");
        try {
            String raw = String.valueOf(stack.getComponents());
            int start = raw.indexOf("{");
            int end = raw.lastIndexOf("}");
            if (start >= 0 && end > start) {
                raw = raw.substring(start + 1, end);
            }

            List<String> parts = splitTopLevel(raw);
            parts.sort(java.util.Comparator.comparing(s -> {
                int idx = s.indexOf("=>");
                return idx > 0 ? s.substring(0, idx) : s;
            }));

            for (String part : parts) {
                printPretty(part, sb, 0);
            }
        } catch (Exception e) {
            sb.append("components=error: ").append(e.getMessage()).append("\n");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void printPretty(String text, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent);
        int idx = text.indexOf("=>");

        if (idx < 0) {
            sb.append(prefix).append(text).append("\n");
            return;
        }

        String key = text.substring(0, idx).trim();
        String value = text.substring(idx + 2).trim();

        if ("minecraft:tool".equals(key) || key.endsWith(":tool")) {
            sb.append(prefix).append(key).append(" => <skipped>\n");
            return;
        }

        if (key.contains("attribute_modifiers")) {
            dumpAttributeModifiers(value, sb);
        } else if (key.contains("enchantments")) {
            dumpEnchantments(value, sb, indent);
        } else if (key.contains("affixes")) {
            dumpAffixes(value, sb, indent);
        } else if (key.contains("properties")) {
            dumpSilentGearProperties(value, sb, indent);
        } else {
            sb.append(prefix).append(key).append(" => ");
            if (isComplex(value)) {
                sb.append("\n");
                printNested(value, sb, indent + 1);
            } else {
                sb.append(value).append("\n");
            }
        }
    }

    private static boolean isComplex(String v) {
        return v != null && (v.contains("[") || v.contains("{") || v.contains("("));
    }

    private static void printNested(String value, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent);
        sb.append(prefix).append(value).append("\n");
    }

    // ================= ENCHANTMENTS =================
    private static void dumpEnchantments(String raw, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        sb.append(prefix).append("\n[Enchantments]\n");

        String body = extractBracedBody(raw, "enchantments={");
        if (body == null) {
            sb.append(prefix).append("  none\n");
            return;
        }

        List<String> entries = splitTopLevel(body);
        for (String e : entries) {
            parseEnchantmentEntry(e, sb, indent + 1);
        }
        sb.append("\n");
    }

    private static void parseEnchantmentEntry(String entry, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        int arrow = entry.indexOf("=>");
        if (arrow < 0) return;

        String left = entry.substring(0, arrow).trim();
        String level = entry.substring(arrow + 2).trim();
        String id = extractEnchantmentId(left);

        sb.append(prefix).append(id).append(" => ").append(level).append("\n");
    }

    private static String extractEnchantmentId(String raw) {
        int slash = raw.indexOf('/');
        if (slash >= 0 && slash < raw.length() - 1) {
            String right = raw.substring(slash + 1).trim();
            right = right.replace("]", "").trim();
            int space = right.indexOf(' ');
            if (space > 0) right = right.substring(0, space);
            right = right.replace("=Enchantment","");
            return right;
        }
        return raw;
    }

    // ================= AFFIXES =================
    private static void dumpAffixes(String raw, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        sb.append(prefix).append("[Affixes]\n");

        String body = extractBracedBody(raw, "affixes={");
        if (body == null) {
            sb.append(prefix).append("  none\n");
            return;
        }

        List<String> entries = splitTopLevel(body);
        for (String e : entries) {
            parseAffixEntry(e, sb, indent + 1);
        }
        sb.append("\n");
    }

    private static void parseAffixEntry(String entry, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        int arrow = entry.indexOf("=>");
        if (arrow < 0) return;

        String left = entry.substring(0, arrow).trim();
        String value = entry.substring(arrow + 2).trim();
        String id = extractAffixId(left);

        sb.append(prefix).append(id).append(" => ").append(trimFloat(value)).append("\n");
    }

    private static String extractAffixId(String raw) {
        int slash = raw.indexOf('/');
        if (slash >= 0 && slash < raw.length() - 1) {
            String right = raw.substring(slash + 1).trim();
            return right.replace("}", "").trim();
        }
        return raw;
    }

    private static String trimFloat(String v) {
        try {
            double d = Double.parseDouble(v.replace("}", "").trim());
            return String.format("%.2f", d);
        } catch (Exception e) {
            return v.replace("}", "").trim();
        }
    }

    // ================= SILENT GEAR PROPERTIES =================
    private static void dumpSilentGearProperties(String raw, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        sb.append(prefix).append("\n[SilentGear Properties]\n");

        String body = extractBracedBody(raw, "properties={");
        if (body == null) {
            sb.append(prefix).append("  none\n");
            return;
        }

        List<String> entries = splitTopLevel(body);
        for (String e : entries) {
            parseSilentGearProperty(e, sb, indent + 1);
        }
        sb.append("\n");
    }

    private static void parseSilentGearProperty(String entry, StringBuilder sb, int indent) {
        String prefix = "  ".repeat(indent + 1);
        int eq = entry.indexOf('=');
        if (eq < 0) return;

        String key = entry.substring(0, eq).trim();
        String value = entry.substring(eq + 1).trim();

        sb.append(prefix).append(key).append(" => ");

        if (value.startsWith("[") && value.endsWith("]")) {
            sb.append("\n");
            String inner = value.substring(1, value.length() - 1);
            List<String> items = splitTopLevel(inner);
            for (String i : items) {
                sb.append(prefix).append("  ").append(i.trim()).append("\n");
            }
            return;
        }

        sb.append(trimSilentGearValue(value)).append("\n");
    }

    private static String trimSilentGearValue(String v) {
        v = v.replace("}", "").trim();
        if (v.startsWith("AVERAGE")) {
            String[] parts = v.split(" ");
            if (parts.length == 2) {
                try {
                    double d = Double.parseDouble(parts[1]);
                    return " " + String.format("%.2f", d);
                } catch (Exception ignored) {}
            }
        }
        return v;
    }

    // ================= ATTRIBUTE MODIFIERS =================
    private static void dumpAttributeModifiers(String raw, StringBuilder sb) {
        try {
            sb.append("\n    [Attributes]\n");
            sb.append("    raw=").append(raw).append("\n");

            List<String> entries = extractEntries(raw);
            if (entries.isEmpty()) {
                sb.append("    none\n\n");
                return;
            }

            for (String e : entries) {
                parseEntry(e, sb);
            }
        } catch (Exception e) {
            sb.append("attribute_error=").append(e.getMessage()).append("\n");
        }
        sb.append("\n");
    }

    private static List<String> extractEntries(String raw) {
        List<String> out = new java.util.ArrayList<>();
        int i = 0;
        while (true) {
            int start = raw.indexOf("Entry[", i);
            if (start < 0) break;

            int depth = 0;
            boolean started = false;
            for (int j = start; j < raw.length(); j++) {
                char c = raw.charAt(j);
                if (c == '[') { depth++; started = true; }
                if (c == ']') { depth--; }
                if (started && depth == 0) {
                    out.add(raw.substring(start, j + 1));
                    i = j + 1;
                    break;
                }
                if (j == raw.length() - 1) {
                    i = raw.length();
                    break;
                }
            }
        }
        return out;
    }

    private static void parseEntry(String entry, StringBuilder sb) {
        String attr = extractAttributeKey(entry);
        String amount = extractBetween(entry, "amount=", ",");
        String operation = extractBetween(entry, "operation=", ",");
        String slot = extractBetween(entry, "slot=", "]");
        String modifierId = extractBetween(entry, "id=", ",");

        if ("unknown".equals(operation)) {
            operation = extractBetween(entry, "operation=", "]");
        }
        operation = operation.replace("]", "").trim();

        sb.append("    ").append(attr).append(" (").append(slot).append(")\n");
        sb.append("        ").append(modifierId).append(" => ").append(amount).append(", ").append(operation).append("\n");
    }

    private static String extractAttributeKey(String entry) {
        String raw = extractBetween(entry, "ResourceKey[", "]");
        if ("unknown".equals(raw)) return "unknown";

        int slash = raw.indexOf('/');
        if (slash >= 0 && slash < raw.length() - 1) {
            String right = raw.substring(slash + 1).trim().replace(" ", "");
            if (isValidResourceLocation(right)) return right;
        }

        String direct = extractFirstValidResourceLocation(raw);
        if (direct != null) return direct;

        int g = raw.indexOf("generic.");
        if (g >= 0) return "minecraft:" + raw.substring(g);

        return raw;
    }

    private static String extractFirstValidResourceLocation(String raw) {
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (!Character.isLetter(c)) continue;

            int start = i;
            while (i < raw.length()) {
                char cc = raw.charAt(i);
                if (Character.isLetterOrDigit(cc) || cc == '_' || cc == ':' || cc == '/' || cc == '.' || cc == '-') {
                    i++;
                } else {
                    break;
                }
            }
            String candidate = raw.substring(start, i);
            int colon = candidate.indexOf(':');
            if (colon > 0 && colon < candidate.length() - 1) {
                if (isValidResourceLocation(candidate)) return candidate;
            }
        }
        return null;
    }

    private static boolean isValidResourceLocation(String s) {
        int idx = s.indexOf(':');
        if (idx <= 0 || idx == s.length() - 1) return false;
        String ns = s.substring(0, idx);
        String path = s.substring(idx + 1);
        return ns.matches("[a-z0-9_.-]+") && path.matches("[a-z0-9/_.-]+");
    }

    // ================= TAGS =================
    private static void dumpTags(ItemStack stack, StringBuilder sb) {
        sb.append("\n[TAGS]\n");
        try {
            Item item = stack.getItem();
            @SuppressWarnings("deprecation")
            Set<TagKey<Item>> tags = new HashSet<>(item.builtInRegistryHolder().tags().toList());
            if (tags.isEmpty()) {
                sb.append("tags=null\n");
                return;
            }
            for (TagKey<Item> tag : tags) {
                sb.append(tag.location()).append("\n");
            }
        } catch (Exception e) {
            sb.append("tags=error: ").append(e.getMessage()).append("\n");
        }
    }

    // ================= FLUID =================
    private static void dumpFluidInfo(ItemStack stack, StringBuilder sb) {
        sb.append("\n[FLUID CONTEXT]\n");
        if (stack.isEmpty()) {
            sb.append("empty stack\n");
            return;
        }

        IFluidHandler fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            sb.append("no capability\n");
            return;
        }

        sb.append("displayName=").append(stack.getHoverName().getString()).append("\n");
        sb.append("key=").append(stack.getDescriptionId()).append("\n");

        int tanks = fluidHandler.getTanks();
        sb.append("tanks=").append(tanks).append("\n");

        for (int i = 0; i < tanks; i++) {
            FluidStack fluid = fluidHandler.getFluidInTank(i);
            if (fluid.isEmpty()) {
                sb.append("tank ").append(i).append(": empty\n");
                continue;
            }

            Component name = fluid.getHoverName();
            String translationKey = fluid.getFluidHolder().value().getFluidType().getDescriptionId();
            String registry = fluid.getFluidHolder().unwrapKey().map(k -> k.location().toString()).orElse("<unknown>");

            sb.append("tank ").append(i).append("\n");
            sb.append("  displayName=").append(name.getString()).append("\n");
            sb.append("  registry=").append(registry).append("\n");
            sb.append("  key=").append(translationKey).append("\n");
        }
    }

    // ================= TOOLTIP =================
    private static void dumpTooltip(ItemStack stack, StringBuilder sb) {
        sb.append("\n[TOOLTIP]\n");
        try {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            TooltipContext context = TooltipContext.of(mc.level);
            TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
            List<Component> tooltip = stack.getTooltipLines(context, player, flag);

            if (tooltip.isEmpty()) {
                sb.append("empty\n");
                return;
            }
            for (Component c : tooltip) {
                sb.append(c.getString()).append("\n");
            }
        } catch (Exception e) {
            sb.append("tooltip=error: ").append(e.getMessage()).append("\n");
        }
    }

    // ================= UTILITIES =================
    private static List<String> splitTopLevel(String raw) {
        List<String> out = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '[' || c == '{' || c == '(') depth++;
            if (c == ']' || c == '}' || c == ')') depth = Math.max(0, depth - 1);
            if (c == ',' && depth == 0) {
                out.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (!current.isEmpty()) out.add(current.toString().trim());
        return out;
    }

    private static String extractBetween(String s, String start, String end) {
        int i = s.indexOf(start);
        if (i < 0) return "unknown";
        i += start.length();
        int j = s.indexOf(end, i);
        if (j < 0) return s.substring(i);
        return s.substring(i, j).trim();
    }


    private static String extractBracedBody(String raw, String marker) {
        int markerIdx = raw.indexOf(marker);
        if (markerIdx < 0) return null;

        int start = raw.indexOf("{", markerIdx);
        if (start < 0) return null;

        int depth = 0;
        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            if (depth == 0) {
                return raw.substring(start + 1, i);
            }
        }
        return null;
    }
}