package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightRewardsConfig;
import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import cn.blockforge.generated.createareofreight.infrastructure.config.RewardPoolFileConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// Run with JDK 21 and the project's compile + runtime classpaths, after compiling the mod.
class RewardPoolCheck {
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("--generate-general")) {
            var translations = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/goldentweaks/lang/zh_cn.json"))).getAsJsonObject();
            GTProfessionIntroductions.writeGeneral(Path.of(args[1]), translations);
            inspectIntroductions(Path.of(args[1]));
            return;
        }
        if (args.length == 2 && args[0].equals("--inspect-introductions")) {
            inspectIntroductions(Path.of(args[1]));
            return;
        }
        checkRewardSelection();
        require(GTBountifulRewardPools.equalPoolWeights(List.of("农产", "test"))
                .equals(List.of("农产|weight=1.0", "test|weight=1.0")), "Every pool weight is 1.0");
        var generalRows = GTBountifulRewardPools.generalRewardRows();
        require(generalRows.equals(List.of("questshop:coin=1.0", "goldentweaks:radiant_gold=0.4", "goldentweaks:brilliant_gold=0.2")),
                "Only the first three currencies are general rewards, weighted 1.0:0.4:0.2");
        for (String row : generalRows) require(FreightRewardsConfig.parseGeneralEntry(row) != null, "General row must parse upstream");
        byte[] large = new byte[3 * 1024 * 1024 + 17];
        new java.util.Random(42).nextBytes(large);
        var parts = new java.util.ArrayList<GTEditorTransfer.Part>();
        var transferId = GTEditorTransfer.send(large, parts::add);
        var pending = new GTEditorTransfer.Pending(transferId);
        byte[] assembled = null;
        for (var part : parts) {
            var encoded = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                GTEditorTransfer.Part.CODEC.encode(encoded, part);
                require(encoded.readableBytes() < 32767, "Fragments must fit the serverbound packet limit");
                assembled = pending.accept(GTEditorTransfer.Part.CODEC.decode(encoded));
                require((assembled != null) == part.last(), "Only process complete transfers");
            } finally { encoded.release(); }
        }
        require(java.util.Arrays.equals(large, assembled), "Large transfer must reassemble byte-for-byte");
        try {
            new GTEditorTransfer.Pending(transferId).accept(parts.get(1));
            throw new AssertionError("Out-of-order fragment accepted");
        } catch (IllegalArgumentException expected) { }
        for (int count : new int[]{0, 512, 513, 765, 5344, 12000}) {
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                buffer.writeVarInt(count);
                for (int i = 0; i < count; i++) buffer.writeUtf("test:minecraft:iron_ingot=" + i + ",1");
                buffer.writeVarInt(1);
                buffer.writeUtf("next list 中文");
                var entries = GTEditorLists.read(buffer);
                require(entries.size() == count, "Read every editor entry: " + count);
                if (count > 0) require(entries.getLast().equals("test:minecraft:iron_ingot=" + (count - 1) + ",1"), "Keep final entry");
                require(GTEditorLists.read(buffer).equals(List.of("next list 中文")) && !buffer.isReadable(),
                        "Keep subsequent snapshot fields aligned: " + count);
            } finally {
                buffer.release();
            }
        }
        for (int count : new int[]{-1, Integer.MAX_VALUE}) {
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                buffer.writeVarInt(count);
                try {
                    GTEditorLists.read(buffer);
                    throw new AssertionError("Accepted corrupt list count");
                } catch (io.netty.handler.codec.DecoderException expected) {
                    // Reject counts that cannot fit in the actual packet.
                }
            } finally {
                buffer.release();
            }
        }
        var mixedSpecial = List.of("test:minecraft:iron_ingot=2,4", "test:missing_mod:item=3,5", "bad format");
        var guarded = GTRewardItemGuard.filter(mixedSpecial, true, id -> id.toString().equals("minecraft:iron_ingot"));
        require(guarded.equals(List.of(mixedSpecial.get(0), "bad format")),
                "Missing items must not block valid editor entries; retain malformed rows for validation");
        require(mixedSpecial.size() == 3, "Do not mutate loaded configuration");
        var general = List.of("minecraft:iron_ingot=0.4", "missing_mod:item=0.2");
        require(GTRewardItemGuard.filter(general, false, id -> id.toString().equals("minecraft:iron_ingot"))
                .equals(List.of(general.getFirst())), "Guard general rewards as well as pool rewards");
        require(GTRewardItemGuard.filter(List.of("test:missing_mod:item=1,1"), true, id -> false).isEmpty(),
                "Do not invent replacements when all rewards are missing");
        JsonObject pool = JsonParser.parseString("""
                {"content": {
                  "iron": {"type":"item","content":"minecraft:iron_ingot","amount":{"min":2,"max":6},"weightMult":0.243},
                  "diamond": {"type":"item","content":"minecraft:diamond","amount":{"min":1,"max":1},"weightMult":0.001},
                  "disabled": {"type":"item","content":"minecraft:coal","amount":{"min":1,"max":1},"weightMult":0},
                  "missing": {"type":"item","content":"missing_mod:item","amount":{"min":1,"max":1},"weightMult":1},
                  "air": {"type":"item","content":"minecraft:air","amount":{"min":1,"max":1},"weightMult":1},
                  "other": {"type":"entity","content":"minecraft:zombie"}
                }}
                """).getAsJsonObject();
        List<String> rows = GTBountifulRewardPools.rewardRows(pool,
                item -> Set.of("minecraft:iron_ingot", "minecraft:diamond", "minecraft:coal").contains(item.toString()));
        require(rows.size() == 2, "Missing, disabled and non-item entries must be skipped");
        RewardEntry iron = FreightRewardsConfig.parsePoolEntry(rows.getFirst());
        require(iron != null && iron.chanceWeight() == 0.243 && iron.quantityWeight() == 4,
                "Preserve selection weight and use the amount midpoint");
        require(!iron.guaranteed() && !iron.hasFixedCount(), "Retain upstream order scaling and random draws");
        var parsed = new TomlParser().parse(RewardPoolFileConfig.serialize("goldentweaks_bountiful_test", false, rows));
        require(parsed.get("rewards").equals(rows), "Upstream TOML round trip");
        String numericSample = "    \"minecraft:iron_ingot=7.5E-4,12.0\",\n    \"minecraft:diamond=0.0015,2.5;preset=example:test\",\n";
        String normalized = GTBountifulRewardPools.normalizeRewardWeights(numericSample);
        require(normalized.contains("iron_ingot=75,12") && normalized.contains("diamond=150,2.5;preset=example:test"),
                "Scale all chance weights equally to integers, preserving quantities and suffixes");
        require(GTBountifulRewardPools.normalizeRewardWeights(normalized).equals(normalized), "Weight normalization is idempotent");
        String broken = RewardPoolFileConfig.serialize("goldentweaks:bountiful/test", false, rows);
        require(FreightRewardsConfig.parseSpecialEntry("goldentweaks:bountiful/test:" + rows.getFirst()) == null,
                "Reproduce the editor's colon separator failure");
        String migrated = GTBountifulRewardPools.migrateLegacyPoolId(broken, "test");
        var migratedConfig = new TomlParser().parse(migrated);
        require(migratedConfig.get("rewards").equals(rows), "Migration must preserve reward rows");
        String poolId = migratedConfig.get("id");
        require(poolId.equals("goldentweaks_bountiful_test"), "Use editor-compatible pool IDs");
        for (String row : rows) {
            var special = FreightRewardsConfig.parseSpecialEntry(poolId + ":" + row);
            require(special != null && special.getKey().equals(poolId)
                    && special.getValue().equals(FreightRewardsConfig.parsePoolEntry(row)),
                    "Flattened editor rewards must retain their real item IDs");
        }
        require(GTBountifulRewardPools.migrateLegacyPoolId(migrated, "test").equals(migrated), "Migration is idempotent");
        String custom = RewardPoolFileConfig.serialize("custom_pool", false, rows);
        require(GTBountifulRewardPools.migrateLegacyPoolId(custom, "test").equals(custom), "Keep custom pool IDs");
        pool.getAsJsonObject("content").getAsJsonObject("iron").addProperty("weightMult", -1);
        try {
            GTBountifulRewardPools.rewardRows(pool, item -> true);
            throw new AssertionError("Negative weight was accepted");
        } catch (IllegalArgumentException expected) {
            // Invalid data must not produce a partial pool file.
        }

        Path root = Path.of("src/main/resources/data/bountiful");
        Set<String> names = new TreeSet<>();
        try (var files = Files.list(root.resolve("bounty_decrees/goldentweaks"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                var decree = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                for (var reward : decree.getAsJsonArray("rewards")) {
                    names.add(reward.getAsString());
                }
            }
        }
        int entries = 0;
        for (String name : names) {
            var data = JsonParser.parseString(Files.readString(root.resolve("bounty_pools/goldentweaks/" + name + ".json"))).getAsJsonObject();
            var originalRows = GTBountifulRewardPools.rewardRows(data, item -> true);
            var normalizedPool = new TomlParser().parse(GTBountifulRewardPools.normalizeRewardWeights(
                    RewardPoolFileConfig.serialize("test", false, originalRows)));
            List<String> normalizedRows = normalizedPool.get("rewards");
            java.math.BigDecimal factor = null;
            for (int i = 0; i < originalRows.size(); i++) {
                String[] before = originalRows.get(i).split("=")[1].split(",");
                String[] after = normalizedRows.get(i).split("=")[1].split(",");
                require(after[0].matches("[0-9]+") && !after[1].matches(".*[eE].*"), "Plain integer chance weights: " + name);
                var oldWeight = new java.math.BigDecimal(before[0]);
                var newWeight = new java.math.BigDecimal(after[0]);
                if (factor == null) factor = newWeight.divide(oldWeight);
                require(oldWeight.multiply(factor).compareTo(newWeight) == 0, "Preserve exact relative weights: " + name);
                require(new java.math.BigDecimal(before[1]).compareTo(new java.math.BigDecimal(after[1])) == 0,
                        "Preserve quantity weights: " + name);
            }
            for (var element : data.getAsJsonObject("content").asMap().values()) {
                var entry = element.getAsJsonObject();
                var amount = entry.getAsJsonObject("amount");
                double min = amount.get("min").getAsDouble();
                double max = amount.get("max").getAsDouble();
                var reward = new RewardEntry(entry.get("content").getAsString(), entry.get("weightMult").getAsDouble(), (min + max) / 2);
                require(min >= 1 && max >= min, "Invalid amount: " + name);
                require(FreightRewardsConfig.parsePoolEntry(FreightRewardsConfig.serializePoolEntry(reward)).equals(reward),
                        "Reward round trip: " + name);
                entries++;
            }
        }
        require(names.size() == 36 && names.contains("golden_all_rews") && names.contains("pilot_sell"),
                "Expected profession rewards and common currency, without objective pools");
        var labels = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/bountiful/lang/zh_cn.json"))).getAsJsonObject();
        var modifiers = GTBountifulRewardPools.professionModifiers(List.copyOf(names), labels);
        var specialOrders = GTBountifulRewardPools.professionOrders(List.copyOf(names), labels);
        require(specialOrders.size() == 35, "One special order per profession");
        for (String row : specialOrders) {
            var parsedOrder = cn.blockforge.generated.createareofreight.FreightOrdersConfig.parseEntry(row);
            require(parsedOrder != null && parsedOrder.introductionLocked() && !parsedOrder.ticketRefreshDisabled()
                    && parsedOrder.introductionTemplate().equals(GTProfessionIntroductions.TEMPLATE)
                    && names.contains(parsedOrder.introductionContent())
                    && parsedOrder.rewardPool().equals("goldentweaks_bountiful_" + parsedOrder.introductionContent()),
                    "Special order must bind the matching reward pool and introduction");
        }
        require(modifiers.size() == 35, "One modifier per profession, excluding common currency");
        var lang = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/goldentweaks/lang/zh_cn.json"))).getAsJsonObject();
        for (String modifier : modifiers) {
            var parsedModifier = cn.blockforge.generated.createareofreight.ModifierOrderConfig.parseRewardPoolModifier(modifier);
            require(parsedModifier != null && parsedModifier.score() == 20, "Every profession modifier must grant +20 score");
            String stem = parsedModifier.rewardPoolFile().replace("goldentweaks-bountiful-", "").replace(".toml", "");
            require(names.contains(stem) && lang.has("tooltip.goldentweaks.aerofreight." + stem), "Profession pool and introduction must exist");
        }
        Path introductionRoot = Path.of("build/aerofreight-introduction-check");
        Files.createDirectories(introductionRoot);
        Files.writeString(introductionRoot.resolve("templates.toml"), "templates = []\n");
        var professionNames = names.stream().filter(name -> name.endsWith("_sell")).toList();
        GTProfessionIntroductions.write(introductionRoot, professionNames, labels, lang);
        var loader = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.class
                .getDeclaredMethod("prepareReload", Path.class);
        loader.setAccessible(true);
        var prepared = (cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.Prepared) loader.invoke(null, introductionRoot);
        cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.apply(prepared);
        Set<String> writers = new TreeSet<>();
        for (int seed = 0; seed < 200; seed++) {
            var ordinary = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                    "GENERAL-AUDIT", "normal", "农产", "d2_l3.nbt", 2, 2, 3, 1500, "AUDIT-DESTINATION", new java.util.Random(seed).nextLong());
            require(ordinary.templateId().equals(GTProfessionIntroductions.GENERAL_TEMPLATE), "Ordinary orders must only use Yuanli");
            writers.add(ordinary.commissionerName());
        }
        require(writers.equals(Set.of("硝酸根", "无人问津", "周小胖", "天临")), "All four Yuanli writers must be selectable");
        for (String profession : professionNames) {
            var intro = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                    "test", "normal", "词条货物", "d2_l3.nbt", 2, 2, 3, 1500, "测试配送站", 42L,
                    GTProfessionIntroductions.TEMPLATE, profession);
            require(intro.templateId().equals(GTProfessionIntroductions.TEMPLATE) && intro.body().contains("测试配送站")
                    && !intro.body().contains("${") && intro.title().contains(labels.get("bountiful.decree." + profession + ".name").getAsString()),
                    "Order detail introduction must resolve for " + profession);
            require(intro.factionName().equals(labels.get("bountiful.decree." + profession + ".name").getAsString())
                    && intro.commissionerName().matches("店员[1-4]"), "Profession-specific shop and staff");
            Set<String> staff = new TreeSet<>();
            for (int seed = 0; seed < 100; seed++) {
                var variant = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                        "test", "normal", "词条货物", "d2_l3.nbt", 2, 2, 3, 1500, "测试配送站", new java.util.Random(seed).nextLong(),
                        GTProfessionIntroductions.TEMPLATE, profession);
                require(variant.commissionerId().startsWith(profession + "_staff_"), "No staff from other professions");
                staff.add(variant.commissionerName());
            }
            require(staff.size() == 4, "All four placeholder staff must be selectable");
            for (int staffIndex = 1; staffIndex <= 4; staffIndex++) {
                String staffId = profession + "_staff_" + staffIndex;
                var personal = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                        "test", "normal", "词条货物", "d2_l3.nbt", 2, 2, 3, 1500, "测试配送站", 42,
                        GTProfessionIntroductions.TEMPLATE, staffId);
                require(personal.commissionerId().equals(staffId) && personal.factionId().equals(profession)
                        && personal.body().contains("测试配送站"), "Each personal text must bind its own staff and shop");
            }
        }
        Path authoredContent = introductionRoot.resolve(GTProfessionIntroductions.TEMPLATE).resolve("contents").resolve(professionNames.getFirst() + ".toml");
        String authored = Files.readString(authoredContent).replace("货运委托", "自定义委托正文");
        Files.writeString(authoredContent, authored);
        Path authoredFactions = introductionRoot.resolve(GTProfessionIntroductions.TEMPLATE).resolve("factions.toml");
        String customFactions = Files.readString(authoredFactions).replace("name=店员1", "name=自定义店员");
        Files.writeString(authoredFactions, customFactions);
        GTProfessionIntroductions.write(introductionRoot, professionNames, labels, lang);
        require(Files.readString(authoredContent).equals(authored), "Keep authored introductions on regeneration");
        require(Files.readString(authoredFactions).contains("name=自定义店员"), "Keep authored staff names on regeneration");
        Path personalFile = authoredContent.resolveSibling(professionNames.getFirst() + "_staff_1.toml");
        var personalConfig = new TomlParser().parse(Files.readString(personalFile));
        personalConfig.set("body", "店员一专属留言 ${destination}");
        Files.writeString(personalFile, new com.electronwill.nightconfig.toml.TomlWriter().writeToString(personalConfig));
        String personalText = Files.readString(personalFile);
        GTProfessionIntroductions.write(introductionRoot, professionNames, labels, lang);
        require(Files.readString(personalFile).equals(personalText), "Preserve authored personal text");
        prepared = (cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.Prepared) loader.invoke(null, introductionRoot);
        cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.apply(prepared);
        var personalIntro = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                "test", "normal", "词条货物", "d2_l3.nbt", 2, 2, 3, 1500, "测试配送站", 42,
                GTProfessionIntroductions.TEMPLATE, professionNames.getFirst() + "_staff_1");
        require(personalIntro.body().equals("店员一专属留言 测试配送站"), "Load independent authored staff prose");
        var legacy = new TomlParser().parse(Files.readString(authoredContent));
        legacy.set("commissioners", List.of("dispatcher"));
        legacy.set("body", "自行填写的旧正文 ${destination}");
        Files.writeString(authoredContent, new com.electronwill.nightconfig.toml.TomlWriter().writeToString(legacy));
        GTProfessionIntroductions.write(introductionRoot, professionNames, labels, lang);
        var migratedContent = new TomlParser().parse(Files.readString(authoredContent));
        require("自行填写的旧正文 ${destination}".equals(migratedContent.get("body"))
                && ((List<?>)migratedContent.get("commissioners")).size() == 4, "Migrate legacy dispatcher without losing authored text");
        System.out.println("PASS: " + names.size() + " pools, " + entries + " reward entries; conversion and TOML round trips");
        if (args.length == 2) {
            Files.writeString(Path.of(args[1]), GTBountifulRewardPools.normalizeRewardWeights(Files.readString(Path.of(args[0]))));
        }
    }

    private static void inspectIntroductions(Path root) throws Exception {
        var loader = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.class
                .getDeclaredMethod("prepareReload", Path.class);
        loader.setAccessible(true);
        var prepared = (cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.Prepared) loader.invoke(null, root);
        cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.apply(prepared);
        Set<String> ordinaryWriters = new TreeSet<>();
        for (int seed = 0; seed < 200; seed++) {
            var ordinary = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                    "GENERAL-AUDIT", "normal", "农产", "d2_l3.nbt", 2, 2, 3, 1500, "AUDIT-DESTINATION", new java.util.Random(seed).nextLong());
            require(ordinary.templateId().equals(GTProfessionIntroductions.GENERAL_TEMPLATE), "Production ordinary orders use Yuanli only");
            ordinaryWriters.add(ordinary.commissionerName());
        }
        require(ordinaryWriters.equals(Set.of("硝酸根", "无人问津", "周小胖", "天临")), "Four production writers resolve");
        Path contents = root.resolve(GTProfessionIntroductions.TEMPLATE).resolve("contents");
        int count = 0;
        int issues = 0;
        try (var files = Files.list(contents)) {
            for (Path file : files.filter(path -> path.getFileName().toString().contains("_staff_")
                    && path.getFileName().toString().endsWith(".toml")).sorted().toList()) {
                var config = new TomlParser().parse(Files.readString(file));
                String id = config.get("id");
                var intro = cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig.resolve(
                        "AUDIT-123", "normal", "audit", "d2_l3.nbt", 2, 2, 3, 1500, "AUDIT-DESTINATION", 42,
                        GTProfessionIntroductions.TEMPLATE, id);
                require(!intro.templateId().isBlank(), "Personal introduction must load: " + file);
                var callback = new org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<cn.blockforge.generated.createareofreight.OrderIntroduction>(
                        "audit", true, intro);
                var hook = java.util.Arrays.stream(net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight.OrderIntroductionConfigMixin.class.getDeclaredMethods())
                        .filter(method -> method.getName().equals("gt$personalText")).findFirst().orElseThrow();
                hook.setAccessible(true);
                hook.invoke(null, "AUDIT-123", "normal", "audit", "d2_l3.nbt", 2, 2, 3, 1500, "AUDIT-DESTINATION", 42L,
                        GTProfessionIntroductions.TEMPLATE, "guest_shop_sell", callback);
                require(callback.isCancelled() && callback.getReturnValue().body().equals(intro.body())
                        && callback.getReturnValue().signature().equals(intro.signature()), "Guest/ghostwriter personal text must not require matching shop");
                var bad = java.util.regex.Pattern.compile("(?<!\\$)\\{(?:distance|destination|cargo_size|order_id|commissioner|faction)\\}|\\$\\{[^}]+}")
                        .matcher(intro.title() + "\n" + intro.body() + "\n" + intro.signature());
                while (bad.find()) {
                    System.out.println("ISSUE " + file.getFileName() + ": " + bad.group());
                    issues++;
                }
                count++;
            }
        }
        System.out.println("INSPECTED " + count + " personal introductions; placeholder issues=" + issues);
    }

    private static void checkRewardSelection() throws Exception {
        var staffPacket = new org.objectweb.asm.tree.ClassNode();
        try (var input = dev.simulated_team.simulated.network.packets.physics_staff.PhysicsStaffActionPacket.class.getResourceAsStream("PhysicsStaffActionPacket.class")) {
            new org.objectweb.asm.ClassReader(input).accept(staffPacket, 0);
        }
        var staffHandler = staffPacket.methods.stream().filter(method -> method.name.equals("handle")).findFirst().orElseThrow();
        int handChecks = 0;
        boolean invalidDisconnect = false;
        for (var instruction : staffHandler.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.owner.equals("net/minecraft/world/item/ItemStack")
                    && call.name.equals("is") && call.desc.equals("(Lnet/minecraft/core/Holder;)Z")) handChecks++;
            if (instruction instanceof org.objectweb.asm.tree.LdcInsnNode constant && "Invalid packet".equals(constant.cst)) invalidDisconnect = true;
        }
        require(handChecks == 2 && invalidDisconnect, "Physics action must retain both hand validation sites and the observed disconnect cause");
        var tmrvClass = new org.objectweb.asm.ClassWriter(0);
        tmrvClass.visit(org.objectweb.asm.Opcodes.V21, org.objectweb.asm.Opcodes.ACC_PUBLIC,
                "dev/nolij/toomanyrecipeviewers/test/LockedManager", null, "java/lang/Object", null);
        var constructor = tmrvClass.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(org.objectweb.asm.Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(org.objectweb.asm.Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        tmrvClass.visitEnd();
        class TestLoader extends ClassLoader {
            Class<?> loadFixture(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
        }
        Object lockedManager = new TestLoader().loadFixture(tmrvClass.toByteArray()).getConstructor().newInstance();
        require(!GTRecipeViewerCompat.supportsRuntimeChanges(lockedManager), "Skip unsupported TMRV runtime recipe mutations");
        require(GTRecipeViewerCompat.supportsRuntimeChanges(new Object()), "Leave ordinary JEI runtime support intact");
        var pluginNode = new org.objectweb.asm.tree.ClassNode();
        try (var input = cn.blockforge.generated.createareofreight.jei.PackingMachineJeiPlugin.class.getResourceAsStream("PackingMachineJeiPlugin.class")) {
            new org.objectweb.asm.ClassReader(input).accept(pluginNode, 0);
        }
        var refresh = pluginNode.methods.stream().filter(method -> method.name.equals("refreshRecipes")).findFirst().orElseThrow();
        Set<String> mutations = new TreeSet<>();
        for (var instruction : refresh.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.owner.equals("mezz/jei/api/recipe/IRecipeManager")
                    && Set.of("addRecipes", "hideRecipes", "unhideRecipes").contains(call.name)) mutations.add(call.name + call.desc);
        }
        require(mutations.equals(Set.of("addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V",
                "hideRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/Collection;)V",
                "unhideRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/Collection;)V")), "All upstream mutation injection sites must match");
        var chinese = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/goldentweaks/lang/zh_cn.json"))).getAsJsonObject();
        var english = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/goldentweaks/lang/en_us.json"))).getAsJsonObject();
        for (String suffix : List.of("display_name", "staff_name", "title", "signature", "legacy_signature", "body",
                "content_comment", "staff_comment", "instructions", "staff_instructions")) {
            String key = "text.goldentweaks.aerofreight.introduction." + suffix;
            require(chinese.has(key) && english.has(key), "Introduction localization key present in both languages: " + key);
        }
        String generator = Files.readString(Path.of("src/main/java/net/fodoth/skina/goldentweaks/compat/createaerofreight/GTProfessionIntroductions.java"));
        require(!generator.contains("正在筹备") && !generator.contains("name=店员") && !generator.contains("# 此店员"),
                "Do not hardcode generated prose in Java");
        List<RewardEntry> pool = java.util.stream.IntStream.range(0, 30)
                .mapToObj(i -> new RewardEntry("test:item_" + i, 1.0, 1.0)).toList();
        int[] histogram = new int[19];
        for (long seed = 0; seed < 100000; seed++) {
            int count = GTRewardSelection.specialtyCount(seed);
            require(count >= 1 && count <= 18, "Specialty count must stay within 1..18");
            histogram[count]++;
        }
        int cumulative = 0;
        int median = 0;
        for (; median < histogram.length; median++) {
            cumulative += histogram[median];
            if (cumulative >= 50000) break;
        }
        require(median == 6 && histogram[18] > 0, "Median must be six and eighteen must be reachable");
        for (long seed = 0; seed < 100; seed++) {
            var selected = GTRewardSelection.choose(pool, seed);
            require(selected.equals(GTRewardSelection.choose(pool, seed)), "Preview/payout selection must use the same seed");
            require(selected.size() == GTRewardSelection.specialtyCount(seed)
                    && new java.util.HashSet<>(selected).size() == selected.size(), "Draw distinct entries within budget");
        }
        require(GTRewardSelection.choose(List.of(), 0).isEmpty(), "No emergency reward for an empty pool");
        var guaranteed = new RewardEntry("test:guaranteed", 1.0, 1.0, true, 0, "");
        require(GTRewardSelection.choose(List.of(guaranteed), 0).equals(List.of(guaranteed)), "Retain guaranteed rewards");
        require(GTRewardSelection.specialtiesFirst(List.of("coin", "gold", "gem", "special1", "special2"), 3)
                .equals(List.of("special1", "special2", "coin", "gold", "gem")), "Specialties precede general currencies");
        List<Integer> entries = java.util.stream.IntStream.range(0, 18).boxed().toList();
        for (int capacity : new int[]{0, 1, 4, 6, 10}) {
            Set<Integer> seen = new TreeSet<>();
            for (int page = 0; page < 18; page++) {
                var visible = GTRewardSelection.page(entries, capacity, page * 2000L);
                require(visible.size() <= Math.min(capacity, 6), "Respect width and six-icon limit");
                seen.addAll(visible);
            }
            require(capacity == 0 ? seen.isEmpty() : seen.size() == 18, "Rotation must eventually display every reward");
        }
        require(GTRewardSelection.page(entries, 6, 0).equals(entries.subList(0, 6)), "First page starts with specialties");
        require(GTRewardSelection.page(entries, 6, 1999).equals(entries.subList(0, 6)), "Keep pages stable for two seconds");
        require(GTRewardSelection.page(entries, 6, 6000).equals(entries.subList(0, 6)), "Rotation wraps");
        var mixin = net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight.FreightRewardsMixin.class;
        for (String name : List.of("gt$generalQuantity", "gt$fixedSpecialtyQuantity", "gt$specialtyQuantity")) {
            var method = mixin.getDeclaredMethod(name, RewardEntry.class);
            method.setAccessible(true);
            double scaled = (double)method.invoke(null, new RewardEntry("test:item", 7.0, 10.0));
            require(scaled == (name.equals("gt$generalQuantity") ? 40.0 : 3.0), "Scale quantity only: " + name);
        }
        var fullRewards = new java.util.ArrayList<Integer>(List.of(-1, -2, -3));
        fullRewards.addAll(entries);
        var ordered = GTRewardSelection.specialtiesFirst(fullRewards, 3);
        require(ordered.size() == 21 && ordered.subList(18, 21).equals(List.of(-1, -2, -3)),
                "Eighteen specialties plus all three general currencies");
        var node = new org.objectweb.asm.tree.ClassNode();
        try (var input = cn.blockforge.generated.createareofreight.FreightRewards.class.getResourceAsStream("FreightRewards.class")) {
            new org.objectweb.asm.ClassReader(input).accept(node, 0);
        }
        var amounts = node.methods.stream().filter(method -> method.name.equals("rewardAmounts")).findFirst().orElseThrow();
        int calls = 0;
        for (var instruction : amounts.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.name.equals("quantityWeight")) calls++;
        }
        require(calls == 3, "Upstream must retain general/fixed-specialty/random-specialty quantity call sites");
        System.out.println("PASS: reward count distribution (100000 seeds), deterministic draws, ordering and rotation");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
