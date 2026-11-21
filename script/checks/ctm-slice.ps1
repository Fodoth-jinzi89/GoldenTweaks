#requires -Version 5.1
<#
.SYNOPSIS
    Slice / generate OptiFine-standard CTM tiles (0.png .. 46.png) consumed by
    Continuity / NeoContinuity.

.DESCRIPTION
    Continuity's `ctm` method needs at least 47 sprites. `tiles=` does NOT crop a
    single atlas image: every entry resolves to its own PNG file. This script
    produces those files.

    Two modes:

      -Sheet   Slice an existing 12x4 mother sheet into 0..46.png.
      -Source  Synthesize all 47 tiles from one base tile by suppressing the
               border on every side that is connected.

    Tile index -> connected sides below is derived from
    CtmSpriteProvider.SPRITE_INDEX_MAP in the Continuity jar, whose raw masks use
    bit0/2/4/6 for the four side directions and bit1/3/5/7 for the corners.
    Here sides are re-packed as bit0 = right, bit1 = bottom, bit2 = left,
    bit3 = top. Index 0 is the isolated tile, 46 is fully connected, and 26 is
    fully connected including corner joints.

.PARAMETER Sheet
    12x4 grid mother sheet. Only 47 of the 48 cells are used (index 47 unused).

.PARAMETER Source
    Single base tile used to synthesize the 47 tiles.

.PARAMETER Output
    Output directory. Receives 0.png .. 46.png.

.PARAMETER BandWidth
    Pixel band removed from a connected side. 0 = auto (tileSize / 8).

.PARAMETER Properties
    Optional path; when set a ready-to-use .properties file is written there.

.PARAMETER TilePrefix
    Sprite prefix used for the generated tiles= line, for example
    goldentweaks:textures/block/ctm/shell_pe

.PARAMETER MatchBlocks
    Block id written to matchBlocks=, for example goldentweaks:shell_pe

.EXAMPLE
    ./ctm-slice.ps1 -Source multiblock/shell_pe.png -Output src/main/resources/assets/goldentweaks/textures/block/ctm/shell_pe -Properties src/main/resources/assets/goldentweaks/optifine/ctm/shell_pe.properties -TilePrefix goldentweaks:textures/block/ctm/shell_pe -MatchBlocks goldentweaks:shell_pe
