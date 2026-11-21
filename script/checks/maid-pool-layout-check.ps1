param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/maid-pool-layout-check'
New-Item -ItemType Directory -Force $output | Out-Null
$fastutil = Get-ChildItem -LiteralPath "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.5.12" -Recurse -Filter 'fastutil-8.5.12.jar' | Select-Object -First 1
@'
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
public class LayoutCheck {
    static Instrumentation instrumentation;
    public static void premain(String args, Instrumentation value) { instrumentation=value; }
    static long storage(Map<Object,Object> map) throws Exception {
        long bytes=instrumentation.getObjectSize(map);
        if(map instanceof HashMap<?,?>) {
            Field field=HashMap.class.getDeclaredField("table");field.setAccessible(true);
            Object[] table=(Object[])field.get(map);
            if(table!=null){bytes+=instrumentation.getObjectSize(table);
                for(Object node:table)while(node!=null){bytes+=instrumentation.getObjectSize(node);Field next=node.getClass().getDeclaredField("next");next.setAccessible(true);node=next.get(node);}}
        } else {
            for(String name:new String[]{"key","value"}){Field field=Object2ObjectOpenHashMap.class.getDeclaredField(name);field.setAccessible(true);bytes+=instrumentation.getObjectSize(field.get(map));}
        }
        return bytes;
    }
    public static void main(String[] args) throws Exception {
        long total=0;
        for(int count:new int[]{12836,18382,7927,2575,32768,16384,8192}) {
            Map<Object,Object> before=new HashMap<>(),after=new Object2ObjectOpenHashMap<>();
            Object value=new Object();
            for(int i=0;i<count;i++){Object key=new Object();before.put(key,value);after.put(key,value);}
            for(var entry:before.entrySet())if(after.get(entry.getKey())!=entry.getValue())throw new AssertionError("Mapping changed");
            long oldBytes=storage(before),newBytes=storage(after);
            if(newBytes>=oldBytes)throw new AssertionError("No storage saving");
            System.out.printf("entries=%d HashMap=%d fastutil=%d saved=%d bytes%n",count,oldBytes,newBytes,oldBytes-newBytes);
            if(count==12836 || count==18382 || count==7927 || count==2575)total+=oldBytes-newBytes;
        }
        System.out.println("Representative four-pool storage saving: "+total+" bytes (excludes shared keys/values)");
    }
}
'@ | Set-Content (Join-Path $output 'LayoutCheck.java') -Encoding utf8
& (Join-Path $Jdk 'bin/javac.exe') -cp $fastutil.FullName -d $output (Join-Path $output 'LayoutCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Layout check compilation failed' }
"Premain-Class: LayoutCheck`n" | Set-Content (Join-Path $output 'MANIFEST.MF') -Encoding ascii
& (Join-Path $Jdk 'bin/jar.exe') cfm (Join-Path $output 'agent.jar') (Join-Path $output 'MANIFEST.MF') -C $output LayoutCheck.class
if ($LASTEXITCODE -ne 0) { throw 'Layout check agent creation failed' }
foreach ($compressed in @('+', '-')) {
    & (Join-Path $Jdk 'bin/java.exe') "-XX:${compressed}UseCompressedOops" "-XX:${compressed}UseCompressedClassPointers" --add-opens=java.base/java.util=ALL-UNNAMED "-javaagent:$output/agent.jar" -cp "$output;$($fastutil.FullName)" LayoutCheck
    if ($LASTEXITCODE -ne 0) { throw 'Layout check failed' }
}
