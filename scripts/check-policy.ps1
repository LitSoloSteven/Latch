param(
    [string]$CommitMessageFile,
    [string]$EventPath,
    [string]$BaseSha,
    [string]$HeadSha
)
$ErrorActionPreference = 'Stop'

function Test-CommitMessage([string]$Message) {
    $lines = ($Message -replace "`r", '') -split "`n"
    $header = $lines[0]
    if ($header -notmatch '^(feat|fix|test|docs|build)(\(M(?:0[1-9]|1[0-2])\))?(!)?: .+ \((TAR|DEF)-[0-9]{3}\)$') {
        throw "Cabecera invalida: $header"
    }
    $reference = [regex]::Match($header, '(TAR|DEF)-[0-9]{3}\)$').Value.TrimEnd(')')
    if ($Message -notmatch "(?m)^Refs: $reference\r?$") { throw "Falta Refs: $reference" }
    if ($lines.Count -lt 3 -or $lines[1] -ne '') { throw 'Separa cabecera y cuerpo con una linea vacia.' }
    if ($header -notmatch '\(M(?:0[1-9]|1[0-2])\)' -and $Message -notmatch '(?m)^Module: M(?:0[1-9]|1[0-2])\r?$') {
        throw 'Falta Module: M01..M12.'
    }
    if ($header -match '!:' -and $Message -notmatch '(?m)^BREAKING CHANGE: .+') { throw 'Falta BREAKING CHANGE.' }
    if ($Message -match '(?i)\b(close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#\d+') { throw 'Usa Refs; el cierre requiere aceptacion D47.' }
}

if ($CommitMessageFile) {
    Test-CommitMessage (Get-Content -LiteralPath $CommitMessageFile -Raw -Encoding utf8)
    Write-Output 'Mensaje valido.'
    exit 0
}

if (!$EventPath) { throw 'Indica CommitMessageFile o EventPath.' }
$eventData = Get-Content -LiteralPath $EventPath -Raw -Encoding utf8 | ConvertFrom-Json
$pr = $eventData.pull_request
if (!$pr) { throw 'El evento no contiene una pull request.' }
$branchPattern = (Get-Content (Join-Path $PSScriptRoot '../config/branch-regex.txt') -Raw).Trim()
if ($pr.head.ref -notmatch $branchPattern) { throw "Rama invalida: $($pr.head.ref)" }
$branchId = [regex]::Match($pr.head.ref, '(TAR|DEF)-[0-9]{3}').Value
if ($pr.title -notmatch "\($branchId\)$") { throw 'El titulo debe conservar el identificador de la rama.' }
if ($pr.base.ref -ne 'main') { throw 'Esta adopcion usa main; candidatas y hotfix siguen pendientes de enmienda.' }
if ($pr.body -notmatch '(?im)^Refs\s+#\d+\s*$') { throw 'Falta Refs #numero con el issue real en el cuerpo de la PR.' }
$squash = [regex]::Match($pr.body, '(?s)<!-- squash-message:start -->\s*```(?:text)?\s*\r?\n(.*?)\r?\n```\s*<!-- squash-message:end -->')
if (!$squash.Success) { throw 'Falta el bloque Squash message de la plantilla.' }
$squashMessage = $squash.Groups[1].Value.Trim()
Test-CommitMessage $squashMessage
if (($squashMessage -split '\r?\n')[0] -cne $pr.title) { throw 'El titulo y la cabecera del squash deben coincidir.' }
if ($pr.body -match '(?i)\b(close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#\d+') {
    throw 'La adopcion inicial usa Refs; cierra el issue manualmente tras D47.'
}
if (!$BaseSha) { $BaseSha = $pr.base.sha }
if (!$HeadSha) { $HeadSha = $pr.head.sha }
$commits = @(git rev-list "$BaseSha..$HeadSha")
if ($LASTEXITCODE -ne 0 -or !$commits.Count) { throw 'No se pudieron obtener los commits del cambio.' }
foreach ($commit in $commits) {
    $parents = (git rev-list --parents -n 1 $commit) -split ' '
    if ($parents.Count -gt 2) { throw 'Actualiza la rama propia con rebase antes de revisar; no mezcles merges.' }
    $message = (git log -1 --format=%B $commit) -join "`n"
    Test-CommitMessage $message
}
Write-Output "Politica valida: $($commits.Count) commits; $($pr.head.ref). La evidencia y el mensaje final del merge requieren revision humana."
