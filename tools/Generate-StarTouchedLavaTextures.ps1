param(
    [Parameter(Mandatory = $true)][string]$MinecraftClientJar,
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '../src/main/resources/assets/endlesssands/textures'),
    [string]$ReportDirectory = (Join-Path $PSScriptRoot '../build/reports/star-touched-lava')
)

# Rebuild the authored Star Touched Lava art without the old orange palette mapping.
# Still source: texture-sources/star_touched_lava_still.png, an authored 20-frame strip.
# Flow is derived directly from those same still frames, so the surface and falling
# liquid retain one visual identity. Periodic sampling keeps every block edge seamless.
# This is an art build tool, separate from Minecraft DataGen. Requires PowerShell 7.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
$ReportDirectory = [IO.Path]::GetFullPath($ReportDirectory)
[IO.Directory]::CreateDirectory((Join-Path $OutputDirectory 'block')) | Out-Null
[IO.Directory]::CreateDirectory((Join-Path $OutputDirectory 'item')) | Out-Null
[IO.Directory]::CreateDirectory($ReportDirectory) | Out-Null

if (-not ('StarTouchedLavaArt' -as [type])) {
    $drawingReferences = @('System.Drawing.Common', 'System.Drawing.Primitives')
    # PowerShell 7.6 / .NET 10 split GDI+ interfaces into an additional assembly.
    foreach ($assembly in @('System.Private.Windows.GdiPlus.dll', 'System.Private.Windows.Core.dll')) {
        $gdiAssembly = Join-Path $PSHOME $assembly
        if (Test-Path -LiteralPath $gdiAssembly) { $drawingReferences += $gdiAssembly }
    }
    Add-Type -ReferencedAssemblies $drawingReferences -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;

public static class StarTouchedLavaArt
{
    public const int Size = 64;
    public const int Frames = 64;
    private const double Tau = Math.PI * 2;

    private static double Wrap(double x, double period)
    {
        double wrapped = x - Math.Floor(x / period) * period;
        // Rounding a tiny negative input can produce period instead of zero.
        return wrapped >= period ? 0 : Math.Max(0, wrapped);
    }
    private static double Clamp(double x) { return Math.Max(0, Math.Min(1, x)); }
    private static double Luma(Color c) { return .2126 * c.R + .7152 * c.G + .0722 * c.B; }
    private static Color Rgb(double r, double g, double b)
    {
        return Color.FromArgb(255, (int)Math.Round(Clamp(r / 255) * 255),
            (int)Math.Round(Clamp(g / 255) * 255), (int)Math.Round(Clamp(b / 255) * 255));
    }
    private static Color Mix(Color a, Color b, double t)
    {
        return Rgb(a.R + (b.R - a.R) * t, a.G + (b.G - a.G) * t, a.B + (b.B - a.B) * t);
    }

    // Area sampling preserves the generated shapes; a small highlight contribution
    // keeps tiny star-hot cores legible when reducing the large source to 64 pixels.
    public static Bitmap MakeTile(Bitmap source)
    {
        var tile = new Bitmap(Size, Size, PixelFormat.Format32bppArgb);
        for (int y = 0; y < Size; y++) for (int x = 0; x < Size; x++)
        {
            double r = 0, g = 0, b = 0;
            int n = 0;
            Color brightest = Color.Black;
            for (int sy = y * source.Height / Size; sy < (y + 1) * source.Height / Size; sy++)
            for (int sx = x * source.Width / Size; sx < (x + 1) * source.Width / Size; sx++)
            {
                Color c = source.GetPixel(sx, sy);
                r += c.R; g += c.G; b += c.B; n++;
                if (Luma(c) > Luma(brightest)) brightest = c;
            }
            tile.SetPixel(x, y, Mix(Rgb(r / n, g / n, b / n), brightest, .18));
        }
        // Reconcile opposite edges in a narrow border so the authored image tiles
        // cleanly even if its generated boundary pixels are not an exact match.
        for (int edge = 0; edge < 3; edge++)
        {
            double weight = edge == 0 ? .5 : (edge == 1 ? .2 : .05);
            for (int y = 0; y < Size; y++)
            {
                Color a = tile.GetPixel(edge, y), b = tile.GetPixel(Size - 1 - edge, y);
                tile.SetPixel(edge, y, Mix(a, b, weight));
                tile.SetPixel(Size - 1 - edge, y, Mix(b, a, weight));
            }
            for (int x = 0; x < Size; x++)
            {
                Color a = tile.GetPixel(x, edge), b = tile.GetPixel(x, Size - 1 - edge);
                tile.SetPixel(x, edge, Mix(a, b, weight));
                tile.SetPixel(x, Size - 1 - edge, Mix(b, a, weight));
            }
        }
        return tile;
    }

    private static Color Sample(Bitmap tile, double x, double y)
    {
        x = Wrap(x, tile.Width); y = Wrap(y, tile.Height);
        int ix = (int)x, iy = (int)y;
        Color top = Mix(tile.GetPixel(ix, iy), tile.GetPixel((ix + 1) % tile.Width, iy), x - ix);
        Color bottom = Mix(tile.GetPixel(ix, (iy + 1) % tile.Height),
            tile.GetPixel((ix + 1) % tile.Width, (iy + 1) % tile.Height), x - ix);
        return Mix(top, bottom, y - iy);
    }

    public static Bitmap MakeStrip(Bitmap tile, Bitmap glint, bool flowing)
    {
        // Minecraft's flow UVs sample half the sprite. Repeat the 64-pixel tile in
        // all four quadrants of a 128-pixel flow frame to preserve still/flow scale.
        int size = flowing ? Size * 2 : Size;
        var strip = new Bitmap(size, size * Frames, PixelFormat.Format32bppArgb);
        for (int frame = 0; frame < Frames; frame++)
        {
            double phase = (double)frame / Frames;
            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
            {
                double u = x % Size, v = y % Size;
                double scroll = flowing ? phase * Size : 0;
                double sx = u + 2.2 * Math.Sin(Tau * (v / Size + phase))
                    + .9 * Math.Cos(Tau * (2 * u / Size - phase));
                double sy = v - scroll + 1.7 * Math.Sin(Tau * (u / Size - phase))
                    + .7 * Math.Cos(Tau * (2 * v / Size + phase));
                Color molten = Sample(tile, sx, sy);

                // Two independently moving passes of vanilla's real glint texture.
                // Integer coordinate transforms and whole-period motion close both
                // spatial seams and the last-to-first animation transition.
                Color g1 = Sample(glint, (u + v) / Size * glint.Width + phase * glint.Width,
                    (v - u) / Size * glint.Height - phase * glint.Height);
                Color g2 = Sample(glint, (u - v) / Size * glint.Width - 2 * phase * glint.Width,
                    (u + v) / Size * glint.Height + phase * glint.Height);
                double shimmer = Clamp((Luma(g1) + .6 * Luma(g2) - 18) / 180);
                double sweep = Math.Pow(.5 + .5 * Math.Sin(Tau * ((u + v) / Size - phase)), 14);
                double amount = .18 * shimmer + .18 * sweep;
                Color enchanted = Mix(molten, Color.FromArgb(148, 126, 228), amount);
                double hot = Clamp((Luma(molten) - 125) / 95);
                double pulse = Math.Pow(.5 + .5 * Math.Sin(Tau * (phase + (4 * u + 8 * v) / Size)), 6) * hot;
                strip.SetPixel(x, frame * size + y,
                    Rgb(enchanted.R + 16 * pulse, enchanted.G + 19 * pulse, enchanted.B + 14 * pulse));
            }
        }
        return strip;
    }

    public static Bitmap MakeBucket(Bitmap vanilla)
    {
        var result = new Bitmap(vanilla);
        Color[] palette = { Color.FromArgb(16, 23, 59), Color.FromArgb(29, 48, 91),
            Color.FromArgb(53, 101, 133), Color.FromArgb(112, 179, 190), Color.FromArgb(237, 241, 184) };
        double min = 255, max = 0;
        for (int y = 0; y < vanilla.Height; y++) for (int x = 0; x < vanilla.Width; x++)
        {
            Color c = vanilla.GetPixel(x, y);
            if (c.A > 0 && (c.R != c.G || c.G != c.B))
            { min = Math.Min(min, Luma(c)); max = Math.Max(max, Luma(c)); }
        }
        for (int y = 0; y < vanilla.Height; y++) for (int x = 0; x < vanilla.Width; x++)
        {
            Color c = vanilla.GetPixel(x, y);
            if (c.A == 0 || (c.R == c.G && c.G == c.B)) continue;
            int index = (int)Math.Round(Clamp((Luma(c) - min) / Math.Max(1, max - min)) * (palette.Length - 1));
            Color target = palette[index];
            result.SetPixel(x, y, Color.FromArgb(c.A, target.R, target.G, target.B));
        }
        return result;
    }

    private static Color SampleStillFrame(Bitmap still, int frame, double x, double y)
    {
        const int frameSize = 16;
        x = Wrap(x, frameSize); y = Wrap(y, frameSize);
        int ix = (int)x, iy = (int)y;
        int nextX = (ix + 1) % frameSize, nextY = (iy + 1) % frameSize;
        int topY = frame * frameSize;
        Color top = Mix(still.GetPixel(ix, topY + iy),
            still.GetPixel(nextX, topY + iy), x - ix);
        Color bottom = Mix(still.GetPixel(ix, topY + nextY),
            still.GetPixel(nextX, topY + nextY), x - ix);
        return Mix(top, bottom, y - iy);
    }

    public static Bitmap MakeFlowFromStill(Bitmap still)
    {
        const int frameSize = 16;
        const int flowSize = 32;
        const int frameCount = 20;
        var strip = new Bitmap(flowSize, flowSize * frameCount, PixelFormat.Format32bppArgb);
        for (int frame = 0; frame < frameCount; frame++)
        {
            double phase = (double)frame / frameCount;
            for (int y = 0; y < flowSize; y++) for (int x = 0; x < flowSize; x++)
            {
                double u = x % frameSize, v = y % frameSize;
                double sx = u + 1.15 * Math.Sin(Tau * (v / frameSize + phase));
                double sy = v - phase * frameSize
                    + .65 * Math.Sin(Tau * (u / frameSize - phase));
                strip.SetPixel(x, frame * flowSize + y,
                    SampleStillFrame(still, frame, sx, sy));
            }
        }
        return strip;
    }

    public static void SavePreview(Bitmap still, Bitmap flow, Bitmap bucket, string path)
    {
        int stillSize = still.Width;
        int stillFrames = still.Height / stillSize;
        using (var preview = new Bitmap(1024, 640))
        using (var graphics = Graphics.FromImage(preview))
        using (var font = new Font("Segoe UI", 16))
        using (var small = new Font("Segoe UI", 11))
        {
            graphics.Clear(Color.FromArgb(14, 19, 33));
            graphics.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.NearestNeighbor;
            graphics.PixelOffsetMode = System.Drawing.Drawing2D.PixelOffsetMode.Half;
            graphics.DrawString("STAR TOUCHED LAVA", font, Brushes.White, 24, 16);
            graphics.DrawString("Still surface / 3 x 3 tiles", small, Brushes.LightSteelBlue, 24, 57);
            for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++)
                graphics.DrawImage(still, new Rectangle(24 + x * 128, 88 + y * 128, 128, 128),
                    new Rectangle(0, 0, stillSize, stillSize), GraphicsUnit.Pixel);
            graphics.DrawString("Flowing surface / same pixel scale", small, Brushes.LightSteelBlue, 440, 57);
            graphics.DrawImage(flow, new Rectangle(440, 88, 256, 256),
                new Rectangle(0, 0, flow.Width, flow.Width), GraphicsUnit.Pixel);
            graphics.DrawImage(flow, new Rectangle(440, 344, 256, 128),
                new Rectangle(0, 0, flow.Width, flow.Width / 2), GraphicsUnit.Pixel);
            graphics.DrawString("Bucket", small, Brushes.LightSteelBlue, 748, 57);
            graphics.DrawImage(bucket, new Rectangle(748, 105, 224, 224));
            graphics.DrawString("Animation phases / molten motion + drifting violet enchantment", small,
                Brushes.LightSteelBlue, 24, 492);
            for (int i = 0; i < 8; i++)
                graphics.DrawImage(still, new Rectangle(24 + i * 124, 524, 96, 96),
                    new Rectangle(0, i * stillFrames / 8 * stillSize, stillSize, stillSize), GraphicsUnit.Pixel);
            preview.Save(path, ImageFormat.Png);
        }
    }
}
'@
}

