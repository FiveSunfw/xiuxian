Add-Type -AssemblyName System.Drawing
$project=Split-Path $PSScriptRoot
$catalog=(& node -e "console.log(JSON.stringify(require('./scripts/manual-catalog.cjs')()))" | ConvertFrom-Json)
$directory=Join-Path $project 'src/main/resources/assets/xiuxian/textures/item/manuals'
foreach($manual in $catalog){
 if($manual.id -eq $manual.family){continue}
 $source=Join-Path $directory ($manual.family+'.png')
 $image=[System.Drawing.Bitmap]::new($source)
 $canvas=[System.Drawing.Bitmap]::new(32,32)
 $graphics=[System.Drawing.Graphics]::FromImage($canvas)
 $graphics.DrawImageUnscaled($image,0,0)
 $cover=[System.Drawing.SolidBrush]::new($image.GetPixel(15,5))
 $paper=[System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#d9cca8'))
 $dark=[System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#8c7959'))
 if($manual.coverage -lt 9){
  $graphics.FillRectangle($paper,19,24,7,4)
  $graphics.FillRectangle($dark,20,25,4,1)
  if($manual.coverage -eq 6){$graphics.FillRectangle($paper,10,26,5,3);$graphics.FillRectangle($dark,11,27,3,1)}
 }else{
  $graphics.FillRectangle($cover,20,24,7,4)
  $graphics.FillRectangle($paper,9,28,17,1)
  $stamp=[System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#586497'))
  $graphics.FillRectangle($stamp,22,23,4,4);$graphics.FillRectangle($paper,23,24,2,1);$stamp.Dispose()
 }
 $canvas.Save((Join-Path $directory ($manual.id+'.png')),[System.Drawing.Imaging.ImageFormat]::Png)
 $graphics.Dispose();$canvas.Dispose();$image.Dispose();$cover.Dispose();$paper.Dispose();$dark.Dispose()
}
Write-Output "Prepared $($catalog.Count) manual textures."
