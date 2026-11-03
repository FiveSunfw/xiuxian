param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GradleArgs)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$localTools = Join-Path $projectRoot '.tools'
$jdk = if (Test-Path -LiteralPath $localTools) {
    Get-ChildItem -LiteralPath $localTools -Directory -Filter 'jdk-17*' | Select-Object -First 1
}
if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
elseif (!$env:JAVA_HOME) { throw 'Java 17 is required: extract a JDK into .tools or set JAVA_HOME.' }
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-cache'
Push-Location $projectRoot
try {
    & (Join-Path $projectRoot 'gradlew.bat') @GradleArgs
    $result = $LASTEXITCODE
} finally { Pop-Location }
exit $result
