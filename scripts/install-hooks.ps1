$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    git config --local core.hooksPath .githooks
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron configurar hooks.' }
    git config --local core.autocrlf false
    git config --local core.eol lf
    git config --local pull.ff only
    git config --local fetch.prune true
    git config --local push.default simple
    git config --local push.autoSetupRemote true
    Write-Output 'Hooks y opciones locales instalados. Identidad, credenciales y firma pertenecen a cada integrante.'
} finally { Pop-Location }
