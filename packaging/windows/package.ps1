[CmdletBinding()]
param(
    [ValidateSet('app-image','exe','msi')][string]$Type = 'app-image',
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$MavenPath,
    [string]$AppVersion,
    [string]$Destination,
    [switch]$RunMySqlIntegration,
    [string]$IntegrationConfig,
    [switch]$CheckOnly
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-Checked {
    param([string]$Executable,[string[]]$Arguments)
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Tool failed with exit code ${LASTEXITCODE}: $Executable" }
}

if ($env:OS -ne 'Windows_NT') { throw 'Build Windows packages on Windows.' }
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if ([string]::IsNullOrWhiteSpace($AppVersion)) {
    [xml]$projectPom = Get-Content -LiteralPath (Join-Path $project 'pom.xml') -Raw
    $AppVersion = ([string]$projectPom.project.version) -replace '-SNAPSHOT$', ''
}
if ($AppVersion -notmatch '^\d+\.\d+\.\d+$') { throw 'Use a three-part numeric application version, such as 1.0.0.' }

if ([string]::IsNullOrWhiteSpace($JdkHome)) { throw 'Set JAVA_HOME or pass -JdkHome with a JDK 21 installation.' }
$jdk = [IO.Path]::GetFullPath($JdkHome)
foreach ($tool in @('java.exe','jar.exe','jlink.exe','jpackage.exe')) {
    if (-not (Test-Path -LiteralPath (Join-Path $jdk "bin/$tool") -PathType Leaf)) { throw "JDK tool missing: $tool" }
}
$release = Join-Path $jdk 'release'
if (-not (Test-Path -LiteralPath $release -PathType Leaf)) { throw 'The JDK release file is missing.' }
$versionLine = Get-Content -LiteralPath $release | Where-Object { $_ -match '^JAVA_VERSION=' }
if ($versionLine -notmatch '^JAVA_VERSION="21(?:[.+-][^"]*)?"$') { throw 'Packaging requires JDK 21; a newer JDK must not be bundled as a substitute.' }
$java = Join-Path $jdk 'bin/java.exe'
$actualVersion = & $java --version
if ($LASTEXITCODE -ne 0 -or (($actualVersion -join ' ') -notmatch '\b21(?:[. +\-]|$)')) { throw 'The selected java executable does not report Java 21.' }
if ([string]::IsNullOrWhiteSpace($MavenPath)) {
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($null -eq $maven) { $maven = Get-Command mvn -ErrorAction SilentlyContinue }
    if ($null -eq $maven) { throw 'Maven 3.9.6+ is required. Add it to PATH or pass -MavenPath.' }
    $MavenPath = $maven.Source
}
if (-not (Test-Path -LiteralPath $MavenPath -PathType Leaf)) { throw 'The Maven executable path is invalid.' }
if ($RunMySqlIntegration) {
    if ([string]::IsNullOrWhiteSpace($IntegrationConfig) -or -not (Test-Path -LiteralPath $IntegrationConfig -PathType Leaf)) { throw '-RunMySqlIntegration requires an external -IntegrationConfig file.' }
    $IntegrationConfig = [IO.Path]::GetFullPath($IntegrationConfig)
    # Maven's Windows .cmd launcher invokes cmd.exe. Keep its file argument literal.
    if ($IntegrationConfig.IndexOfAny([char[]]'&|<>^%!"`') -ge 0 -or $IntegrationConfig -match '[\r\n]') { throw 'Use an integration-config path without cmd.exe metacharacters.' }
}
if ($Type -ne 'app-image') {
    foreach ($tool in @('candle.exe','light.exe')) {
        $wix = Get-Command $tool -ErrorAction SilentlyContinue
        if ($null -eq $wix) { throw "JDK 21 installers require WiX 3.x ($tool on PATH). Use -Type app-image without WiX." }
        $major = (Get-Item -LiteralPath $wix.Source).VersionInfo.FileMajorPart
        if ($major -ne 3) { throw 'Use WiX 3.x with JDK 21 packaging.' }
    }
}
if ($CheckOnly) { Write-Output 'Packaging prerequisites passed. No build, database test or package was started.'; return }

$run = [guid]::NewGuid().ToString('N')
$work = Join-Path $project "target/jpackage/$run"
if ([string]::IsNullOrWhiteSpace($Destination)) { $Destination = Join-Path $project "dist/Queue-Clearance-$AppVersion-$($run.Substring(0,8))" }
$resultDirectory = [IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $resultDirectory) { throw 'Destination already exists. Choose a new directory; existing packages are never deleted or overwritten.' }
$previousJavaHome = $env:JAVA_HOME
Push-Location -LiteralPath $project
try {
    $env:JAVA_HOME = $jdk
    $mavenArguments = @('clean','verify')
    if ($RunMySqlIntegration) { $mavenArguments += @('-Pmysql-it',"-Dqueueclearance.it.config=$IntegrationConfig") }
    Invoke-Checked $MavenPath $mavenArguments
    # Maven clean has now finished. Only this run's staging directory is created.
    $inputDirectory = Join-Path $work 'input'
    $runtimeDirectory = Join-Path $work 'runtime'
    $imageDestination = Join-Path $work 'image'
    New-Item -ItemType Directory -Path (Join-Path $inputDirectory 'lib') -Force | Out-Null
    $jar = Join-Path $project 'target/queue-clearance.jar'
    if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) { throw 'Verified application JAR is missing.' }
    $entries = & (Join-Path $jdk 'bin/jar.exe') tf $jar
    if ($LASTEXITCODE -ne 0 -or ($entries -contains 'app.properties')) { throw 'Cannot inspect JAR, or a live app.properties was included.' }
    foreach ($required in @('app/PackagedLauncher.class','app/InstallationCheck.class','app.properties.example','logback.xml')) {
        if ($entries -notcontains $required) { throw "Required packaged resource missing: $required" }
    }
    Copy-Item -LiteralPath $jar -Destination (Join-Path $inputDirectory 'queue-clearance.jar')
    $libraries = @(Get-ChildItem -LiteralPath (Join-Path $project 'target/lib') -Filter '*.jar' -File)
    foreach ($pattern in @('HikariCP-*','mysql-connector-j-*','flatlaf-*','openpdf-*','poi-ooxml-*','jbcrypt-*','logback-classic-*','log4j-to-slf4j-*')) {
        if (@($libraries | Where-Object { $_.Name -like $pattern }).Count -eq 0) { throw "Missing runtime library: $pattern" }
    }
    foreach ($library in $libraries) { Copy-Item -LiteralPath $library.FullName -Destination (Join-Path $inputDirectory 'lib') }
    Invoke-Checked (Join-Path $jdk 'bin/jlink.exe') @('--add-modules','java.se,jdk.crypto.ec,jdk.crypto.mscapi,jdk.charsets,jdk.unsupported,jdk.zipfs','--strip-debug','--no-header-files','--no-man-pages','--output',$runtimeDirectory)
    Invoke-Checked (Join-Path $jdk 'bin/jpackage.exe') @('--type','app-image','--name','Queue-Clearance','--app-version',$AppVersion,'--vendor','Queue-Clearance','--description','Campus queue and student clearance','--input',$inputDirectory,'--main-jar','queue-clearance.jar','--main-class','app.PackagedLauncher','--runtime-image',$runtimeDirectory,'--java-options','-Dfile.encoding=UTF-8','--dest',$imageDestination)
    $image = Join-Path $imageDestination 'Queue-Clearance'
    Invoke-Checked (Join-Path $image 'runtime/bin/java.exe') @('-Djava.awt.headless=true',"-Dqueueclearance.logDir=$(Join-Path $work 'check-logs')",'-cp',(Join-Path $image 'app/queue-clearance.jar'),'app.InstallationCheck')
    New-Item -ItemType Directory -Path $resultDirectory | Out-Null
    if ($Type -eq 'app-image') {
        Copy-Item -LiteralPath $image -Destination (Join-Path $resultDirectory 'Queue-Clearance') -Recurse
    } else {
        Invoke-Checked (Join-Path $jdk 'bin/jpackage.exe') @('--type',$Type,'--name','Queue-Clearance','--app-version',$AppVersion,'--app-image',$image,'--dest',$resultDirectory,'--win-per-user-install','--win-menu','--win-menu-group','Queue-Clearance','--win-shortcut','--win-dir-chooser','--win-upgrade-uuid','406cfac8-d62d-4c7b-8e41-4308fe934531')
    }
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'README-FIRST.txt') -Destination $resultDirectory
    $files = @(Get-ChildItem -LiteralPath $resultDirectory -File -Recurse | Sort-Object FullName | ForEach-Object {
        [ordered]@{path=$_.FullName.Substring($resultDirectory.Length+1).Replace('\','/');sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant();bytes=$_.Length}
    })
    [ordered]@{application='Queue-Clearance';version=$AppVersion;type=$Type;java=($actualVersion -join ' ');mainClass='app.PackagedLauncher';files=$files} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $resultDirectory 'manifest.json') -Encoding UTF8
    Write-Output "Package created and runtime check passed: $resultDirectory"
    Write-Output 'Native GUI, campus TLS, printing and upgrade tests still need the target Windows workstation.'
} finally {
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
