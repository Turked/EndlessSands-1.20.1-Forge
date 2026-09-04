param(
    [Parameter(Mandatory = $true)][string]$SourceDirectory,
    [Parameter(Mandatory = $true)][string]$MinecraftClientJar
)

# Exact palette translation, not image synthesis. Run again to rebuild the six texture assets.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$outputDirectory = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../src/main/resources/assets/endlesssands/textures'))
$reportDirectory = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../build/reports/liquid-textures'))
[IO.Directory]::CreateDirectory($reportDirectory) | Out-Null

function Read-ZipBitmap($Archive, [string]$Name) {
    $entry = $Archive.GetEntry($Name)
    if ($null -eq $entry) { throw "Missing image $Name" }
    $stream = $entry.Open()
    try {
        $original = [Drawing.Bitmap]::new($stream)
        try { return $original.Clone() } finally { $original.Dispose() }
    } finally { $stream.Dispose() }
}

function Read-Palette([string]$Name) {
    $palette = @{}
    foreach ($line in Get-Content -LiteralPath (Join-Path $SourceDirectory "$Name.gpl")) {
        if ($line -match '^\s*(\d+)\s+(\d+)\s+(\d+)\s+(\S+)\s*$') {
            $palette[$Matches[4]] = [Drawing.Color]::FromArgb(255, [int]$Matches[1], [int]$Matches[2], [int]$Matches[3])
        }
    }
    return $palette
}

function Read-Mapping([string]$Name, $Palette) {
    $document = Get-Content -Raw -LiteralPath (Join-Path $SourceDirectory "$Name.json") | ConvertFrom-Json
    if ($document.schema -ne 'minecraft_fluid_color_translation.v1') { throw 'Unsupported mapping schema' }
    $mapping = @{}
    foreach ($row in $document.mappings) {
        $rgb = $Palette[$row.target_palette_key]
        if ($null -eq $rgb -or ('#{0:X2}{1:X2}{2:X2}' -f $rgb.R, $rgb.G, $rgb.B) -ne $row.target_rgb) {
            throw "JSON/GPL color mismatch for $($row.target_palette_key)"
        }
        $alpha = [Convert]::ToInt32($row.source_alpha, 16)
        $mapping[$row.source_rgba] = [Drawing.Color]::FromArgb($alpha, $rgb.R, $rgb.G, $rgb.B)
    }
    if ($mapping.Count -ne $document.mapping_count) { throw 'Mapping count mismatch' }
    return $mapping
}

function Rgba-Key([Drawing.Color]$Color) {
    return '#{0:X2}{1:X2}{2:X2}{3:X2}' -f $Color.R, $Color.G, $Color.B, $Color.A
}

function Brightness([Drawing.Color]$Color) {
    return 0.2126 * $Color.R + 0.7152 * $Color.G + 0.0722 * $Color.B
}

