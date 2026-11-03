param()
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
$jdk = Get-ChildItem (Join-Path $root '.tools') -Directory -Filter 'jdk-17*' | Select-Object -First 1
if (!$jdk) { throw 'Java 17 is missing from .tools.' }
$outputDir = Join-Path $root 'build/progression-tests'
New-Item -ItemType Directory -Force $outputDir | Out-Null
& (Join-Path $jdk.FullName 'bin/javac.exe') -encoding UTF-8 -d $outputDir (Join-Path $root 'src/main/java/dev/xiuxian/CultivationProgress.java') (Join-Path $root 'src/main/java/dev/xiuxian/CultivationGain.java') (Join-Path $root 'tests/CultivationProgressTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Progression test compilation failed.' }
& (Join-Path $jdk.FullName 'bin/java.exe') -cp $outputDir dev.xiuxian.CultivationProgressTest
exit $LASTEXITCODE

