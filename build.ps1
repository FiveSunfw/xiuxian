param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GradleArgs)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$jdk = Get-ChildItem -LiteralPath (Join-Path $projectRoot '.tools') -Directory -Filter 'jdk-17*' | Select-Object -First 1
if (!$jdk) { throw 'Extract a Java 17 JDK into .tools before building.' }
$env:JAVA_HOME = $jdk.FullName
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-cache'
Push-Location $projectRoot
try {
    & (Join-Path $projectRoot 'gradlew.bat') @GradleArgs
    $result = $LASTEXITCODE
} finally { Pop-Location }
exit $result
