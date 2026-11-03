Add-Type -AssemblyName System.Drawing
$root=Join-Path $PSScriptRoot '../src/main/resources/assets/xiuxian/textures'
$preview=Join-Path $PSScriptRoot '../build/previews'
New-Item -ItemType Directory -Force "$root/item/manuals","$root/gui/manuals",$preview | Out-Null
$rows=@(
@('basic_sword','基础养剑诀','#526C68','sword',1),@('breeze_sword','清风剑经','#79A99A','breeze',2),@('stone_sword','磐石剑经','#766B57','stone',2),@('river_sword','长河剑经','#3D7696','river',2),@('piercing_sword','破锋剑诀','#383E48','pierce',3),@('returning_sword','回澜剑经','#317C80','return',3),@('lone_sword','孤锋剑典·炼气篇','#242A37','lone',1),@('radiant_sword_fragment','流光剑典·残篇','#343C75','radiant',1),
@('metal_origin','金元诀','#84613A','metal',1),@('greenwood','青木诀','#366A4C','wood',1),@('dark_water','玄水诀','#31557D','water',1),@('scarlet_fire','赤火诀','#923D32','fire',1),@('thick_earth','厚土诀','#997B45','earth',1),@('frost','凝霜诀','#75A6B4','ice',1),@('thunder','引雷诀','#665083','thunder',1),@('wind','御风诀','#70A398','wind',1),
@('water_fire','水火相济经','#3C5368','waterfire',3),@('metal_wood','金木交生经','#3F624B','metalwood',3),@('wood_fire','木火生息经','#744A3D','woodfire',3),@('earth_metal','土金凝元经','#716047','earthmetal',3),@('wind_thunder','风雷引','#47496E','windthunder',1),@('ice_water','冰水归流经','#356780','icewater',1))
function Brush($color){New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($color))}
function Box($x,$y,$w,$h,$c){$b=Brush $c;$g.FillRectangle($b,[single]$x,[single]$y,[single]$w,[single]$h);$b.Dispose()}
function Line($points,$c,$width=1){$pen=New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($c)),([single]$width);for($i=0;$i -lt $points.Count-1;$i++){$g.DrawLine($pen,[single]$points[$i][0],[single]$points[$i][1],[single]$points[$i+1][0],[single]$points[$i+1][1])};$pen.Dispose()}
function Glyph($kind){
 switch -Regex ($kind){
 'wood' {Line @(@(15,23),@(15,16),@(10,12),@(8,12)) '#9bd27e';Line @(@(15,17),@(20,13),@(22,13)) '#9bd27e';Box 10 13 3 2 '#c2df8a';Box 18 14 3 2 '#a4d995'}
 'water|river|return' {Line @(@(8,21),@(11,19),@(15,21),@(19,19),@(22,21)) '#b3e1ee';Line @(@(8,24),@(12,22),@(17,24),@(22,22)) '#8ec4de'}
 'fire' {Line @(@(10,20),@(9,17),@(13,18),@(16,10),@(17,16),@(20,14),@(21,20),@(16,23),@(10,20)) '#f6b65b';Box 14 18 3 4 '#ffe1a0'}
 'metal|pierce' {Line @(@(9,22),@(13,13),@(14,20)) '#efce76';Line @(@(17,20),@(19,12),@(21,22)) '#f8e5ab'}
 'earth|stone' {Line @(@(8,21),@(11,16),@(14,19),@(17,14),@(22,21),@(8,21)) '#d4bc88';Line @(@(9,24),@(21,24)) '#aa9879'}
 'ice|frost' {Line @(@(15,11),@(15,24)) '#e2f6ff';Line @(@(9,14),@(21,21)) '#e2f6ff';Line @(@(9,21),@(21,14)) '#e2f6ff';Box 13 15 5 5 '#c2e6ee'}
 'thunder' {Line @(@(18,10),@(12,17),@(17,17),@(12,24)) '#e9e0ff' 2}
 'wind|breeze' {Line @(@(8,15),@(19,15),@(21,13),@(19,11)) '#d4ece3';Line @(@(8,19),@(21,19),@(23,17)) '#d4ece3';Line @(@(10,23),@(17,23),@(19,21)) '#b7ddd4'}
 }
 if($kind -in @('sword','breeze','stone','river','pierce','return','lone','radiant')){Line @(@(13,24),@(19,11)) '#ecf0ef' 2;Line @(@(11,20),@(17,23)) '#d7bd74';Box 12 24 2 2 '#ba8b50';if($kind -eq 'lone'){Box 13 9 1 3 '#f5e5af';Box 12 10 3 1 '#f5e5af'};if($kind -eq 'radiant'){Line @(@(9,23),@(16,10)) '#eed38b';Line @(@(19,23),@(22,13)) '#d9dffa'}}
}
foreach($r in $rows){
 $bmp=New-Object System.Drawing.Bitmap 32,32;$g=[System.Drawing.Graphics]::FromImage($bmp);$g.Clear([System.Drawing.Color]::Transparent)
 Box 4 3 24 27 '#202725';Box 5 4 22 25 $r[2];Box 5 4 3 25 '#303b3a';Box 8 28 18 2 '#c9bb98';Box 9 28 16 1 '#e4d5af';Box 24 5 2 8 '#e5d8b0'
 for($y=7;$y -le 25;$y+=5){Box 5 $y 4 1 '#c0b18c'}
 for($y=7;$y -le 11;$y+=2){Box 24 $y 1 1 '#665547'}
 Glyph $r[3]
 $high=$r[0] -in @('lone_sword','radiant_sword_fragment','wind_thunder','ice_water')
 Box 22 23 4 4 $(if($high){'#586497'}else{'#c09a53'})
 for($j=0;$j -lt $r[4];$j++){Box 23 (23+$j) 2 1 '#f3dfae'}
 if($r[0] -eq 'radiant_sword_fragment'){Box 20 27 7 3 '#202725';Box 20 24 5 4 '#d9cca8';Box 21 25 3 1 '#a58c6b'}
 $bmp.Save("$root/item/manuals/$($r[0]).png",[System.Drawing.Imaging.ImageFormat]::Png);$g.Dispose();$bmp.Dispose()
}
$sheet=New-Object System.Drawing.Bitmap 1500,950;$g=[System.Drawing.Graphics]::FromImage($sheet);$g.Clear([System.Drawing.ColorTranslator]::FromHtml('#192620'));$font=New-Object System.Drawing.Font 'Microsoft YaHei',13;$title=New-Object System.Drawing.Font 'Microsoft YaHei',23;$ink=Brush '#e7dcc0';$g.DrawString('炼气功法 · 22本物品贴图',$title,$ink,30,18)
$g.InterpolationMode=[System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor;$g.PixelOffsetMode=[System.Drawing.Drawing2D.PixelOffsetMode]::Half
for($i=0;$i -lt $rows.Count;$i++){$r=$rows[$i];$x=30+($i%8)*185;$y=85+[Math]::Floor($i/8)*280;$img=[System.Drawing.Image]::FromFile("$root/item/manuals/$($r[0]).png");$g.DrawImage($img,[int]$x,[int]$y,160,160);$g.DrawString($r[1],$font,$ink,[single]$x,[single]($y+170));$g.DrawImage($img,[int]($x+65),[int]($y+207),32,32);$img.Dispose()}
$sheet.Save("$preview/manual-covers.png",[System.Drawing.Imaging.ImageFormat]::Png);$g.Dispose();$sheet.Dispose()
$rows | ForEach-Object {[PSCustomObject]@{id=$_[0];name=$_[1];texture="xiuxian:item/manuals/$($_[0])"}} | ConvertTo-Json | Set-Content -Encoding UTF8 "$preview/manual-cover-manifest.json"