$client = [IO.Compression.ZipFile]::OpenRead($MinecraftClientJar)
try {
    foreach ($liquid in @(
        @{ Name = 'ancient_ocean_water'; Vanilla = 'water'; Flow = 'flowing_water'; Mapping = 'water_to_ancient_ocean_water'; FlowMapping = 'flowing_water_to_flowing_ancient_ocean_water' },
        @{ Name = 'star_touched_lava'; Vanilla = 'lava'; Flow = 'flowing_lava'; Mapping = 'lava_to_star_touched_lava'; FlowMapping = 'flowing_lava_to_flowing_star_touched_lava' }
    )) {
        $palette = Read-Palette $liquid.Name
        $stillMapping = Read-Mapping $liquid.Mapping $palette
        $flowMapping = Read-Mapping $liquid.FlowMapping $palette
        foreach ($variant in @(
            @{ Archive = $liquid.Vanilla; Suffix = 'still'; Mapping = $stillMapping },
            @{ Archive = $liquid.Flow; Suffix = 'flow'; Mapping = $flowMapping }
        )) {
            $archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $SourceDirectory "$($variant.Archive)_png_frames.zip"))
            try {
                $frames = @($archive.Entries | Where-Object { $_.FullName -match '_frame_\d+\.png$' } |
                    Sort-Object { [int]([regex]::Match($_.FullName, '_frame_(\d+)\.png$').Groups[1].Value) })
                if ($frames.Count -eq 0) { throw 'No animation frames' }
                $first = Read-ZipBitmap $archive $frames[0].FullName
                $width = $first.Width
                $height = $first.Height
                $first.Dispose()
                $strip = [Drawing.Bitmap]::new($width, $height * $frames.Count, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
                # The separately exported translucent PNGs have rounded RGB values. The lossless
                # source strip in each ZIP is authoritative and matches the supplied RGBA mapping.
                $sourceStrip = Read-ZipBitmap $archive "$($variant.Archive)_source_strip.png"
                try {
                    if ($sourceStrip.Width -ne $width -or $sourceStrip.Height -ne $height * $frames.Count) {
                        throw 'Source strip/frame count mismatch'
                    }
                    for ($i = 0; $i -lt $frames.Count; $i++) {
                        $frame = Read-ZipBitmap $archive $frames[$i].FullName
                        try {
                            if ($frame.Width -ne $width -or $frame.Height -ne $height) { throw 'Inconsistent frame dimensions' }
                            for ($y = 0; $y -lt $height; $y++) {
                                for ($x = 0; $x -lt $width; $x++) {
                                    $pixel = $sourceStrip.GetPixel($x, $i * $height + $y)
                                    $key = Rgba-Key $pixel
                                    if (!$variant.Mapping.ContainsKey($key)) { throw "Unmapped color $key in $($frames[$i].FullName)" }
                                    $strip.SetPixel($x, $i * $height + $y, $variant.Mapping[$key])
                                }
                            }
                        } finally { $frame.Dispose() }
                    }
                    $destination = Join-Path $outputDirectory "block/$($liquid.Name)_$($variant.Suffix).png"
                    $strip.Save($destination, [Drawing.Imaging.ImageFormat]::Png)
                    $verified = [Drawing.Bitmap]::new($destination)
                    try {
                        for ($y = 0; $y -lt $verified.Height; $y++) {
                            for ($x = 0; $x -lt $verified.Width; $x++) {
                                $expected = $variant.Mapping[(Rgba-Key $sourceStrip.GetPixel($x, $y))]
                                if ($verified.GetPixel($x, $y).ToArgb() -ne $expected.ToArgb()) {
                                    throw "Saved PNG changed the mapped pixel at $x,$y"
                                }
                            }
                        }
                    } finally { $verified.Dispose() }
                    # Preserve vanilla speed and lava's forward/backward frame sequence exactly.
                    $metaEntry = $client.GetEntry("assets/minecraft/textures/block/$($liquid.Vanilla)_$($variant.Suffix).png.mcmeta")
                    $reader = [IO.StreamReader]::new($metaEntry.Open())
                    try { [IO.File]::WriteAllText("$destination.mcmeta", $reader.ReadToEnd(), [Text.UTF8Encoding]::new($false)) }
                    finally { $reader.Dispose() }
                    Write-Output "$($liquid.Name)_$($variant.Suffix): $($frames.Count) frames, ${width}x${height}; all pixels matched JSON/GPL"
                } finally { $sourceStrip.Dispose(); $strip.Dispose() }
            } finally { $archive.Dispose() }
        }

        $bucket = Read-ZipBitmap $client "assets/minecraft/textures/item/$($liquid.Vanilla)_bucket.png"
        $originalBucket = [Drawing.Bitmap]::new($bucket)
        try {
            # Vanilla bucket metal/outline are grayscale; ONLY chromatic liquid pixels are translated.
            # Bucket colors are absent from the block mappings. Match their light/dark range to
            # the mapped fluid palette, retaining bucket shading and original alpha.
            $shades = @{}
            for ($y = 0; $y -lt $bucket.Height; $y++) {
                for ($x = 0; $x -lt $bucket.Width; $x++) {
                    $c = $bucket.GetPixel($x, $y)
                    if ($c.A -gt 0 -and ($c.R -ne $c.G -or $c.G -ne $c.B)) { $shades[$c.ToArgb()] = $c }
                }
            }
            $ordered = @($shades.Values | Sort-Object { Brightness $_ })
            $targets = @($palette.Values | Sort-Object { Brightness $_ })
            $min = Brightness $ordered[0]
            $range = (Brightness $ordered[-1]) - $min
            $targetMin = Brightness $targets[0]
            $targetRange = (Brightness $targets[-1]) - $targetMin
            $bucketMap = @{}
            foreach ($shade in $ordered) {
                $wanted = $targetMin + ((Brightness $shade) - $min) / $range * $targetRange
                $target = $targets | Sort-Object { [Math]::Abs((Brightness $_) - $wanted) } | Select-Object -First 1
                $bucketMap[$shade.ToArgb()] = [Drawing.Color]::FromArgb($shade.A, $target.R, $target.G, $target.B)
            }
            $changed = 0
            for ($y = 0; $y -lt $bucket.Height; $y++) {
                for ($x = 0; $x -lt $bucket.Width; $x++) {
                    $c = $bucket.GetPixel($x, $y)
                    if ($bucketMap.ContainsKey($c.ToArgb())) {
                        $bucket.SetPixel($x, $y, $bucketMap[$c.ToArgb()])
                        $changed++
                    } elseif ($bucket.GetPixel($x, $y).ToArgb() -ne $originalBucket.GetPixel($x, $y).ToArgb()) {
                        throw 'Bucket metal/outline was altered'
                    }
                }
            }
            $bucket.Save((Join-Path $outputDirectory "item/$($liquid.Name)_bucket.png"), [Drawing.Imaging.ImageFormat]::Png)
            $preview = [Drawing.Bitmap]::new(512, 256)
            $graphics = [Drawing.Graphics]::FromImage($preview)
            try {
                $graphics.Clear([Drawing.Color]::FromArgb(60, 60, 60))
                $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
                $graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
                $graphics.DrawImage($originalBucket, 0, 0, 256, 256)
                $graphics.DrawImage($bucket, 256, 0, 256, 256)
                $preview.Save((Join-Path $reportDirectory "$($liquid.Name)_buckets.png"), [Drawing.Imaging.ImageFormat]::Png)
            } finally { $graphics.Dispose(); $preview.Dispose() }
            Write-Output "$($liquid.Name)_bucket: recolored $changed liquid pixels; metal, outline, transparency unchanged"
        } finally { $originalBucket.Dispose(); $bucket.Dispose() }
    }
} finally { $client.Dispose() }
