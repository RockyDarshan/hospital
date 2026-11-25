# Hospital API smoke test (Windows PowerShell 5.1 or PowerShell 7).
# Start the API first, then run:   .\scripts\smoke-test.ps1
# Skip the medical-record section:  .\scripts\smoke-test.ps1 -SkipRecords
param([switch]$SkipRecords)

$base = 'http://localhost:8080'
$script:pass = 0
$script:fail = 0

function Call($method, $path, $token, $body) {
    $headers = @{}
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    $params = @{ Method = $method; Uri = "$base$path"; Headers = $headers; UseBasicParsing = $true }
    if ($body) {
        $params['Body'] = ($body | ConvertTo-Json -Depth 5)
        $params['ContentType'] = 'application/json'
    }
    try {
        $r = Invoke-WebRequest @params
        $json = $null
        if ($r.Content) { $json = $r.Content | ConvertFrom-Json -ErrorAction SilentlyContinue }
        return [pscustomobject]@{ Status = [int]$r.StatusCode; Body = $json }
    } catch {
        $resp = $_.Exception.Response
        $status = 0
        $text = ''
        if ($resp) {
            $status = [int]$resp.StatusCode
            try {
                $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
                $text = $reader.ReadToEnd()
            } catch { }
        }
        $json = $null
        if ($text) { $json = $text | ConvertFrom-Json -ErrorAction SilentlyContinue }
        return [pscustomobject]@{ Status = $status; Body = $json }
    }
}

function Check($name, $actual, $expected) {
    if ($actual -eq $expected) {
        Write-Host ("PASS  {0}  (HTTP {1})" -f $name, $actual) -ForegroundColor Green
        $script:pass++
    } else {
        Write-Host ("FAIL  {0}  expected {1}, got {2}" -f $name, $expected, $actual) -ForegroundColor Red
        $script:fail++
    }
}

$adminEmail = Read-Host 'Admin email (your ADMIN_EMAIL, for example admin@hospital.local)'
$adminPass = Read-Host 'Admin password (your ADMIN_PASSWORD)'
$s = Get-Random -Maximum 99999
$slotDate = (Get-Date).AddDays(2).ToString('yyyy-MM-dd', [Globalization.CultureInfo]::InvariantCulture)

Write-Host "`n--- Public endpoints and login" -ForegroundColor Cyan
Check 'health endpoint is public' (Call GET '/actuator/health').Status 200
Check 'departments need a token' (Call GET '/api/departments').Status 401
$r = Call POST '/api/auth/login' $null @{ email = $adminEmail; password = $adminPass }
Check 'admin login' $r.Status 200
$admin = $r.Body.accessToken
if (-not $admin) {
    Write-Host 'Admin login failed, so the rest cannot run. Check the credentials and that the app is running.' -ForegroundColor Red
    exit 1
}
Check 'wrong password is rejected' (Call POST '/api/auth/login' $null @{ email = $adminEmail; password = 'wrong-password-123' }).Status 401
Check '/api/auth/me works with a token' (Call GET '/api/auth/me' $admin).Status 200

Write-Host "`n--- Creating test data" -ForegroundColor Cyan
$r = Call POST '/api/departments' $admin @{ name = "Dept$s"; description = 'Smoke test' }
Check 'admin creates a department' $r.Status 201
$deptId = $r.Body.id

$r = Call POST '/api/doctors' $admin @{ email = "dr$s@hospital.test"; password = 'Doctor@123'; fullName = 'Dr Smoke Test'; phone = '9876543210'; qualification = 'MBBS'; departmentId = $deptId }
Check 'admin creates a doctor' $r.Status 201
$doctorId = $r.Body.id

$r = Call POST '/api/auth/register' $null @{ email = "a$s@patient.test"; password = 'Patient@123'; fullName = 'Patient A'; dateOfBirth = '1998-05-14'; gender = 'MALE'; phone = '9123456780'; address = 'Bangalore' }
Check 'register patient A' $r.Status 201
$patientA = $r.Body.id
$r = Call POST '/api/auth/register' $null @{ email = "b$s@patient.test"; password = 'Patient@123'; fullName = 'Patient B'; dateOfBirth = '1995-09-02'; gender = 'FEMALE'; phone = '9123456781'; address = 'Bangalore' }
Check 'register patient B' $r.Status 201
$patientB = $r.Body.id

$patA = (Call POST '/api/auth/login' $null @{ email = "a$s@patient.test"; password = 'Patient@123' }).Body.accessToken
$patB = (Call POST '/api/auth/login' $null @{ email = "b$s@patient.test"; password = 'Patient@123' }).Body.accessToken
$doc = (Call POST '/api/auth/login' $null @{ email = "dr$s@hospital.test"; password = 'Doctor@123' }).Body.accessToken
Check 'patient A, B and the doctor all logged in' ([bool]($patA -and $patB -and $doc)) $true