function Read-ZipBitmap($Archive, [string]$Name) {
    $entry = $Archive.GetEntry($Name)
    if ($null -eq $entry) { throw "Missing vanilla image: $Name" }
    $stream = $entry.Open()
    try {
        $original = [Drawing.Bitmap]::new($stream)
        try { return [Drawing.Bitmap]::new($original) } finally { $original.Dispose() }
    } finally { $stream.Dispose() }
}

$archive = [IO.Compression.ZipFile]::OpenRead($MinecraftClientJar)
$source = $stillSource = $tile = $glint = $vanillaBucket = $still = $flow = $bucket = $null
try {
    $source = [Drawing.Bitmap]::new((Join-Path $PSScriptRoot 'texture-sources/star_touched_lava.png'))
    $stillSource = [Drawing.Bitmap]::new((Join-Path $PSScriptRoot 'texture-sources/star_touched_lava_still.png'))
    if ($stillSource.Width -ne 16 -or $stillSource.Height -ne 320) {
        throw 'Star Touched Lava still source must be a 16x320 strip containing 20 16x16 frames.'
    }
    $tile = [StarTouchedLavaArt]::MakeTile($source)
    $glint = Read-ZipBitmap $archive 'assets/minecraft/textures/misc/enchanted_glint_item.png'
    $vanillaBucket = Read-ZipBitmap $archive 'assets/minecraft/textures/item/lava_bucket.png'
    $still = [Drawing.Bitmap]::new($stillSource)
    $flow = [StarTouchedLavaArt]::MakeFlowFromStill($stillSource)
    $bucket = [StarTouchedLavaArt]::MakeBucket($vanillaBucket)
    foreach ($variant in @(
        @{Name='still'; Image=$still; Frames=20},
        @{Name='flow'; Image=$flow; Frames=20}
    )) {
        $destination = Join-Path $OutputDirectory "block/star_touched_lava_$($variant.Name).png"
        $variant.Image.Save($destination, [Drawing.Imaging.ImageFormat]::Png)
        $metadata = @{animation=@{frametime=2; interpolate=$true; width=$variant.Image.Width; height=$variant.Image.Width}} |
            ConvertTo-Json -Depth 3
        [IO.File]::WriteAllText("$destination.mcmeta", "$metadata`n", [Text.UTF8Encoding]::new($false))
        $duration = $variant.Frames / 10.0
        Write-Output "star_touched_lava_$($variant.Name): $($variant.Frames) frames, $($variant.Image.Width)x$($variant.Image.Width), $duration-second loop"
    }
    $bucket.Save((Join-Path $OutputDirectory 'item/star_touched_lava_bucket.png'), [Drawing.Imaging.ImageFormat]::Png)
    [StarTouchedLavaArt]::SavePreview($still, $flow, $bucket, (Join-Path $ReportDirectory 'preview.png'))
    # A self-contained playback report uses the same 100ms frames and interpolation
    # as Minecraft. Reports live under ignored build/, never in runtime resources.
    $html = @'
<!doctype html>
<html lang="en"><meta charset="utf-8"><title>Star Touched Lava — animation preview</title>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
body{margin:0;padding:36px;background:#0e1321;color:#e2edf1;font:16px system-ui}
main{max-width:1000px;margin:auto}h1{font-size:30px;margin:0 0 10px}p{color:#a5b8cf}
.panels{display:flex;flex-wrap:wrap;gap:32px;margin:28px 0}canvas{image-rendering:pixelated;max-width:100%;height:auto}
button,input{accent-color:#9ce6e9}button{background:#253352;color:#e2edf1;border:1px solid #486186;border-radius:6px;padding:9px 20px;margin-right:16px}
label{display:block;margin:10px 0}input{width:300px;max-width:75vw}
</style><main><h1>Star Touched Lava</h1><p>Midnight molten eddies, pearl-hot cores and a drifting violet enchantment.</p>
<button id="play">Pause</button><span id="time"></span>
<label>Animation position <input id="seek" type="range" min="0" max="6399" step="1" value="0"></label>
<div class="panels"><section><p>Still pool · 3 × 3 blocks</p><canvas id="still" width="192" height="192" style="width:480px"></canvas></section>
<section><p>Flow · 2 × 3 blocks</p><canvas id="flow" width="128" height="192" style="width:320px"></canvas></section></div>
<p>Still and flow: 20 frames / 2.0-second loop. Texture preview; in-game lighting and particles are not shown.</p></main>
<script>
const urls=['data:image/png;base64,%STILL%','data:image/png;base64,%FLOW%'];
const ids=['still','flow'],images=urls.map(url=>{const img=new Image();img.src=url;return img});
const widths=[16,32],frameCounts=[20,20];
const scratch=document.createElement('canvas');scratch.width=scratch.height=64;
const ctx=scratch.getContext('2d');ctx.imageSmoothingEnabled=false;
let running=true,position=0,last=performance.now();
const play=document.getElementById('play'),seek=document.getElementById('seek');
play.onclick=()=>{running=!running;play.textContent=running?'Pause':'Play'};
seek.oninput=()=>{position=Number(seek.value);running=false;play.textContent='Play'};
function draw(now){if(running)position=(position+now-last)%6400;last=now;
images.forEach((img,i)=>{if(!img.complete||!img.naturalWidth)return;
const phase=position/100,frame=Math.floor(phase)%frameCounts[i],blend=phase-Math.floor(phase),width=widths[i];
ctx.globalAlpha=1;ctx.drawImage(img,0,frame*width,width,width,0,0,64,64);
ctx.globalAlpha=blend;ctx.drawImage(img,0,((frame+1)%frameCounts[i])*width,width,width,0,0,64,64);ctx.globalAlpha=1;
const canvas=document.getElementById(ids[i]),out=canvas.getContext('2d');out.imageSmoothingEnabled=false;
for(let y=0;y<canvas.height;y+=64)for(let x=0;x<canvas.width;x+=64)out.drawImage(scratch,x,y);
});seek.value=Math.floor(position);document.getElementById('time').textContent=(position/1000).toFixed(1)+' / 6.4 s';requestAnimationFrame(draw)}
requestAnimationFrame(draw);
</script></html>
'@
    $html = $html.Replace('%STILL%', [Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $OutputDirectory 'block/star_touched_lava_still.png'))))
    $html = $html.Replace('%FLOW%', [Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $OutputDirectory 'block/star_touched_lava_flow.png'))))
    [IO.File]::WriteAllText((Join-Path $ReportDirectory 'preview.html'), $html, [Text.UTF8Encoding]::new($false))
    Write-Output "Preview: $(Join-Path $ReportDirectory 'preview.png')"
} finally {
    foreach ($bitmap in @($source, $stillSource, $tile, $glint, $vanillaBucket, $still, $flow, $bucket)) {
        if ($null -ne $bitmap) { $bitmap.Dispose() }
    }
    $archive.Dispose()
}
