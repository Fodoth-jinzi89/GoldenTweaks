param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/ysm-linear-frames-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
$jar=Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter '*yes_steve*' | Select-Object -First 1
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp "$cp;$($jar.FullName)" -d $output "$PSScriptRoot/CheckYsmFrames.java"
if($LASTEXITCODE){throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp;$($jar.FullName)" CheckYsmFrames $jar.FullName "$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/fix/yesstevemodel/KeyFrameFactoryMixin.class"
if($LASTEXITCODE){throw 'YSM constant check failed'}


