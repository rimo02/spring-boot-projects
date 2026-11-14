$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$envFile = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw 'Create .env from .env.example and configure DB_PASSWORD and JWT_SECRET first.'
}

# Make Docker and Java use exactly the same local settings, including when the
# terminal inherited older DB_URL or REDIS_PORT values. Never print secrets.
foreach ($line in Get-Content -LiteralPath $envFile) {
    $line = $line.Trim()
    if ($line -and -not $line.StartsWith('#')) {
        $pair = $line -split '=', 2
        if ($pair.Count -ne 2 -or $pair[0] -notmatch '^[A-Z][A-Z0-9_]*$') {
            throw 'Invalid .env entry. Use NAME=value, one setting per line.'
        }
        [Environment]::SetEnvironmentVariable($pair[0], $pair[1], 'Process')
    }
}
foreach ($name in @('DB_PASSWORD', 'DB_URL', 'POSTGRES_PORT', 'REDIS_PORT', 'JWT_SECRET')) {
    if (-not [Environment]::GetEnvironmentVariable($name, 'Process')) {
        throw "Missing required setting: $name"
    }
}

if ($env:REDIS_PORT -ne '6380') {
    throw 'This local Compose setup publishes Redis on 6380. Set REDIS_PORT=6380 in .env.'
}
Write-Host "Starting PostgreSQL on $env:POSTGRES_PORT and Redis on $env:REDIS_PORT..."
docker compose --file (Join-Path $projectRoot 'compose.yaml') --env-file $envFile up -d --wait
if ($LASTEXITCODE -ne 0) { throw 'Docker services could not start. Check Docker Desktop and the selected ports.' }

if (-not $env:PORT) { $env:PORT = '8084' }
Write-Host "Starting Banking API. Swagger: http://localhost:$env:PORT/swagger-ui.html"
mvn spring-boot:run
if ($LASTEXITCODE -ne 0) { throw 'Application startup failed; see the Maven output above.' }
