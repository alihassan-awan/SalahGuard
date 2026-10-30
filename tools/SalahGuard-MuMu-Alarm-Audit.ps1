param(
    [string]$AdbPath = 'D:\Program Files\Netease\MuMuPlayer\nx_device\15.0\shell\adb.exe',
    [string]$Device = '127.0.0.1:16384',
    [string]$Package = 'com.alihassanawan.salahguard',
    [string]$ApkPath = ''
)

$ErrorActionPreference = 'Continue'
if (-not (Test-Path -LiteralPath $AdbPath)) {
    Write-Error "ADB not found: $AdbPath"
    exit 1
}

function Adb([string[]]$Arguments) {
    & $script:AdbPath -s $script:Device @Arguments 2>&1 | Out-String
}

Write-Host "ADB: $AdbPath"
& $AdbPath start-server | Out-Host
& $AdbPath connect $Device | Out-Host
$devices = & $AdbPath devices -l | Out-String
Write-Host $devices
if ($devices -notmatch [regex]::Escape($Device) + '\s+device\b') {
    Write-Error "No authorized Android device at $Device. Check MuMu ADB local connection."
    exit 2
}

if ($ApkPath.Trim()) {
    if (-not (Test-Path -LiteralPath $ApkPath)) { Write-Error "APK missing: $ApkPath"; exit 3 }
    Write-Host "Installing APK (preserving existing data)..."
    $install = Adb @('install', '-r', $ApkPath)
    Write-Host $install
    if ($install -notmatch 'Success') { Write-Error 'APK install failed; aborting audit.'; exit 4 }
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$folder = Join-Path $PSScriptRoot "reports\alarm-audit-$stamp"
New-Item -ItemType Directory -Path $folder -Force | Out-Null

$sections = [ordered]@{
  'device.txt'        = @('shell','getprop','ro.build.version.release')
  'package.txt'       = @('shell','dumpsys','package',$Package)
  'alarms.txt'        = @('shell','dumpsys','alarm')
  'notifications.txt' = @('shell','dumpsys','notification','--noredact')
  'activity.txt'      = @('shell','dumpsys','activity','activities')
  'location.txt'      = @('shell','dumpsys','location')
  'appops.txt'        = @('shell','cmd','appops','get',$Package)
  'clock.txt'         = @('shell','date')
}
foreach ($name in $sections.Keys) {
  $content = Adb ([string[]]$sections[$name])
  Set-Content -Path (Join-Path $folder $name) -Value $content -Encoding UTF8
}
$log = Adb @('logcat','-d','-v','time','-t','3000')
Set-Content -Path (Join-Path $folder 'logcat-last3000.txt') -Value $log -Encoding UTF8

$pkg = Get-Content (Join-Path $folder 'package.txt') -Raw
$alarm = Get-Content (Join-Path $folder 'alarms.txt') -Raw
$noti = Get-Content (Join-Path $folder 'notifications.txt') -Raw
$summary = @(
    "SalahGuard MuMu 15 read-only alarm audit"
    "Captured: $(Get-Date -Format o)"
    "Device: $Device"
    "Package: $Package"
    "Android: $((Get-Content (Join-Path $folder 'device.txt') -Raw).Trim())"
    "Installed package found: $($pkg -match [regex]::Escape($Package))"
    "Alarm references found: $(([regex]::Matches($alarm, [regex]::Escape($Package))).Count)"
    "Notification references found: $(([regex]::Matches($noti, [regex]::Escape($Package))).Count)"
    ""
    "Related scheduled-alarm lines (context/details in alarms.txt):"
)
$matched = $alarm -split "`r?`n" | Where-Object { $_ -match 'SalahGuardPrayerReceiver|com\.alihassanawan\.salahguard|RTC_WAKEUP|9400[0-4]' } | Select-Object -First 80
$summary += $matched
$summary += ""
$summary += 'This is a snapshot, not proof that an alarm actually fired.'
$summary += 'Do not send full logs publicly: they can include other apps and device information.'
$summary | Set-Content -Path (Join-Path $folder 'SUMMARY.txt') -Encoding UTF8
Write-Host "`nAudit saved: $folder" -ForegroundColor Green
Write-Host 'Share SUMMARY.txt first; additional files only if needed.'
