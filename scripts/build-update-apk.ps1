param(
    [string]$VersionName
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not $env:JAVA_HOME) {
    $studioJbr = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path $studioJbr) {
        $env:JAVA_HOME = $studioJbr
    }
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    throw "JAVA_HOME is not configured. Install Android Studio/JDK and set JAVA_HOME."
}

$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$now = Get-Date
$versionCode = (($now.Year - 2000) * 10000000) + ($now.DayOfYear * 10000) + ($now.Hour * 100) + $now.Minute
$resolvedVersionName = if ([string]::IsNullOrWhiteSpace($VersionName)) {
    Get-Date -Format "yyyy.MM.dd-HH.mm"
} else {
    $VersionName
}

Write-Host "Building update APK with versionCode=$versionCode versionName=$resolvedVersionName"

& .\gradlew.bat :app:assembleDebug "-PappVersionCode=$versionCode" "-PappVersionName=$resolvedVersionName"

Copy-Item -Force "app\build\outputs\apk\debug\app-debug.apk" "app-debug.apk"

Write-Host "APK ready: $root\app-debug.apk"
Write-Host "Install/update with: adb install -r $root\app-debug.apk"
