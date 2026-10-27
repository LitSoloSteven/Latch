param([ValidateSet('Prepare', 'Protect', 'Inspect', 'Project')][string]$Action = 'Inspect')
$ErrorActionPreference = 'Stop'
$repository = 'LitSoloSteven/Latch'
$root = Split-Path $PSScriptRoot -Parent
$env:GIT_TERMINAL_PROMPT = '0'
$credentialOutput = "protocol=https`nhost=github.com`n`n" | git -c credential.interactive=false credential fill
if ($LASTEXITCODE -ne 0) { throw 'Inicia sesion HTTPS con Git Credential Manager antes de configurar GitHub.' }
$credential = @{}
foreach ($line in $credentialOutput) {
    if ($line -match '^([^=]+)=(.*)$') { $credential[$matches[1]] = $matches[2] }
}
if (!$credential['password']) { throw 'Git Credential Manager no devolvio una credencial.' }
$headers = @{ Authorization = 'Bearer ' + $credential['password']; Accept = 'application/vnd.github+json'; 'X-GitHub-Api-Version' = '2022-11-28' }
$credentialOutput = $null
$credential.Clear()

function Invoke-GitHub([string]$Method, [string]$Path, $Body = $null) {
    $request = @{ Method = $Method; Uri = "https://api.github.com/$Path"; Headers = $headers }
    if ($null -ne $Body) {
        $request.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 40 -Compress))
        $request.ContentType = 'application/json; charset=utf-8'
    }
    $response = Invoke-RestMethod @request
    if ($response -is [array]) {
        foreach ($item in $response) { Write-Output $item }
    } else { $response }
}

