param([string]$Jdk='E:/MCreator/MCreator20253/jdk')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$iris=Get-ChildItem "$root/libs/implementation" -Filter 'iris-*.jar' | Select-Object -First 1
$upstream=(& "$Jdk/bin/javap.exe" -c -p -classpath $iris.FullName net.irisshaders.iris.compat.sodium.config.IrisConfig) -join "`n"
if($LASTEXITCODE){throw 'Cannot inspect Iris'}
$register=($upstream -split 'private static void lambda\$registerConfigLate\$11')[0]
if(([regex]::Matches($register,'Component.literal:')).Count -ne 3){throw 'Iris literal call sites changed'}
foreach($label in @('Settings','Packs','None')) {
    if($register -notmatch "String $label\r?\n\s*\d+: invokestatic[^\n]+Component.literal:"){throw "Missing literal $label"}
}
$hook=(& "$Jdk/bin/javap.exe" -c -p -classpath "$root/build/classes/java/main" net.fodoth.skina.goldentweaks.mixin.fix.iris.IrisVideoSettingsTranslationMixin) -join "`n"
if($LASTEXITCODE){throw 'Build the translation mixin first'}
foreach($text in @('Settings','Packs','None','options.iris.settings','options.iris.shaderPackList','options.off','Component.translatable:','Component.literal:')) {
    if(-not $hook.Contains($text)){throw "Missing compiled branch: $text"}
}
$zh=Get-Content "$root/src/main/resources/assets/iris/lang/zh_cn.json" -Raw | ConvertFrom-Json
$en=Get-Content "$root/src/main/resources/assets/iris/lang/en_us.json" -Raw | ConvertFrom-Json
if($zh.'options.iris.settings' -ne '设置' -or $en.'options.iris.settings' -ne 'Settings'){throw 'Missing page translations'}
$mixins=Get-Content "$root/src/main/resources/goldentweaks.mixins.json" -Raw | ConvertFrom-Json
if($mixins.client -notcontains 'fix.iris.IrisVideoSettingsTranslationMixin'){throw 'Mixin must be client-only'}
Write-Host 'PASS: upstream Settings/Packs/None call sites, compiled translation branches, language keys and client registration'
