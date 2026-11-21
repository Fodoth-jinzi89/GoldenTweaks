param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/emi-index-memory-check'
New-Item -ItemType Directory -Force $output | Out-Null
$fastutil = Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.5.12" -Recurse -Filter 'fastutil-8.5.12.jar' | Select-Object -First 1
$asm = foreach ($module in @('asm','asm-tree')) { Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.ow2.asm/$module" -Recurse -Filter "$module-9.8.jar" | Select-Object -First 1 -ExpandProperty FullName }
$emi = Get-ChildItem "$root/libs/implementation" -Filter 'emi-*.jar' | Select-Object -First 1
$cp = (@("$root/build/classes/java/main",$fastutil.FullName)+$asm)-join ';'
@'
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.fodoth.skina.goldentweaks.compat.emi.GTRecipeIndexes;
import net.fodoth.skina.goldentweaks.compat.emi.GTEmiSubstringIndex;
public class Check {
    public static void main(String[] args) throws Exception {
        Map<String,Object> source=new HashMap<>();Object value=new Object();
        for(int i=0;i<160221;i++)source.put("recipe:"+i,value);
        source.put(null,value);source.put("null",null);
        var compact=GTRecipeIndexes.compactIds(source);
        if(!compact.equals(source) || compact.size()!=source.size() || compact.get(new String("recipe:13"))!=value
                || compact.getOrDefault("missing",value)!=value || compact.getOrDefault("null",value)!=null)throw new AssertionError("ID map semantics");
        source.clear();if(compact.size()!=160223)throw new AssertionError("ID map isolation");
        compact.put("extra",value);compact.remove("extra");
        Map<String,List<Object>> lists=new HashMap<>();
        Object a=new String("same"),b=new String("same"),c=new Object();
        for(int n=0;n<4;n++){Object[] all={a,b,c};lists.put("size"+n,Arrays.stream(Arrays.copyOf(all,n)).toList());}
        var nullable=Arrays.stream(new Object[]{null}).toList();lists.put("nullable",nullable);
        var large=lists.get("size3");
        GTRecipeIndexes.compactSmallLists(lists);
        if(lists.get("size3")!=large || lists.get("nullable").getFirst()!=null || lists.get("size2").get(0)!=a || lists.get("size2").get(1)!=b)throw new AssertionError("List identity/order/null fallback");
        for(var list:lists.values())try{list.add(c);throw new AssertionError("List became mutable");}catch(UnsupportedOperationException expected){}
        if(lists.get("size1").contains(null) || lists.get("size1").indexOf(null)!=-1)throw new AssertionError("Null query semantics");
        GTRecipeIndexes.compactSmallLists(lists);
        Random random=new Random(7183);List<String> strings=new ArrayList<>();List<Integer> ids=new ArrayList<>();
        var builder=GTEmiSubstringIndex.builder();
        for(int i=0;i<1500;i++){
            StringBuilder text=new StringBuilder();int length=random.nextInt(30);
            String alphabet="abcXYZ中文\u0000\ud83d\ude00";
            for(int j=0;j<length;j++)text.append(alphabet.charAt(random.nextInt(alphabet.length())));
            String key=text.toString();int id=random.nextInt(300);strings.add(key);ids.add(id);builder.put(key,id);
        }
        var index=builder.build();
        for(int i=0;i<2000;i++){
            String token=i<1500?strings.get(i):"not-found";
            if(!token.isEmpty()){int start=random.nextInt(token.length());token=token.substring(start,Math.min(token.length(),start+1+random.nextInt(8)));}
            Set<Integer> expected=new HashSet<>(),actual=new HashSet<>();
            if(!token.isEmpty())for(int j=0;j<strings.size();j++)if(strings.get(j).contains(token))expected.add(ids.get(j));
            index.forEachMatch(token,actual::add);
            if(!actual.equals(expected))throw new AssertionError("Substring differential mismatch");
        }
        var repeated=GTEmiSubstringIndex.builder();repeated.put("abcdefxyz",3);var shared=repeated.build();
        Field grams=GTEmiSubstringIndex.class.getDeclaredField("grams");grams.setAccessible(true);
        Map<?,?> postings=(Map<?,?>)grams.get(shared);
        Object first=postings.values().iterator().next();
        for(Object posting:postings.values())if(posting!=first)throw new AssertionError("Equal postings not shared");
        repeated.put("another",4);repeated.build();
        Set<Integer> oldResult=new HashSet<>();shared.forEachMatch("another",oldResult::add);
        if(!oldResult.isEmpty())throw new AssertionError("Builder modified published snapshot");
        try(JarFile jar=new JarFile(args[0])){
            ClassNode manager=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("dev/emi/emi/registry/EmiRecipes$Manager.class"))).accept(manager,0);
            for(String field:List.of("byId","byInput","byOutput"))if(manager.fields.stream().noneMatch(f->f.name.equals(field) && f.desc.equals("Ljava/util/Map;")))throw new AssertionError("Manager field changed");
            var constructor=manager.methods.stream().filter(m->m.name.equals("<init>") && m.desc.equals("(Ljava/util/List;Ljava/util/Map;Ljava/util/List;Z)V")).findFirst().orElseThrow();
            int snapshots=0;for(AbstractInsnNode insn:constructor.instructions)if(insn instanceof MethodInsnNode call && call.owner.equals("java/util/stream/Stream") && call.name.equals("toList"))snapshots++;
            if(snapshots<2)throw new AssertionError("Immutable index construction changed");
        }
        System.out.println("PASS: 160221 ID mappings, equals/null/missing/mutation/isolation; immutable small-list identity/order/null fallback; 2000 randomized Unicode substring comparisons; posting sharing/snapshot isolation; actual EMI fields/constructor/snapshot sites");
    }
}
'@ | Set-Content "$output/Check.java" -Encoding utf8
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check $emi.FullName
if ($LASTEXITCODE -ne 0) { throw 'EMI index check failed' }