#>
[CmdletBinding(DefaultParameterSetName = 'Generate')]
param(
    [Parameter(Mandatory = $true, ParameterSetName = 'Sheet')]
    [string]$Sheet,

    [Parameter(Mandatory = $true, ParameterSetName = 'Generate')]
    [string]$Source,

    [Parameter(Mandatory = $true)]
    [string]$Output,

    [int]$BandWidth = 0,

    [string]$Properties,

    [string]$TilePrefix,

    [string]$MatchBlocks
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$SheetColumns = 12
$SheetRows = 4
$TileCount = 47

# Connection bit per tile index. bit0 = right, bit1 = bottom, bit2 = left, bit3 = top.
$TileSides = @(
    0, 4, 5, 1, 6, 3, 14, 7,           # 0-7
    15, 15, 15, 15,                    # 8-11
    2, 6, 7, 3,                        # 12-15
    12, 9, 13, 11,                     # 16-19
    15, 15, 15, 15,                    # 20-23
    10, 14, 15, 11, 14, 7, 14, 7,      # 24-31
    15, 15, 15, 15,                    # 32-35
    8, 12, 13, 9, 13, 11, 13, 11,      # 36-43
    15, 15, 15                         # 44-46
)

if ($TileSides.Count -ne $TileCount) {
    throw "internal error: TileSides has $($TileSides.Count) entries, expected $TileCount"
}

function New-ArgbBitmap {
    param([string]$Path)

    $img = [System.Drawing.Image]::FromFile((Resolve-Path -LiteralPath $Path).Path)
    try {
        $bmp = New-Object System.Drawing.Bitmap($img.Width, $img.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($bmp)
        try {
            $graphics.DrawImage($img, 0, 0, $img.Width, $img.Height)
        }
        finally {
            $graphics.Dispose()
        }
        return $bmp
    }
    finally {
        $img.Dispose()
    }
}

New-Item -ItemType Directory -Path $Output -Force | Out-Null
$outputDir = (Resolve-Path -LiteralPath $Output).Path

if ($PSCmdlet.ParameterSetName -eq 'Sheet') {
    $sheetBmp = New-ArgbBitmap -Path $Sheet
    try {
        if ($sheetBmp.Width % $SheetColumns -ne 0) {
            throw "sheet width $($sheetBmp.Width) is not divisible by $SheetColumns"
        }
        if ($sheetBmp.Height % $SheetRows -ne 0) {
            throw "sheet height $($sheetBmp.Height) is not divisible by $SheetRows"
        }

        $tileWidth = [int]($sheetBmp.Width / $SheetColumns)
        $tileHeight = [int]($sheetBmp.Height / $SheetRows)
        if ($tileWidth -ne $tileHeight) {
            throw "cells are ${tileWidth}x${tileHeight}, expected square"
        }

        for ($index = 0; $index -lt $TileCount; $index++) {
            $column = $index % $SheetColumns
            $row = [int][Math]::Floor($index / $SheetColumns)

            $tile = New-Object System.Drawing.Bitmap($tileWidth, $tileHeight, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $graphics = [System.Drawing.Graphics]::FromImage($tile)
            try {
                $sourceRect = New-Object System.Drawing.Rectangle(($column * $tileWidth), ($row * $tileHeight), $tileWidth, $tileHeight)
                $destRect = New-Object System.Drawing.Rectangle(0, 0, $tileWidth, $tileHeight)
                $graphics.DrawImage($sheetBmp, $destRect, $sourceRect, [System.Drawing.GraphicsUnit]::Pixel)
            }
            finally {
                $graphics.Dispose()
            }

            try {
                $tile.Save((Join-Path $outputDir "$index.png"), [System.Drawing.Imaging.ImageFormat]::Png)
            }
            finally {
                $tile.Dispose()
            }
        }
    }
    finally {
        $sheetBmp.Dispose()
    }
}
else {
    $sourceBmp = New-ArgbBitmap -Path $Source
    try {
        $size = $sourceBmp.Width
        if ($sourceBmp.Height -ne $size) {
            throw "source tile is $($sourceBmp.Width)x$($sourceBmp.Height), expected square"
        }

        if ($BandWidth -le 0) {
            $BandWidth = [int][Math]::Max(1, [Math]::Floor($size / 8))
        }
        if ($BandWidth -ge $size) {
            throw "BandWidth $BandWidth leaves no room in a ${size}px tile"
        }

        $fill = $sourceBmp.GetPixel([int][Math]::Floor($size / 2), [int][Math]::Floor($size / 2))

        for ($index = 0; $index -lt $TileCount; $index++) {
            $sides = $TileSides[$index]
            $connectRight = ($sides -band 1) -ne 0
            $connectBottom = ($sides -band 2) -ne 0
            $connectLeft = ($sides -band 4) -ne 0
            $connectTop = ($sides -band 8) -ne 0

            $tile = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                for ($y = 0; $y -lt $size; $y++) {
                    for ($x = 0; $x -lt $size; $x++) {
                        $clear = $false
                        if ($connectRight -and $x -ge ($size - $BandWidth)) { $clear = $true }
                        if ($connectLeft -and $x -lt $BandWidth) { $clear = $true }
                        if ($connectBottom -and $y -ge ($size - $BandWidth)) { $clear = $true }
                        if ($connectTop -and $y -lt $BandWidth) { $clear = $true }

                        if ($clear) {
                            $tile.SetPixel($x, $y, $fill)
                        }
                        else {
                            $tile.SetPixel($x, $y, $sourceBmp.GetPixel($x, $y))
                        }
                    }
                }

                $tile.Save((Join-Path $outputDir "$index.png"), [System.Drawing.Imaging.ImageFormat]::Png)
            }
            finally {
                $tile.Dispose()
            }
        }
    }
    finally {
        $sourceBmp.Dispose()
    }
}

Write-Host "wrote $TileCount tiles to $outputDir"

if ($Properties) {
    if (-not $TilePrefix) { throw '-TilePrefix is required when -Properties is used' }
    if (-not $MatchBlocks) { throw '-MatchBlocks is required when -Properties is used' }

    $entries = for ($index = 0; $index -lt $TileCount; $index++) { "$TilePrefix/$index.png" }

    $lines = @(
        'method=ctm'
        "matchBlocks=$MatchBlocks"
        'connect=block'
        'faces=all'
        'innerSeams=false'
        ('tiles=' + ($entries -join ','))
    )

    $propertiesPath = [System.IO.Path]::GetFullPath($Properties)
    $propertiesDir = [System.IO.Path]::GetDirectoryName($propertiesPath)
    if (-not (Test-Path -LiteralPath $propertiesDir)) {
        New-Item -ItemType Directory -Path $propertiesDir -Force | Out-Null
    }

    Set-Content -LiteralPath $propertiesPath -Value $lines -Encoding ASCII
    Write-Host "wrote properties to $propertiesPath"
}