Write-Host "`n--- Access control (roles and ownership)" -ForegroundColor Cyan
$newDoc = @{ email = "x$s@hospital.test"; password = 'Doctor@123'; fullName = 'Blocked Doctor'; phone = '9876543211'; qualification = 'MBBS'; departmentId = $deptId }
Check 'PATIENT cannot create a doctor' (Call POST '/api/doctors' $patA $newDoc).Status 403
Check 'DOCTOR cannot create a doctor' (Call POST '/api/doctors' $doc $newDoc).Status 403
Check 'PATIENT cannot update a doctor' (Call PUT "/api/doctors/$doctorId" $patA @{ fullName = 'Hacked'; phone = '9876543212'; qualification = 'None'; departmentId = $deptId }).Status 403
Check 'ADMIN can update a doctor' (Call PUT "/api/doctors/$doctorId" $admin @{ fullName = 'Dr Smoke Test'; phone = '9876543210'; qualification = 'MBBS, MD'; departmentId = $deptId }).Status 200
Check 'PATIENT cannot create a department' (Call POST '/api/departments' $patA @{ name = "Blocked$s" }).Status 403
Check 'PATIENT cannot list all patients' (Call GET '/api/patients' $patA).Status 403
Check 'PATIENT can read their own record' (Call GET "/api/patients/$patientA" $patA).Status 200
Check 'PATIENT cannot read another patient' (Call GET "/api/patients/$patientB" $patA).Status 403
Check 'ADMIN can read any patient' (Call GET "/api/patients/$patientB" $admin).Status 200
Check 'any logged-in user can list doctors' (Call GET '/api/doctors' $patA).Status 200
$tampered = $patA.Substring(0, $patA.Length - 2) + 'xx'
Check 'a tampered token is rejected' (Call GET '/api/auth/me' $tampered).Status 401

Write-Host "`n--- Booking rules" -ForegroundColor Cyan
$slot = @{ doctorId = $doctorId; startTime = "${slotDate}T10:00:00+05:30"; reason = 'Chest pain' }
$r = Call POST '/api/appointments' $patA $slot
Check 'patient A books 10:00' $r.Status 201
$apptA = $r.Body.id
Check 'same slot again is a conflict' (Call POST '/api/appointments' $patA $slot).Status 409
Check 'patient B, same doctor and slot, is a conflict' (Call POST '/api/appointments' $patB $slot).Status 409
Check 'off the half-hour grid is rejected' (Call POST '/api/appointments' $patA @{ doctorId = $doctorId; startTime = "${slotDate}T10:15:00+05:30" }).Status 400
Check 'before opening hours is rejected' (Call POST '/api/appointments' $patA @{ doctorId = $doctorId; startTime = "${slotDate}T08:00:00+05:30" }).Status 400
Check 'a time in the past is rejected' (Call POST '/api/appointments' $patA @{ doctorId = $doctorId; startTime = '2025-01-01T10:00:00+05:30' }).Status 400
Check 'a doctor cannot book' (Call POST '/api/appointments' $doc $slot).Status 403
$av = Call GET "/api/appointments/availability?doctorId=$doctorId&date=$slotDate" $patB
Check 'availability responds' $av.Status 200
Check 'availability hides the booked slot (15 free of 16)' $av.Body.freeSlots.Count 15
Check 'availability without a date is rejected' (Call GET "/api/appointments/availability?doctorId=$doctorId" $patB).Status 400
Check 'patient and doctor both see it in /mine (patient)' (Call GET '/api/appointments/mine' $patA).Status 200
Check 'patient and doctor both see it in /mine (doctor)' (Call GET '/api/appointments/mine' $doc).Status 200
Check 'patient B cannot read patient A appointment' (Call GET "/api/appointments/$apptA" $patB).Status 403
Check 'admin can read it' (Call GET "/api/appointments/$apptA" $admin).Status 200
Check 'patient B cannot cancel it' (Call PATCH "/api/appointments/$apptA/cancel" $patB).Status 403
Check 'patient A cancels it' (Call PATCH "/api/appointments/$apptA/cancel" $patA).Status 200
Check 'cancelling twice is a conflict' (Call PATCH "/api/appointments/$apptA/cancel" $patA).Status 409
$r = Call POST '/api/appointments' $patB $slot
Check 'patient B books the freed slot' $r.Status 201
$apptB = $r.Body.id
Check 'doctor cannot complete a future appointment' (Call PATCH "/api/appointments/$apptB/complete" $doc).Status 409

if (-not $SkipRecords) {
    Write-Host "`n--- Completion and medical records" -ForegroundColor Cyan
    Write-Host 'The API refuses past bookings, so make appointment' $apptB 'old. In DBeaver (hospital_user connection, Auto-commit on) run:' -ForegroundColor Yellow
    Write-Host ("UPDATE appointments SET start_time = now() - interval '2 hours' WHERE id = {0};" -f $apptB) -ForegroundColor Yellow
    Read-Host 'Press Enter when that has run'
    $rec = @{ diagnosis = 'Mild flu'; prescription = 'Rest, fluids' }
    Check 'record before completion is a conflict' (Call POST "/api/appointments/$apptB/record" $doc $rec).Status 409
    Check 'patient cannot complete an appointment' (Call PATCH "/api/appointments/$apptB/complete" $patB).Status 403
    Check 'doctor completes it' (Call PATCH "/api/appointments/$apptB/complete" $doc).Status 200
    Check 'doctor adds the record' (Call POST "/api/appointments/$apptB/record" $doc $rec).Status 201
    Check 'a second record is a conflict' (Call POST "/api/appointments/$apptB/record" $doc $rec).Status 409
    Check 'the patient reads their record' (Call GET "/api/appointments/$apptB/record" $patB).Status 200
    Check 'another patient cannot read it' (Call GET "/api/appointments/$apptB/record" $patA).Status 403
    Check 'admin cannot read medical records' (Call GET "/api/appointments/$apptB/record" $admin).Status 403
}

Write-Host ("`nPassed: {0}   Failed: {1}" -f $script:pass, $script:fail) -ForegroundColor Cyan
if ($script:fail -gt 0) { exit 1 }