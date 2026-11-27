<#
.SYNOPSIS
    Generates GoldenTweaks item / entity aspect JSON for the items of one mod namespace.

.DESCRIPTION
    Enumerates the ids a mod jar exposes (assets/<ns>/lang/en_us.json item./block. keys,
    including META-INF/jarjar nested jars), skips the ids that already have an aspect file and
    writes a definition for the rest, picking aspects from a category rule set
    (raw / cooked food, spawn eggs, dead animals, tools, cloth, machines, storage cells, ...).

    The rule set is a starting point: values are grouped per category, so tweak the generated
    files (or the rules below) when a specific item deserves different aspects.

    Storage-cell / machine rules are currently specialised for neoecoae and ae2lt; other
    namespaces fall back to the generic food/tool/block rules.

.EXAMPLE
    ./script/aspect/gen-aspects.ps1 -Jar "D:\packs\myinstance\mods\flavor_immersed_daily_argo-1.0.0.jar" `
        -Namespace flavor_immersed_daily_argo -Kind both

.EXAMPLE
    ./script/aspect/gen-aspects.ps1 -Jar "...\ae2lt-2.1.2-beta3-neoforge-1.21.1.jar" `
        -Namespace ae2lt -Kind items -Apply
#>
param(
    [Parameter(Mandatory = $true)][string]$Jar,
    [Parameter(Mandatory = $true)][string]$Namespace,
    [string]$AspectRoot = (Join-Path $PSScriptRoot "..\..\src\main\resources\data\goldentweaks\recipe\thaumcraft"),
    [ValidateSet("items", "entities", "both")][string]$Kind = "items",
    [string]$CoveredPath = "",
    [switch]$Apply
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$script:Ns = $Namespace

# namespace dependent fallback for ids that no rule matches
$script:ThaumFamily = '^(thaumcraft|thaumcraftcelestial|thaumichorizons|thaumicbases|thaumic_tinkerer|thaumicenergistics|thaumicindustry|thaumickaleidoscope|thaumicconcilium|forbiddenmagic|taintedmagic|mystery_dynamics|gadomancy)$'
if ($Namespace -eq 'enviromats') {
    $script:Fallback = @{ saxum = 2; terra = 1; ordo = 1 }
} elseif ($Namespace -match '^(ultramarine|amendments|theatrical|dndecor)$') {
    $script:Fallback = @{ fabrico = 2; arbor = 1; ordo = 1 }
} elseif ($Namespace -match $script:ThaumFamily) {
    $script:Fallback = @{ praecantatio = 3; cognitio = 1 }
} elseif ($Namespace -match '^(create|offroad|pipez|shaolib|mespliced|neoecoae|ae2lt|thermal|cofh|thermalendergy|rftoolsbase|mio_icif|envirotech|envirocore|valkyrielib|avaritia_more_items|avaritia_integration|create_aerofreight|evolvedmekanism|mekanism|createmoderntech|cbcmoreshells|create_connected|create_dragons_plus|createdieselgenerators|alltheores|silentgems|etlunar|goldentweaks|structure_layout_optimizer|bandwidthoptimizer|aeallpattern|scex_cryotheum_cell|sfm|retrofactorymanager|allthecompressed|oneenoughitem)') {
    $script:Fallback = @{ machina = 2; metallum = 2; fabrico = 1 }
} elseif ($Namespace -match 'maid|animation|touhou') {
    $script:Fallback = @{ pannus = 2; corpus = 1 }
} else {
    $script:Fallback = @{ fames = 1; victus = 1; fabrico = 1 }
}

function Get-NsIds([string]$jarPath, [string]$ns, [string]$what) {
    $set = New-Object 'System.Collections.Generic.HashSet[string]'
    $zip = [IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
        $archives = New-Object System.Collections.Generic.List[IO.Compression.ZipArchive]
        $streams = New-Object System.Collections.Generic.List[IO.MemoryStream]
        $archives.Add($zip)
        foreach ($e in $zip.Entries) {
            if ($e.FullName -notlike 'META-INF/jarjar/*.jar') { continue }
            $ms = New-Object IO.MemoryStream
            try {
                $e.Open().CopyTo($ms)
                $ms.Position = 0
                $archives.Add((New-Object IO.Compression.ZipArchive($ms, [IO.Compression.ZipArchiveMode]::Read)))
                $streams.Add($ms)
            } catch { $ms.Dispose() }
        }
        try {
            # real ids: lang keys drop the block state suffix (block.<ns>.producer.machine -> producer/machine)
            # so keep a lang derived id only when the plain form or its slash form matches a model path
            $modelPaths = New-Object 'System.Collections.Generic.HashSet[string]'
            foreach ($a in $archives) {
                foreach ($e in $a.Entries) {
                    if ($e.FullName -match "^assets/([^/]+)/(?:models/(?:item|block)|blockstates)/(.+)\.json$") {
                        if ($Matches[1] -eq $ns) { [void]$modelPaths.Add($Matches[2]) }
                    }
                }
            }
            foreach ($a in $archives) {
                foreach ($e in $a.Entries) {
                    if ($e.FullName -notmatch "^assets/([^/]+)/lang/en_us\.json$") { continue }
                    if ($Matches[1] -ne $ns) { continue }
                    $rd = New-Object IO.StreamReader($e.Open())
                    $t = $rd.ReadToEnd()
                    $rd.Close()
                    if ($what -eq 'items') {
                        foreach ($m in [regex]::Matches($t, '"(item|block)\.([^"]+)"\s*:')) {
                            $rest = $m.Groups[2].Value
                            $dot = $rest.IndexOf('.')
                            if ($dot -lt 0) { [void]$set.Add("$ns`:$rest"); continue }
                            $head = $rest.Substring(0, $dot)
                            $tail = $rest.Substring($dot + 1)
                            if ($head -eq $ns) {
                                if ($tail -notmatch '\.') { [void]$set.Add("$ns`:$tail"); continue }
                                $slashed = $tail -replace '\.', '/'
                                if ($modelPaths.Contains($slashed)) { [void]$set.Add("$ns`:$slashed") }
                            } elseif ($head -cmatch '^[a-z0-9_]+$' -and $tail -notmatch '[\.:]' -and $tail -notmatch '^(name|desc|description|tooltip|shift|hint|info|text|working|status|message|jei|jei_desc)$') {
                                # assets/<ns>/lang describing another namespace (thermal_foundation -> thermal:apatite_ore)
                                [void]$set.Add("$head`:$tail")
                            }
                        }
                    } else {
                        foreach ($m in [regex]::Matches($t, '"entity\.([^"]+)"\s*:')) {
                            $rest = $m.Groups[1].Value
                            $dot = $rest.IndexOf('.')
                            if ($dot -lt 0) { [void]$set.Add("$ns`:$rest"); continue }
                            $head = $rest.Substring(0, $dot)
                            $tail = $rest.Substring($dot + 1)
                            if ($head -eq $ns) { [void]$set.Add("$ns`:$tail") } else { [void]$set.Add("$ns`:$rest") }
                        }
                    }
                }
            }
        } finally {
            for ($i = 1; $i -lt $archives.Count; $i++) { $archives[$i].Dispose() }
            foreach ($s in $streams) { $s.Dispose() }
        }
    } finally { $zip.Dispose() }

    # drop lang sub-keys (item.foo.tooltip / .working / .stage ...) - real ids never contain a dot
    $filtered = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($id in $set) {
        if ((($id -split ':', 2)[1]) -notmatch '\.') { [void]$filtered.Add($id) }
    }
    return $filtered
}

$covered = New-Object 'System.Collections.Generic.HashSet[string]'
if ($CoveredPath -and (Test-Path -LiteralPath $CoveredPath)) {
    # one id per line, produced by gen-missing-aspects.ps1 (avoids rescanning every aspect file)
    foreach ($line in [IO.File]::ReadLines($CoveredPath)) {
        $id = $line.Trim()
        if ($id) { [void]$covered.Add($id) }
    }
} else {
    foreach ($f in Get-ChildItem -LiteralPath $AspectRoot -Recurse -Filter *.json -File | Where-Object { $_.FullName -match '\\aspects\\|\\entity_aspects\\' }) {
        $t = Get-Content -LiteralPath $f.FullName -Raw -Encoding UTF8
        $m = [regex]::Match($t, '"id"\s*:\s*"([^"]+)"')
        if ($m.Success) { [void]$covered.Add($m.Groups[1].Value) }
    }
}

# ---------- rules ----------
$cookedPrefixes = '^(roast|roasted|braised|boiled|fried|steamed|cooked|stir_fried|spicy|scalded|peking|pickled|stinky|salt_pepper|sugar_roasted|vinegar|scallion|garlic|oil_soaked|typhoon|mackerel_pancake|razor_clam|duck_blood|turtle_pigeon|fish_maw|clam_chive|eel_rice|spoon_worm|pot_stew|pigeon_soup|shrimp|tuna_sushi|chestnut_cake|chestnut_dough)_'

function Get-ItemAspects([string]$path) {
    if ($script:Ns -eq 'ae2lt') {
        if ($path -match 'pigmee_building_(block|slab|panel)$') {
            if ($path -match 'framed') { return @{ fabrico = 2; metallum = 2 } }
            return @{ fabrico = 2; saxum = 2 }
        }
        if ($path -match 'factory|terminal|catalyzer|station|io_port|anvil|railgun|tesla|coil|chime|crystal|mining|^module_') { return @{ machina = 3; cognitio = 2 } }
        if ($path -match 'fumo') { return @{ praecantatio = 2; machina = 1 } }
        if ($path -eq 'dye_base') { return @{ praecantatio = 1; fabrico = 1 } }
    }
    if ($script:Ns -eq 'neoecoae') {
        if ($path -match 'storage_cell$|_cell_(16m|64m|256m|4g)$|_cell_housing$') { return @{ vacuos = 4; vas = 3 } }
        if ($path -match '^integrated_working_station_module_') { return @{ machina = 2; cognitio = 1 } }
    }
    if ($script:Ns -eq 'envirotech') {
        if ($path -match 'void_miner_ccu') { return @{ machina = 3; potentia = 2; cognitio = 2; metallum = 1 } }
    }
    if ($script:Ns -eq 'envirocore') {
        if ($path -match 'void_miner') { return @{ machina = 3; potentia = 2; cognitio = 2 } }
        if ($path -match '_modifier$') { return @{ machina = 2; cognitio = 2; potentia = 1 } }
        if ($path -match 'workstation$|assembler$|crystallizer$|solar_distiller$|crusher$|_fab$|laser_core$|laser_lens_holder$|lens_grinder$|memory_programmer$') { return @{ machina = 3; potentia = 2; cognitio = 1 } }
        if ($path -match '_input$|_output$|^null_io$|voltage') { return @{ machina = 2; potentia = 2; iter = 1 } }
        if ($path -match '^(flash_memory|volatile_memory|cpu|ram|module)$') { return @{ cognitio = 3; machina = 2; potentia = 1 } }
        if ($path -match '^(lens|crystal_lens|laser_diode)$') { return @{ vitreus = 2; lux = 2; ordo = 1 } }
        if ($path -match 'structure_panel') { return @{ vitreus = 2; fabrico = 2; ordo = 1 } }
        if ($path -match 'creative_energy_source') { return @{ potentia = 4; machina = 2; auram = 1 } }
        if ($path -match 'distilled_water_bucket') { return @{ aqua = 3; vacuos = 2; vas = 2 } }
        if ($path -match 'rgb_controller') { return @{ lux = 3; machina = 1; potentia = 1 } }
        if ($path -match 'redstone_connector') { return @{ potentia = 3; metallum = 1; ordo = 1 } }
        if ($path -match 'obsidian_plate') { return @{ saxum = 2; tenebrae = 2; metallum = 1 } }
        if ($path -eq 'mica') { return @{ saxum = 2; vitreus = 1; terra = 1 } }
        if ($path -match '_crystal$') { return @{ vitreus = 2; praecantatio = 2; ordo = 1 } }
        if ($path -match '_powder$') { return @{ perditio = 2; terra = 2; metallum = 1 } }
        if ($path -match '_boule$') { return @{ vitreus = 2; metallum = 2; ordo = 1 } }
        if ($path -match '_wafer$') { return @{ vitreus = 2; machina = 2; cognitio = 1 } }
        if ($path -match '_interconnect$') { return @{ metallum = 2; potentia = 2; machina = 1 } }
        if ($path -match '_frame$') { return @{ fabrico = 2; metallum = 2; machina = 1 } }
        if ($path -match 'brick|cobble|polished|tile|slab|stairs|wall') { return @{ saxum = 2; terra = 1; ordo = 1 } }
        if ($path -match '^(litherite|erodium|kyronite|pladium|ionite|aethium|nanorite|xerothium|aeronite|parathium|millirite|microthium)$') { return @{ saxum = 2; terra = 2; ordo = 1 } }
    }
    if ($script:Ns -eq 'avaritia_more_items') {
        if ($path -match '^multiblock_energy_(?:input|output)_(\d+)$') { $t = [int]$Matches[1]; return @{ machina = 3; potentia = 2; metallum = 2; terminus = $t } }
        if ($path -match '^multiblock_fluid_(?:input|output)_(\d+)$') { $t = [int]$Matches[1]; return @{ machina = 3; aqua = 2; vas = 2; terminus = $t } }
        if ($path -match '^multiblock_item_(?:input|output)_(\d+)$') { $t = [int]$Matches[1]; return @{ machina = 3; iter = 2; vas = 2; terminus = $t } }
        if ($path -eq 'machine_shell') { return @{ machina = 3; metallum = 3; fabrico = 2; terminus = 2 } }
        if ($path -eq 'craft_editor_desk') { return @{ metallum = 3; cognitio = 2; terminus = 2 } }
        if ($path -eq 'two_tier_workbench') { return @{ fabrico = 3; metallum = 3; terminus = 2 } }
        if ($path -eq 'photolithography_machine') { return @{ machina = 4; potentia = 3; vitreus = 2; cognitio = 2; terminus = 4 } }
        if ($path -eq 'wrench') { return @{ instrumentum = 3; metallum = 3; terminus = 1 } }
        if ($path -eq 'multiblock_hammer') { return @{ instrumentum = 3; metallum = 3; fabrico = 2; terminus = 2 } }
    }
    if ($script:Ns -eq 'create_aerofreight') {
        if ($path -match '^modifier_') { return @{ machina = 2; cognitio = 2; fabrico = 1 } }
        if ($path -match '^cargo_container|^packed_cargo') { return @{ vas = 3; machina = 2; iter = 2 } }
        if ($path -match '^cargo_shell') { return @{ machina = 2; metallum = 3; fabrico = 1 } }
        if ($path -eq 'cargo_frame') { return @{ fabrico = 2; metallum = 3; machina = 1 } }
        if ($path -match 'dispatch_station|freight_platform') { return @{ machina = 3; cognitio = 2; motus = 1 } }
        if ($path -eq 'freight_docking_interface') { return @{ machina = 3; motus = 2; iter = 1 } }
        if ($path -match 'order_machine|packing_machine') { return @{ machina = 3; cognitio = 2; fabrico = 1 } }
        if ($path -eq 'freight_compass') { return @{ machina = 2; cognitio = 2; iter = 2 } }
        if ($path -eq 'freight_manifest') { return @{ cognitio = 3; charta = 2; humanus = 1 } }
        if ($path -eq 'friendship_card') { return @{ humanus = 3; cognitio = 2; charta = 1 } }
        if ($path -match 'order_ticket') { return @{ cognitio = 2; charta = 2 } }
        if ($path -eq 'order_refresh_debug_tool') { return @{ cognitio = 3; machina = 2; instrumentum = 2 } }
    }
    if ($script:Ns -eq 'kaleidoscope_chinesefood') {
        if ($path -match '^freezer') { return @{ machina = 3; gelum = 3; fabrico = 1 } }
        if ($path -match '^(couplet|fu_character|horizontal_scroll)$') { return @{ charta = 3; lux = 2; humanus = 2 } }
        if ($path -eq 'firecracker') { return @{ ignis = 3; potentia = 2; sonus = 2 } }
        if ($path -eq 'kongming_lantern') { return @{ lux = 3; ignis = 2; aer = 2 } }
        if ($path -eq 'straw_bed') { return @{ arbor = 2; pannus = 2; fabrico = 1 } }
        if ($path -eq 'pickle_jar') { return @{ vas = 3; aqua = 2; fames = 1 } }
        if ($path -eq 'mooncake_mold') { return @{ fabrico = 2; instrumentum = 1; victus = 1 } }
        if ($path -eq 'bowl_stack') { return @{ fabrico = 2; vas = 1; fames = 1 } }
        if ($path -eq 'salt') { return @{ terra = 2; perditio = 1; fames = 1 } }
        if ($path -eq 'salt_bucket') { return @{ aqua = 3; vas = 2; terra = 1 } }
        if ($path -eq 'corn_ristra') { return @{ messis = 2; herba = 2; fames = 1 } }
        if ($path -eq 'eggplant_crop') { return @{ herba = 2; messis = 2; terra = 1 } }
        if ($path -eq 'eggplant_seed') { return @{ herba = 2; victus = 1; terra = 1 } }
        if ($path -match 'spawn_egg$') { return @{ bestia = 3; victus = 3; vinculum = 3; fames = 1 } }
        if ($path -eq 'yellow_croaker_bucket') { return @{ aqua = 3; vas = 2; bestia = 1 } }
        if ($path -match '^yellow_croaker') { return @{ bestia = 3; victus = 2; aqua = 2 } }
        if ($path -match 'tea') { return @{ fames = 2; herba = 2; aqua = 1 } }
        if ($path -match 'platter') { return @{ fames = 3; praecantatio = 2; lucrum = 1 } }
        if ($path -match '^raw_') { return @{ fames = 2; victus = 2; herba = 1 } }
        return @{ fames = 3; victus = 2; herba = 1 }
    }
    if ($script:Ns -eq 'kaleidoscope_cookery' -or $script:Ns -eq 'kaleidoscope_twilight') {
        if ($path -match '^stripped_bamboo') { return @{ arbor = 3; fabrico = 2 } }
        if ($path -eq 'frozen_bun') { return @{ gelum = 3; fames = 2; victus = 2 } }
    }
    if ($script:Ns -eq 'sfm') {
        if ($path -match '_facade$') { return @{ fabrico = 2; metallum = 2 } }
        if ($path -match 'cable') { return @{ machina = 3; metallum = 2; iter = 1 } }
        if ($path -match 'manager') { return @{ machina = 3; cognitio = 3; potentia = 1 } }
        if ($path -eq 'buffer') { return @{ vas = 2; vacuos = 2; machina = 1 } }
        if ($path -eq 'disk') { return @{ cognitio = 3; machina = 2 } }
        if ($path -match '^(form|labelgun|network_tool)$') { return @{ instrumentum = 2; cognitio = 2; machina = 1 } }
        if ($path -eq 'printing_press') { return @{ machina = 3; cognitio = 1; charta = 1 } }
        if ($path -match 'tank|barrel') { return @{ vas = 3; vacuos = 2; aqua = 1 } }
        if ($path -eq 'xp_goop') { return @{ praecantatio = 2; permutatio = 2; aqua = 1 } }
    }
    if ($script:Ns -eq 'thaumicconcilium') {
        if ($path -match '^pontifex_') { return @{ tutamen = 3; praecantatio = 3; humanus = 2 } }
        if ($path -match '_focus$|^impulse_foci$|^signal_foci$') { return @{ praecantatio = 3; instrumentum = 2; cognitio = 1 } }
        if ($path -match 'spawn_egg$|^overanimated_spawn_egg$') { return @{ bestia = 3; praecantatio = 3; vinculum = 2 } }
        if ($path -match '_bracelet$|^burdening_amulet$|^tight_belt$|_vindings$') { return @{ praecantatio = 3; tutamen = 2; accessories = 1 } }
        if ($path -match '_drum$') { return @{ sonus = 3; arbor = 2; praecantatio = 1 } }
        if ($path -match 'crucible') { return @{ praecantatio = 2; ignis = 2; vas = 2; mixtura = 1 } }
        if ($path -match 'crystal|gem$|_gem') { return @{ vitreus = 2; praecantatio = 2; ordo = 1 } }
        if ($path -match 'syringe|_device$|^protobody$|^valet_core$|^assistance_core$') { return @{ machina = 2; praecantatio = 2; cognitio = 1 } }
        if ($path -match '^bottle_of_|_vapour$|^nether_membrane$|^thaumic_slag$') { return @{ mixtura = 2; aqua = 1; praecantatio = 1 } }
        if ($path -match 'shard_mill') { return @{ machina = 2; vitreus = 2; praecantatio = 1 } }
        if ($path -match '^tainted_|^solid_void$|^void_reality$') { return @{ vitium = 3; praecantatio = 2; tenebrae = 1 } }
        if ($path -match '^crimson_annales$|^hex_of_|^red_powered_mind$|^diamond_') { return @{ praecantatio = 3; cognitio = 2; tenebrae = 1 } }
        if ($path -match 'windings$') { return @{ pannus = 2; praecantatio = 2; tutamen = 1 } }
        if ($path -match '^lithographer$|^polishment_device$|^vis_condenser$|^astral_monitor$|^spotlight_focus$') { return @{ machina = 2; cognitio = 2; praecantatio = 1 } }
        if ($path -match 'witchery|botanist|nether_explorer|samurai|dump_jackboots|surfer_shorts') { return @{ pannus = 2; praecantatio = 2; tutamen = 1 } }
    }
    if ($script:Ns -eq 'thaumicenergistics') {
        if ($path -match '_storage_cell_|cell_housing|^advanced_storage_casing$') { return @{ vacuos = 3; vas = 3; praecantatio = 1 } }
        if ($path -match 'arcane_crafting_chamber') { return @{ machina = 3; praecantatio = 2; fabrico = 1 } }
        if ($path -eq 'essentia_terminal') { return @{ machina = 3; cognitio = 3; praecantatio = 1 } }
        if ($path -match 'budding_thaumic_crystal|crystal_bud|^mystic_crystal$|^mystic_crystal_block$|crystal_cluster|^mystic_crystal_dust$') { return @{ vitreus = 3; praecantatio = 2; ordo = 1 } }
        if ($path -match '^mystic_circuit_board$|^mystic_processor$|^thaumic_processor_press$|^thaumic_upgrade_smithing_template$') { return @{ cognitio = 2; machina = 2; metallum = 1 } }
        if ($path -match 'thaumic_crystal_') { return @{ instrumentum = 2; vitreus = 2; praecantatio = 1 } }
    }
    if ($script:Ns -eq 'thaumickaleidoscope') {
        if ($path -match 'gum$') { return @{ praecantatio = 2; fames = 2; ignis = 1 } }
        if ($path -match '^tainted_') { return @{ vitium = 3; fames = 1; victus = 1 } }
        if ($path -match 'salis_seaweed') { return @{ praecantatio = 2; herba = 2; aqua = 1 } }
        if ($path -match 'mana_bean') { return @{ praecantatio = 2; fames = 2; herba = 1 } }
        if ($path -match 'tentacle|slime_stew') { return @{ bestia = 2; fames = 2; praecantatio = 1 } }
        if ($path -match 'crystal|void_popping') { return @{ vitreus = 2; praecantatio = 2; fames = 1 } }
        if ($path -match 'arcane_butter') { return @{ praecantatio = 2; fames = 2; victus = 1 } }
    }
    if ($script:Ns -eq 'thaumicindustry') {
        if ($path -match 'research_table|research') { return @{ cognitio = 3; machina = 2; praecantatio = 2 } }
        if ($path -match 'generator|turbine|boiler') { return @{ machina = 3; potentia = 2; praecantatio = 1 } }
        if ($path -match 'circuit|processor|chip') { return @{ cognitio = 2; machina = 2; potentia = 1 } }
    }
    if ($script:Ns -eq 'forbiddenmagic' -or $script:Ns -eq 'taintedmagic') {
        if ($path -match 'shard') { return @{ vitium = 2; vitreus = 2; praecantatio = 1 } }
        if ($path -match 'blood') { return @{ victus = 2; praecantatio = 2; corpus = 1 } }
    }
    if ($script:Ns -eq 'valkyrielib') {
        if ($path -eq 'guide') { return @{ cognitio = 3; charta = 2; humanus = 1 } }
        if ($path -eq 'info_tablet') { return @{ cognitio = 3; vitreus = 2; machina = 1 } }
    }
    if ($script:Ns -eq 'mio_icif') {
        # fluids (with or without bucket form)
        if ($path -match '^(hotwater|hotcoolant|pahoehoelava|steam|superheated_?steam)(_bucket)?$') { return @{ aqua = 2; ignis = 3; potentia = 2 } }
        if ($path -match '^(biogas|biomass|weed_ex)(_bucket)?$') { return @{ aqua = 2; herba = 2; fames = 1; permutatio = 1 } }
        if ($path -match '^uumatter(_bucket)?$') { return @{ praecantatio = 3; permutatio = 3; vacuos = 2; alienis = 1 } }
        if ($path -match '^(coolant|distilledwater|constructionfoam|crudeoil|dieseloil|air)(_bucket)?$') { return @{ aqua = 2; permutatio = 2; potentia = 1 } }
        # fluid cells
        if ($path -match '^cell_') { return @{ vacuos = 3; vas = 3; metallum = 2; aqua = 1; fabrico = 1 } }
        # ores / raw ore blocks
        if ($path -match '^block_ore_|^block_raw_') { return @{ metallum = 3; saxum = 2; terra = 2; ordo = 1 } }
        # metal storage blocks
        if ($path -match '^block_(tin|lead|uran|uranium|bronze|adviron|niobium|titanium|fence_iron)$') { return @{ metallum = 4; ordo = 2; terra = 1; fabrico = 1 } }
        # wires / transformers
        if ($path -match '^wire_|^transformer_') { return @{ metallum = 2; potentia = 3; motus = 2; ordo = 1 } }
        # pipes / barrel
        if ($path -match '^pipe_|^block_barrel$') { return @{ fabrico = 2; metallum = 2; iter = 1; aqua = 1 } }
        # rotors
        if ($path -match '^rotor_') { return @{ machina = 2; metallum = 2; motus = 3; aer = 1 } }
        # weapons
        if ($path -match 'rocket_launcher|_launcher|^item_rocket$') { return @{ telum = 3; metallum = 2; ardor = 2; volatus = 1 } }
        if ($path -match 'plasma|disruptor|nanosaber|_saber') { return @{ telum = 3; potentia = 2; metallum = 2; praecantatio = 1 } }
        if ($path -match 'armory_link|_rifle|_gun|_bow$|_shield') { return @{ telum = 3; metallum = 2; machina = 1; potentia = 1 } }
        # trinkets / electric gear
        if ($path -match '^item_trinket_|^item_electric_|force_field|life_support') { return @{ praecantatio = 2; tutamen = 2; potentia = 2; volatus = 1 } }
        # tools
        if ($path -match 'driller|chainsaw|laser_miner') { return @{ instrumentum = 2; metallum = 2; machina = 2; potentia = 1 } }
        if ($path -match 'lathing') { return @{ instrumentum = 2; metallum = 2; machina = 1; ordo = 1 } }
        if ($path -match 'detector') { return @{ instrumentum = 2; metallum = 2; cognitio = 1; potentia = 1 } }
        if ($path -match '^item_tool_|wrench|treetap|scanner$|_meter$') { return @{ instrumentum = 2; metallum = 2; machina = 1; cognitio = 1 } }
        # misc tech parts
        if ($path -match '^power_unit') { return @{ potentia = 3; machina = 2; metallum = 2; ordo = 1 } }
        if ($path -match 'electric_light') { return @{ lux = 3; machina = 1; potentia = 1 } }
        if ($path -match 'repressur|pressuriz') { return @{ machina = 3; potentia = 2; metallum = 2; aer = 1 } }
        # machines
        if ($path -match 'generator') { return @{ machina = 3; potentia = 3; ignis = 1; motus = 1 } }
        if ($path -match 'furnace|condenser|distiller|lathe|cutter|teleporter|^tesla$|refinery|fermenter|_elc$') { return @{ machina = 3; potentia = 2; metallum = 2; motus = 1 } }
        # rubber tree
        if ($path -match 'rubber|^block_rub_|rub_wood|stripped_rubber') { return @{ arbor = 3; herba = 2; aqua = 1 } }
        # radiation
        if ($path -match 'radiating') { return @{ vitium = 3; perditio = 2; mortuus = 1; venenum = 1 } }
        # crops
        if ($path -match 'seed') { return @{ herba = 2; victus = 2; terra = 1; fames = 1 } }
        if ($path -eq 'guide') { return @{ cognitio = 3; charta = 2; humanus = 1; fabrico = 1 } }
    }
    if ($path -match '_spawn_egg$')   { return @{ bestia = 3; victus = 3; vinculum = 3; fames = 1 } }
    if ($path -eq 'poop')             { return @{ perditio = 2; corpus = 1 } }
    if ($path -match '^dead_')        { return @{ bestia = 2; victus = 2; mortuus = 2 } }
    if ($path -match '^bled_')        { return @{ bestia = 2; victus = 2 } }
    if ($path -match '^(earthworm|cicada|locust|silkworm|bee)$|^silkworm_cocoon$|^cicada_slough$') { return @{ bestia = 2; victus = 1 } }
    if ($path -match '^(feed|bait)_') { return @{ herba = 2; victus = 1 } }
    if ($path -match 'music_disc')    { return @{ sonus = 3; pannus = 1 } }
    if ($path -match '_sail$')        { return @{ pannus = 3; aer = 2 } }
    if ($path -match 'tire$|_wheel|bearing$') { return @{ machina = 3; metallum = 2 } }
    if ($path -match 'fuze$|_gun$|shell$|ammo|bullet') { return @{ telum = 3; metallum = 2 } }
    if ($path -match 'terminal|toolkit') { return @{ machina = 3; cognitio = 2 } }
    if ($path -match 'geothermal_vent|^magma$|burning_') { return @{ ignis = 3; saxum = 2 } }
    if ($path -match 'engine|rudder|^oar|wind_vane|propeller|seaglide') { return @{ machina = 3; metallum = 2 } }
    if ($path -match '^anchor$')      { return @{ metallum = 3 } }
    if ($path -match 'upgrade$')      { return @{ machina = 2; cognitio = 1 } }
    if ($path -match '_seed$|liana|vine') { return @{ herba = 2; aqua = 1 } }
    if ($path -match '_head$|_ear$|^tail$|_hand$|_foot$') { return @{ pannus = 2; corpus = 1 } }
    if ($path -match '^raw_')         { return @{ bestia = 2; victus = 2; aqua = 1 } }
    if ($path -match $cookedPrefixes) { return @{ fames = 2; bestia = 1; ignis = 1 } }
    if ($path -match 'meat|fish|shrimp|soup|burger|feast|sushi|gras|crumble|_rice$|honeycomb|dried_') { return @{ fames = 2; bestia = 1; ignis = 1 } }
    if ($path -match '_(roe|egg)$')   { return @{ bestia = 1; victus = 1; fames = 1 } }
    if ($path -match '_sapling$|_fruiting_leaves$|^hanging_') { return @{ arbor = 2; herba = 1 } }
    if ($path -match '^(guava|pomelo|chestnut)$|_(jam|juice|icecream|popsicle|flour|cake|dough)$') { return @{ herba = 2; fames = 1 } }
    if ($path -match 'net$|^(fishing_rod|sickle|harrow|syringe|dosing_tube|whistle|watering_can|steelyard|sewing_machine)|^(reel|rod|sinker|float)_|water_level_gauge|weather_instrument|water_quality_detector|fire_extinguisher') { return @{ instrumentum = 2; metallum = 1 } }
    if ($path -match 'treatment|protection|acid$|oxyphylline|herbicide|brush|milker|whip|rope|paint|^atm$') { return @{ instrumentum = 2; metallum = 1 } }
    if ($path -match '^(line_|silk$|blank_silk_|straw_hat|seat_cushion)|_(skin|scale|bone)$') { return @{ pannus = 2; fabrico = 1 } }
    if ($path -match '^money_|credit_card|shopping_computer|purchasing_box|receipt_printer|business_license|guide_book|handbook|clipboard') { return @{ lucrum = 2; cognitio = 1 } }
    if ($path -match 'pipe|waterwheel|water_pump|water_oxygen|chicken_coop|feeding_trough|fertile_farmland|floating_platform|scarecrow|septic_tank|incubator|insect_board|fish_display_board|fish_bowl|pet_nest|bait_station|^uav|^phone$|^carton$|takeaway_bag|handcart') { return @{ fabrico = 2; machina = 1 } }
    if ($path -match 'sauce|paste|_oil$|powder|substance|fertilizer|jinkela|methane_tank|marsh_gas|particles') { return @{ herba = 2; permutatio = 1; fames = 1 } }
    # ---------- generic structural / material rules (namespaces without a dedicated branch) ----------
    if ($path -match '_bucket$') { return @{ aqua = 2; vas = 2 } }
    if ($path -match 'storage_cell|item_cell') { return @{ vas = 3; vacuos = 3 } }
    if ($path -match '(^|_)(log|wood|planks|sapling|leaves|door|trapdoor|fence|fence_gate|hanging_sign|sign|raft|boat|chest_boat|rafter|rafter_end|bracket|beam|joist|post|boards)$') { return @{ arbor = 2; herba = 1 } }
    if ($path -match '(slab|stairs|wall|bricks?|tiles?|polished|chiseled|cracked|mossy|pillar|column|brick|basalt|alabaster|marble|granite|andesite|diorite|calcite|tuff|limestone|terracotta|plaster)$') { return @{ saxum = 2; terra = 1; ordo = 1 } }
    if ($path -match '(gem|gems|crystal|crystals|shard|shards|gemstone|jade|opal|amber|quartz)$') { return @{ vitreus = 2; praecantatio = 2; ordo = 1 } }
    if ($path -match '(_ore$|_ores$|deepslate_|_raw$|^raw_)') { return @{ terra = 2; metallum = 2; saxum = 1 } }
    if ($path -match '(dust|dusts|powder|powders|crushed|grit|clump|clumps|flour)$') { return @{ perditio = 2; terra = 1; metallum = 1 } }
    if ($path -match '(_fluid$|^molten_|molten_)') { return @{ permutatio = 3; aqua = 2; ignis = 1 } }
    if ($path -match '(plate|plating|gear|gears|rod|rods|nugget|nuggets|ingot|ingots|wire|wires|coil|coils|spring|springs|screw|screws|bolt|bolts|casing|shell|chassis|girder|girders|pipe|pipes|sheet|foil|bracket|brackets|housing)$') { return @{ metallum = 2; fabrico = 1 } }
    if ($path -match '(cogwheel|cogwheels|flywheel|shaft|shafts|bearing|bearings|piston|pistons|valve|valves|regulator|actuator|clutch|hose|pulley|pulleys)$') { return @{ machina = 2; metallum = 2; motus = 1 } }
    if ($path -match '(cell|cells|chest|chests|crate|crates|box|boxes|bag|bags|tank|tanks|canister|canisters|barrel|barrels|drum|drums|jar|jars|bottle|bottles|flask|flasks|vat|vats|basket|baskets|bin|bins)$') { return @{ vas = 2; vacuos = 2 } }
    if ($path -match '(circuit|circuits|processor|processors|chip|chips|pcb|board|boards|transistor|diode|capacitor|resistor|layout|layouts|template|templates)$') { return @{ cognitio = 2; machina = 2; potentia = 1 } }
    if ($path -match '(machine|machines|generator|generators|reactor|reactors|furnace|furnaces|crusher|mixer|assembler|fabricator|compressor|drill|pump|engine|engines|motor|motors|turbine|turbines|boiler|boilers|cooler|heater|extractor|infuser|smelter|kiln|separator|charger|injector)$') { return @{ machina = 3; potentia = 2 } }
    if ($path -match '(lamp|lamps|lantern|lanterns|light|lights|torch|torches|candle|candles)$') { return @{ lux = 3 } }
    if ($path -match '(helmet|helmets|chestplate|chestplates|leggings|boots|goggles|gloves|greaves)$') { return @{ tutamen = 3; metallum = 1 } }
    if ($path -match '(robe|robes|hat|hats|hood|cloak|mantle|cap|mask|apron|scarf)$') { return @{ pannus = 2; tutamen = 2 } }
    if ($path -match '(pickaxe|pickaxes|shovel|shovels|hoe|hoes|scythe|scythes|shears|needle|needles|rasp|mallet|mallets|saw|saws|file|files|trowel|trowels|wrench|wrenches|hammer|hammers)$') { return @{ instrumentum = 2; metallum = 2 } }
    if ($path -match '(sword|swords|dagger|daggers|spear|spears|blade|blades|katana|saber|axe|axes)$') { return @{ telum = 3; metallum = 2 } }
    if ($path -match '(_crop$|_crops$|_seeds?$|_sprout$|_seedling$|_straw$|_hay$|^hay_)') { return @{ herba = 2; victus = 1; terra = 1 } }
    if ($path -match '(book|books|tome|tomes|scroll|scrolls|page|pages|note|notes|map|maps|blueprint|blueprints|schematic|schematics|manual|manuals|guide|guides|letter|letters|card|cards|ticket|tickets|menu|menus|journal|journals|recipe|recipes|record|records|license|licenses|permit|permission|certificate|diploma)$') { return @{ cognitio = 2; charta = 2 } }
    if ($path -match '(coin|coins|token|tokens|banknote|currency|credit|credits)$') { return @{ lucrum = 2; metallum = 1 } }
    if ($path -match '(carpet|carpets|rug|rugs|curtain|curtains|cushion|cushions|banner|banners|tapestry|fabric|fabrics|cloth|cloths|string|strings|rope|ropes|thread|threads|silk|wool)$') { return @{ pannus = 2; fabrico = 1 } }
    if ($path -match '(glass|pane|panes|lens|lenses)$') { return @{ vitreus = 2; lux = 1 } }
    return $script:Fallback
}

$aquatic = 'fish|carp|bass|eel|tuna|sardine|crab|shrimp|lobster|oyster|clam|snail|jellyfish|urchin|cucumber|turtle|goby|pleco|loach|tench|redfin|tilapia|bighead|black_fish|grouper|hairtail|halfbeak|mackerel|saury|pomfret|swordfish|rectum|prawn|catfish|anglerfish|bamboorat|bullfrog|mouse'

function Get-EntityAspects([string]$path) {
    if ($script:Ns -eq 'mio_icif') {
        if ($path -eq 'armory_piece') { return @{ telum = 3; machina = 2; volatus = 2; praecantatio = 1 } }
        if ($path -eq 'wingman')      { return @{ machina = 3; volatus = 3; cognitio = 2; aer = 1 } }
    }
    if ($script:Ns -match 'maid|touhou')                            { return @{ humanus = 4; victus = 3 } }
    if ($path -match '^poop$|^thrown_poop$')                       { return @{ perditio = 2; corpus = 1 } }
    if ($path -match 'delivery_rider|customer')                    { return @{ humanus = 4; victus = 3 } }
    if ($path -eq 'uav')                                           { return @{ machina = 3; aer = 3; volatus = 2 } }
    if ($path -match '^carton$|takeaway_bag|handcart|floating_platform|scarecrow|delivery_point') { return @{ fabrico = 2; machina = 1 } }
    if ($path -eq 'falling_fruit')                                 { return @{ herba = 3; victus = 1 } }
    if ($path -eq 'thrown_poultry_egg')                            { return @{ bestia = 1; victus = 1 } }
    if ($path -match 'duck|goose|pigeon|quail|turkey')             { return @{ bestia = 4; victus = 3; volatus = 2 } }
    if ($path -match $aquatic)                                     { return @{ bestia = 4; victus = 3; aqua = 3 } }
    return @{ bestia = 4; victus = 3 }
}

function Get-Path([string]$id) { return ($id -split ':', 2)[1] }
function Get-FileName([string]$id) { return ((Get-Path $id) -replace '[^A-Za-z0-9_\-]', '_') + '.json' }
function Format-Aspects($map) {
    $lines = @()
    foreach ($k in ($map.Keys | Sort-Object)) { $lines += ('    "{0}": {1}' -f $k, $map[$k]) }
    return ($lines -join ",`n")
}

$written = 0
$summary = @{}
function Emit([string]$id, $aspects, [string]$sub, [string]$field) {
    # a lang file can describe ids of another namespace (thermal_foundation -> thermal:...),
    # so file them under the namespace the id actually belongs to
    $idNs = ($id -split ':', 2)[0]
    $dir = Join-Path (Join-Path $AspectRoot $sub) $idNs
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    $tpl = "`n{`n  `"type`": `"goldentweaks:$($field)_aspect`",`n  `"$field`": {`n    `"id`": `"$id`"`n  },`n  `"aspects`": {`n$(Format-Aspects $aspects)`n  }`n}`n"
    [IO.File]::WriteAllText((Join-Path $dir (Get-FileName $id)), $tpl, (New-Object Text.UTF8Encoding($false)))
    $script:written++
    $key = ($aspects.Keys | Sort-Object) -join '+'
    if (-not $script:summary.ContainsKey($key)) { $script:summary[$key] = 0 }
    $script:summary[$key]++
}

if ($Kind -eq 'items' -or $Kind -eq 'both') {
    Write-Output ("items uncovered: {0}" -f (@(Get-NsIds $Jar $Namespace 'items' | Where-Object { -not $covered.Contains($_) }).Count))
    if ($Apply) {
        foreach ($id in (Get-NsIds $Jar $Namespace 'items' | Sort-Object)) {
            if ($covered.Contains($id)) { continue }
            Emit $id (Get-ItemAspects (Get-Path $id)) 'aspects' 'item'
        }
    }
}
if ($Kind -eq 'entities' -or $Kind -eq 'both') {
    Write-Output ("entities uncovered: {0}" -f (@(Get-NsIds $Jar $Namespace 'entities' | Where-Object { -not $covered.Contains($_) }).Count))
    if ($Apply) {
        foreach ($id in (Get-NsIds $Jar $Namespace 'entities' | Sort-Object)) {
            if ($covered.Contains($id)) { continue }
            Emit $id (Get-EntityAspects (Get-Path $id)) 'entity_aspects' 'entity'
        }
    }
}
if ($Apply) {
    Write-Output ("written: {0}" -f $script:written)
    $script:summary.GetEnumerator() | Sort-Object { -$_.Value } | ForEach-Object { "  {0,4}  {1}" -f $_.Value, $_.Key }
}
