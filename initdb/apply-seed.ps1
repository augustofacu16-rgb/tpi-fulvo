<#
.SYNOPSIS
    Aplica el seeder SQL de los 3 torneos de prueba para TPI Fulvo.
.DESCRIPTION
    Inserta:
    1) Torneo Liga Iniciado (10 equipos cupos completos, 45 partidos Round Robin generados).
    2) Torneo Liga Abierto a Inscripción (Cupo 10, 3 equipos admitidos, 7 vacantes, 1 solicitud pendiente).
    3) Torneo Llaves Iniciado (8 equipos cupos completos, 7 partidos Cuartos/Semi/Final generados).
#>

param(
    [string]$HostName = "localhost",
    [string]$Port = $env:DB_PORT_HOST,
    [string]$DbName = "fulvo_db",
    [string]$User = "postgres"
)

if (-not $Port) { $Port = "5432" }

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " TPI Fulvo - Carga de Torneos de Prueba (Seed SQL)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$sqlFile = Join-Path $scriptDir "seed-torneos-test.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Error "No se encontro el archivo $sqlFile"
    exit 1
}

# 1. Intentar aplicar via Docker si el contenedor postgres-db esta activo
$dockerRunning = $false
try {
    $containerStatus = docker inspect -f '{{.State.Running}}' postgres-db 2>$null
    if ($containerStatus -eq "true") {
        $dockerRunning = $true
    }
} catch {
    $dockerRunning = $false
}

if ($dockerRunning) {
    Write-Host "[Docker] Detectado contenedor 'postgres-db' en ejecucion. Aplicando seed..." -ForegroundColor Green
    Get-Content $sqlFile | docker exec -i postgres-db psql -U $User -d $DbName
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[Docker] Seed aplicado exitosamente en postgres-db!" -ForegroundColor Green
        exit 0
    } else {
        Write-Warning "[Docker] Fallo al ejecutar via docker exec. Intentando via psql local..."
    }
}

# 2. Intentar aplicar via psql local
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlPath) {
    Write-Host "[Local] Ejecutando psql en $HostName`:$Port..." -ForegroundColor Yellow
    & psql -h $HostName -p $Port -U $User -d $DbName -f $sqlFile
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[Local] Seed aplicado exitosamente!" -ForegroundColor Green
        exit 0
    }
}

Write-Host "----------------------------------------------------------" -ForegroundColor Yellow
Write-Host "INSTRUCCION MANUAL:" -ForegroundColor Yellow
Write-Host "Si tienes DBeaver, pgAdmin o tu terminal psql abierta, simplemente ejecuta el contenido de:" -ForegroundColor White
Write-Host "  $sqlFile" -ForegroundColor Cyan
Write-Host "O en terminal con Docker:" -ForegroundColor White
Write-Host "  Get-Content initdb\seed-torneos-test.sql | docker exec -i postgres-db psql -U postgres -d fulvo_db" -ForegroundColor Cyan
Write-Host "----------------------------------------------------------" -ForegroundColor Yellow
