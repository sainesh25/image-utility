# PowerShell script to download WebP ImageIO library
# Run this script from the project root directory

$libDir = "src\main\webapp\WEB-INF\lib"
$jarFile = "$libDir\webp-imageio-core-0.4.4.jar"

# Create lib directory if it doesn't exist
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir -Force | Out-Null
}

Write-Host "Downloading webp-imageio-core library..."

# Try multiple sources for the WebP library
$sources = @(
    "https://github.com/gotson/webp-imageio/releases/download/v0.4.4/webp-imageio-core-0.4.4.jar",
    "https://repo1.maven.org/maven2/com/github/gotson/webp-imageio-core/0.4.4/webp-imageio-core-0.4.4.jar"
)

$downloaded = $false
foreach ($url in $sources) {
    try {
        Write-Host "Trying: $url"
        Invoke-WebRequest -Uri $url -OutFile $jarFile -TimeoutSec 30 -ErrorAction Stop
        Write-Host "Successfully downloaded webp-imageio-core-0.4.4.jar" -ForegroundColor Green
        $downloaded = $true
        break
    } catch {
        Write-Host "Failed to download from $url" -ForegroundColor Yellow
        Write-Host $_.Exception.Message
    }
}

if (-not $downloaded) {
    Write-Host "`nAutomatic download failed. Please manually download the library:" -ForegroundColor Red
    Write-Host "1. Visit: https://github.com/gotson/webp-imageio/releases" -ForegroundColor Cyan
    Write-Host "2. Download: webp-imageio-core-0.4.4.jar" -ForegroundColor Cyan
    Write-Host "3. Place it in: $libDir" -ForegroundColor Cyan
    Write-Host "`nOr use Maven to download:" -ForegroundColor Yellow
    Write-Host "mvn dependency:copy -Dartifact=com.github.gotson:webp-imageio-core:0.4.4 -DoutputDirectory=$libDir" -ForegroundColor Cyan
}

