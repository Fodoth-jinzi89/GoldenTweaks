param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21',
      [string]$Mods = 'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/argo-loot-check'
New-Item -ItemType Directory -Force $output | Out-Null
$argo = Get-ChildItem (Join-Path $root 'libs/compileOnly') -Filter '*argo*.jar' | Select-Object -First 1
$gson = Get-ChildItem 'C:\Users\peiranyu\.mcreator\gradle\caches\modules-2\files-2.1\com.google.code.gson\gson' -Recurse -Filter 'gson-2.10.1.jar' | Select-Object -First 1
$cp = @((Join-Path $root 'build/classes/java/main'),$argo.FullName,$gson.FullName) -join ';'
@'
import com.google.gson.*;
import com.flavor_immersed_daily_argo.economy.product.LootProductEntries;
import net.fodoth.skina.goldentweaks.compat.flavorimmerseddailyargo.GTLootDiscoveryData;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
public class Check {
    static void compare(Map<String,JsonElement> original) {
        String before=original.toString();
        var compact=GTLootDiscoveryData.compact(original);
        for(String key:original.keySet()) {
            var a=LootProductEntries.find(original,key);var b=LootProductEntries.find(compact,key);
            if(!a.equals(b))throw new AssertionError("Discovery changed for "+key+": "+a+" vs "+b);
        }
        if(!before.equals(original.toString()))throw new AssertionError("Original data mutated");
    }
    public static void main(String[] args) throws Exception {
        Map<String,JsonElement> fixtures=new LinkedHashMap<>();
        fixtures.put("test:root",JsonParser.parseString("""
          {"pools":[{"conditions":[{"type":"item","name":"test:ignored"}],"entries":[
            {"type":"item","name":"apple","functions":[{"type":"item","name":"test:ignored"}]},
            {"type":"tag","name":"c:foods"},{"type":"loot_table","value":"test:cycle"},
            {"type":"loot_table","value":{"pools":[{"entries":[{"type":"item","name":"test:inline"}]}]}},
            {"type":"alternatives","children":[{"type":"item","name":"test:child"}]}]}]}
          """));
        fixtures.put("test:cycle",JsonParser.parseString("{\"type\":\"loot_table\",\"name\":\"test:root\"}"));
        fixtures.put("test:invalid",JsonParser.parseString("{\"type\":1,\"name\":{},\"value\":[],\"children\":[null,3]}"));
        for(int i=0;i<36;i++)fixtures.put("test:chain"+i,JsonParser.parseString(i==35?"{\"type\":\"item\",\"name\":\"test:too_deep\"}":"{\"type\":\"loot_table\",\"value\":\"test:chain"+(i+1)+"\"}"));
        JsonElement deep=JsonParser.parseString("{\"type\":\"item\",\"name\":\"test:deep\"}");
        for(int i=0;i<70;i++){JsonObject parent=new JsonObject();parent.add("children",deep);deep=parent;}
        fixtures.put("test:deep",deep);compare(fixtures);
        Map<String,JsonElement> tables=new HashMap<>();
        try(var files=Files.list(Path.of(args[0]))) {
            for(Path file:files.filter(p->p.toString().endsWith(".jar")).sorted().toList()) {
                try(ZipFile zip=new ZipFile(file.toFile())) {
                    for(var entry:Collections.list(zip.entries())) {
                        String n=entry.getName();String[] parts=n.split("/",4);
                        if(parts.length==4 && parts[0].equals("data") && parts[2].equals("loot_table") && n.endsWith(".json")) {
                            try(var reader=new java.io.InputStreamReader(zip.getInputStream(entry),java.nio.charset.StandardCharsets.UTF_8)) {
                                tables.put(parts[1]+":"+parts[3].substring(0,parts[3].length()-5),JsonParser.parseReader(reader));
                            }
                        }
                    }
                }
            }
        }
        compare(tables);
        var compact=GTLootDiscoveryData.compact(tables);
        long originalChars=0,compactChars=0;
        for(var v:tables.values())originalChars+=v.toString().length();
        for(var v:compact.values())compactChars+=v.toString().length();
        System.out.println("PASS: actual upstream find() results match for "+tables.size()+" mod loot tables; cycles/inline/tag/depth/invalid fixtures and source isolation passed");
        System.out.println("JSON characters: "+originalChars+" -> "+compactChars+" (not a heap-size measurement)");
    }
}
'@ | Set-Content (Join-Path $output 'Check.java') -Encoding utf8
& (Join-Path $Jdk 'bin/javac.exe') -proc:none -encoding UTF-8 -cp $cp -d $output (Join-Path $output 'Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xmx2g -Xverify:all -cp "$output;$cp" Check $Mods
if ($LASTEXITCODE -ne 0) { throw 'Loot projection changed discovery results' }