try {
    $account = Invoke-GitHub GET user
    if ($account.login -ne 'LitSoloSteven') { throw 'Esta configuracion pertenece a LitSoloSteven; no se aplico con otra cuenta.' }
    $repo = Invoke-GitHub GET "repos/$repository"
    if (!$repo.permissions.admin) { throw 'Se requiere administracion del repositorio.' }

    if ($Action -eq 'Prepare') {
        $settings = @{
            description = 'Latch: aplicacion de escritorio JavaFX para gestion de alquileres. Proyecto academico UAM.'
            default_branch = 'main'; has_issues = $true; has_projects = $true
            allow_squash_merge = $true; allow_merge_commit = $true; allow_rebase_merge = $false
            allow_auto_merge = $false; delete_branch_on_merge = $true
            squash_merge_commit_title = 'PR_TITLE'; squash_merge_commit_message = 'PR_BODY'
        }
        $null = Invoke-GitHub PATCH "repos/$repository" $settings
        $null = Invoke-GitHub PUT "repos/$repository/topics" @{ names = @('java','javafx','maven','desktop-app','uam','rental-management') }
        $null = Invoke-GitHub PUT "repos/$repository/actions/permissions/workflow" @{ default_workflow_permissions = 'read'; can_approve_pull_request_reviews = $false }
        $null = Invoke-GitHub PUT "repos/$repository/vulnerability-alerts"
        try {
            $null = Invoke-GitHub PATCH "repos/$repository" @{ security_and_analysis = @{
                secret_scanning = @{ status = 'enabled' }
                secret_scanning_push_protection = @{ status = 'enabled' }
            } }
            Write-Output 'Secret scanning y push protection habilitados.'
        } catch { Write-Warning ('Seguridad: verificar en Settings. ' + $_.Exception.Message) }

        $existingLabels = @(Invoke-GitHub GET "repos/$repository/labels?per_page=100")
        foreach ($label in (Get-Content "$root/config/labels.json" -Raw -Encoding utf8 | ConvertFrom-Json)) {
            $encoded = [Uri]::EscapeDataString($label.name)
            if ($label.name -in $existingLabels.name) {
                $null = Invoke-GitHub PATCH "repos/$repository/labels/$encoded" @{ new_name = $label.name; color = $label.color; description = $label.description }
            } else { $null = Invoke-GitHub POST "repos/$repository/labels" $label }
        }
        Write-Output '47 etiquetas del proyecto sincronizadas; etiquetas previas conservadas.'
        $existingMilestones = @(Invoke-GitHub GET "repos/$repository/milestones?state=all&per_page=100")
        foreach ($milestone in (Get-Content "$root/config/milestones.json" -Raw -Encoding utf8 | ConvertFrom-Json)) {
            $existing = $existingMilestones | Where-Object title -eq $milestone.title | Select-Object -First 1
            if ($existing) { $null = Invoke-GitHub PATCH "repos/$repository/milestones/$($existing.number)" $milestone }
            else { $null = Invoke-GitHub POST "repos/$repository/milestones" $milestone }
        }
        Write-Output '16 milestones sincronizados con las fechas del Plan.'
        $collaborators = @(Invoke-GitHub GET "repos/$repository/collaborators?per_page=100")
        $invitations = @(Invoke-GitHub GET "repos/$repository/invitations?per_page=100")
        foreach ($login in @('CTardencilla','Arisgav','wetalff')) {
            $null = Invoke-GitHub GET "users/$login"
            if ($login -in $collaborators.login) { Write-Output "$login ya tiene acceso."; continue }
            if ($login -in $invitations.invitee.login) { Write-Output "$login tiene invitacion pendiente."; continue }
            $null = Invoke-GitHub PUT "repos/$repository/collaborators/$login" @{ permission = 'push' }
            Write-Output "Invitacion enviada a $login con escritura."
        }
    }

    if ($Action -eq 'Protect') {
        $rulesets = @(Invoke-GitHub GET "repos/$repository/rulesets?per_page=100")
        foreach ($file in @('main-ruleset.json','tags-immutable-ruleset.json','tags-create-ruleset.json')) {
            $ruleset = Get-Content "$root/config/$file" -Raw -Encoding utf8 | ConvertFrom-Json
            if ($file -eq 'main-ruleset.json') {
                $pulls = @(Invoke-GitHub GET "repos/$repository/pulls?state=open&head=LitSoloSteven:build/TAR-004-configurar-repositorio")
                if (!$pulls.Count) { throw 'Abre primero la PR de configuracion.' }
                $checks = @(Invoke-GitHub GET "repos/$repository/commits/$($pulls[0].head.sha)/check-runs?per_page=100")
                foreach ($name in @('ci','policy')) {
                    $latest = $checks.check_runs | Where-Object { $_.name -eq $name -and $_.app.slug -eq 'github-actions' } | Sort-Object started_at -Descending | Select-Object -First 1
                    if (!$latest -or $latest.conclusion -ne 'success') { throw "El check $name debe aprobar el ultimo SHA antes de exigirlo." }
                }
            }
            $existing = $rulesets | Where-Object name -eq $ruleset.name | Select-Object -First 1
            if ($existing) { $result = Invoke-GitHub PUT "repos/$repository/rulesets/$($existing.id)" $ruleset }
            else { $result = Invoke-GitHub POST "repos/$repository/rulesets" $ruleset }
            Write-Output "$($result.name): $($result.enforcement), id=$($result.id)"
        }
    }

    if ($Action -eq 'Project') {
        $query = 'query { viewer { login id projectsV2(first: 50) { nodes { id number title url } } } }'
        $response = Invoke-GitHub POST graphql @{ query = $query }
        if ($response.errors) { throw ($response.errors.message -join '; ') }
        $project = $response.data.viewer.projectsV2.nodes | Where-Object title -eq 'Latch - Plan de implementacion' | Select-Object -First 1
        if (!$project) {
            $response = Invoke-GitHub POST graphql @{ query = 'mutation($owner: ID!) { createProjectV2(input: {ownerId: $owner, title: "Latch - Plan de implementacion"}) { projectV2 { id number title url } } }'; variables = @{ owner = $response.data.viewer.id } }
            if ($response.errors) { throw ($response.errors.message -join '; ') }
            $project = $response.data.createProjectV2.projectV2
        }
        Write-Output ($project | ConvertTo-Json -Compress)
        $project | ConvertTo-Json | Set-Content "$root/.local/project.json" -Encoding utf8
    }

    if ($Action -eq 'Inspect') {
        $repo | Select-Object full_name,default_branch,allow_squash_merge,allow_merge_commit,allow_rebase_merge,allow_auto_merge,delete_branch_on_merge,security_and_analysis
        Write-Output ($repo.security_and_analysis | ConvertTo-Json -Depth 5 -Compress)
        Invoke-GitHub GET "repos/$repository/collaborators?per_page=100" | ForEach-Object { $_ | Select-Object login,role_name }
        Invoke-GitHub GET "repos/$repository/invitations?per_page=100" | ForEach-Object { $_ | Select-Object id,@{n='login';e={$_.invitee.login}},permissions }
        Invoke-GitHub GET "repos/$repository/rulesets?per_page=100" | ForEach-Object { $_ | Select-Object id,name,enforcement }
        $remoteLabels = @(Invoke-GitHub GET "repos/$repository/labels?per_page=100")
        $expectedLabels = Get-Content "$root/config/labels.json" -Raw -Encoding utf8 | ConvertFrom-Json
        $missing = @($expectedLabels | Where-Object { $_.name -notin $remoteLabels.name })
        Write-Output ('Etiquetas totales: ' + $remoteLabels.Count + '; del proyecto: ' + ($expectedLabels.Count - $missing.Count) + '/' + $expectedLabels.Count)
        if ($missing.Count) { throw ('Etiquetas pendientes: ' + ($missing.name -join ', ')) }
        Write-Output ('Milestones: ' + @(Invoke-GitHub GET "repos/$repository/milestones?state=all&per_page=100").Count)
    }
} finally {
    $headers.Clear()
    $credential.Clear()
    $credentialOutput = $null
}
