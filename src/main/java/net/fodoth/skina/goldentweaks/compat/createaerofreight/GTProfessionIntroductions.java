package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GTProfessionIntroductions {
    public static final String TEMPLATE = "goldentweaks_professions";
    public static final String GENERAL_TEMPLATE = "goldentweaks_yuanli";

    private GTProfessionIntroductions() {
    }

    public static void generate(List<String> professions, JsonObject names) throws IOException {
        try (var stream = GTProfessionIntroductions.class.getResourceAsStream("/assets/goldentweaks/lang/zh_cn.json")) {
            if (stream == null) throw new IOException("Missing profession introduction text");
            JsonObject descriptions = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            write(OrderIntroductionConfig.directory(), professions, names, descriptions);
            OrderIntroductionConfig.apply(OrderIntroductionConfig.prepareReload());
        }
    }

    static void write(Path root, List<String> professions, JsonObject names, JsonObject descriptions) throws IOException {
        Path folder = root.resolve(TEMPLATE);
        Files.createDirectories(folder.resolve("contents"));
        writeMissing(folder.resolve("填写说明.txt"), text(descriptions, "instructions"));
        writeMissing(folder.resolve("template.toml"), "id = \"" + TEMPLATE
                + "\"\ndisplayName = " + quote(text(descriptions, "display_name")) + "\nlayout = \"faction_contract\"\n");
        StringBuilder variables = new StringBuilder();
        OrderIntroductionConfig.defaultVariableValues().forEach((key, value) -> variables.append(key).append(" = ").append(quote(value)).append('\n'));
        writeMissing(folder.resolve("variables.toml"), variables.toString());
        Path factionFile = folder.resolve("factions.toml");
        var factions = Files.exists(factionFile) ? new TomlParser().parse(Files.readString(factionFile))
                : new TomlParser().parse("factions = []\ncommissioners = []\n");
        List<String> factionRows = new ArrayList<>(factions.get("factions"));
        List<String> commissionerRows = new ArrayList<>(factions.get("commissioners"));
        List<String> rules = new ArrayList<>();
        for (String profession : professions) {
            String key = "bountiful.decree." + profession + ".name";
            String label = names.has(key) ? names.get(key).getAsString() : profession;
            List<String> staff = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String id = profession + "_staff_" + i;
                staff.add(id);
                if (commissionerRows.stream().noneMatch(row -> row.startsWith("id=" + id + "|"))) {
                    commissionerRows.add("id=" + id + "|name=" + text(descriptions, "staff_name", i) + "|weight=1.0|avatar=initial");
                }
            }
            if (factionRows.stream().noneMatch(row -> row.startsWith("id=" + profession + "|"))) {
                factionRows.add("id=" + profession + "|name=" + label + "|weight=1.0|commissioners=" + String.join(",", staff));
            }
            String description = descriptions.get("tooltip.goldentweaks.aerofreight." + profession).getAsString();
            String body = text(descriptions, "body", label, description);
            Path contentFile = folder.resolve("contents").resolve(profession + ".toml");
            String staffList = "[" + String.join(", ", staff.stream().map(GTProfessionIntroductions::quote).toList()) + "]";
            if (Files.exists(contentFile)) {
                var content = new TomlParser().parse(Files.readString(contentFile));
                // Migrate only the old generated dispatcher binding, preserving authored prose.
                if (List.of("dispatcher").equals(content.get("commissioners"))) {
                    content.set("commissioners", staff);
                    if (text(descriptions, "legacy_signature", label).equals(content.get("signature"))) {
                        content.set("signature", text(descriptions, "signature"));
                    }
                    Files.writeString(contentFile, new TomlWriter().writeToString(content), StandardCharsets.UTF_8);
                }
            } else {
                Files.writeString(contentFile, text(descriptions, "content_comment")
                        + "id = \"" + profession + "\"\ntitle = " + quote(text(descriptions, "title", label))
                        + "\nbody = " + quote(body)
                        + "\nsignature = " + quote(text(descriptions, "signature"))
                        + "\ncommissioners = " + staffList + "\n", StandardCharsets.UTF_8);
            }
            // Seed each personal template from the existing profession text, then preserve edits.
            for (String staffId : staff) {
                Path personalFile = folder.resolve("contents").resolve(staffId + ".toml");
                if (Files.exists(personalFile)) continue;
                var personal = new TomlParser().parse(Files.readString(contentFile));
                personal.set("id", staffId);
                personal.set("commissioners", List.of(staffId));
                Files.writeString(personalFile, text(descriptions, "staff_comment")
                        + new TomlWriter().writeToString(personal), StandardCharsets.UTF_8);
            }
            rules.add("content=" + profession + "|cargoTypes=goldentweaks_bountiful_" + profession + "|weight=1.0");
        }
        factions.set("factions", factionRows);
        factions.set("commissioners", commissionerRows);
        Files.writeString(factionFile, new TomlWriter().writeToString(factions), StandardCharsets.UTF_8);
        Files.writeString(folder.resolve("店员文案说明.txt"), text(descriptions, "staff_instructions"), StandardCharsets.UTF_8);
        Files.writeString(folder.resolve("rules.toml"), "rules = [\n" + String.join(",\n", rules.stream().map(GTProfessionIntroductions::quote).toList()) + "\n]\n", StandardCharsets.UTF_8);
        Path index = root.resolve("templates.toml");
        var config = new TomlParser().parse(Files.readString(index));
        List<String> templates = new ArrayList<>(config.get("templates"));
        templates.removeIf(row -> row.startsWith("template=" + TEMPLATE + "|"));
        // Only selected explicitly for profession orders; does not enter ordinary cargo's random pool.
        templates.add("template=" + TEMPLATE + "|folder=" + TEMPLATE + "|cargoTypes=goldentweaks_profession_only|weight=1.0|enabled=true");
        config.set("templates", templates);
        Files.writeString(index, new TomlWriter().writeToString(config), StandardCharsets.UTF_8);
        writeGeneral(root, descriptions);
    }

    static void writeGeneral(Path root, JsonObject translations) throws IOException {
        Path folder = root.resolve(GENERAL_TEMPLATE);
        Files.createDirectories(folder.resolve("contents"));
        writeMissing(folder.resolve("template.toml"), "id = " + quote(GENERAL_TEMPLATE)
                + "\ndisplayName = " + quote(text(translations, "general_name")) + "\nlayout = \"faction_contract\"\n");
        StringBuilder variables = new StringBuilder();
        OrderIntroductionConfig.defaultVariableValues().forEach((key, value) -> variables.append(key).append(" = ").append(quote(value)).append('\n'));
        writeMissing(folder.resolve("variables.toml"), variables.toString());
        List<String> people = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<String> rules = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String id = "writer_" + i;
            ids.add(id);
            people.add("id=" + id + "|name=" + text(translations, "general_writer_" + i) + "|weight=1.0|avatar=initial");
            writeMissing(folder.resolve("contents").resolve(id + ".toml"), text(translations, "staff_comment")
                    + "id = " + quote(id) + "\ntitle = " + quote(text(translations, "general_title"))
                    + "\nbody = " + quote(text(translations, "general_body_" + i))
                    + "\nsignature = " + quote(text(translations, "signature"))
                    + "\ncommissioners = [" + quote(id) + "]\n");
            rules.add("content=" + id + "|weight=1.0");
        }
        writeMissing(folder.resolve("factions.toml"), "factions = ["
                + quote("id=yuanli|name=" + text(translations, "general_name") + "|weight=1.0|commissioners=" + String.join(",", ids))
                + "]\ncommissioners = [" + String.join(",", people.stream().map(GTProfessionIntroductions::quote).toList()) + "]\n");
        writeMissing(folder.resolve("rules.toml"), "rules = [" + String.join(",", rules.stream().map(GTProfessionIntroductions::quote).toList()) + "]\n");
        Path index = root.resolve("templates.toml");
        String previous = Files.readString(index);
        var config = new TomlParser().parse(previous);
        List<String> entries = new ArrayList<>();
        entries.add("template=" + GENERAL_TEMPLATE + "|folder=" + GENERAL_TEMPLATE + "|orderTypes=*|cargoTypes=*|cargoTemplates=*|weight=1.0|enabled=true");
        List<String> existing = config.get("templates");
        existing.stream().filter(row -> row.startsWith("template=" + TEMPLATE + "|")).forEach(entries::add);
        if (!entries.equals(existing)) {
            writeMissing(root.resolve("templates.toml.before-yuanli.bak"), previous);
            config.set("templates", entries);
            Files.writeString(index, new TomlWriter().writeToString(config), StandardCharsets.UTF_8);
        }
    }

    private static void writeMissing(Path file, String text) throws IOException {
        if (!Files.exists(file)) Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    private static String text(JsonObject translations, String key, Object... arguments) {
        return String.format(Locale.ROOT, translations.get("text.goldentweaks.aerofreight.introduction." + key).getAsString(), arguments);
    }

    private static @NotNull String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}
