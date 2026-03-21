# Neon Clean Question-Bank Rebuild

This workflow performs a controlled clean rebuild of question-bank data using the backend admin API and Neon CLI verification.

## What This Rebuild Does

- Deletes and recreates only question-bank taxonomy data:
  - `subjects`
  - `chapters`
  - `questions`
- Keeps user/auth data intact (`users`, sessions, etc.)
- Reseeds from JSON resources in `backend/src/main/resources/questions`
- Supports curated-only mode (`resourceOnly=true`) to avoid synthetic fallback questions
- Supports retaining questions only for selected subjects (`keepQuestionSubjects`)

## Prerequisites

1. Backend deployed and reachable (local or Azure App Service).
2. Admin credentials available for API auth.
3. Neon CLI installed and authenticated (`neonctl auth`).
4. Neon project selected in CLI context.

## Step 1: Dry Run (No Data Changes)

Use admin endpoint:

`POST /api/admin/rebuild-question-bank`

Body:

```json
{
  "dryRun": true,
  "resourceOnly": true,
  "keepQuestionSubjects": ["Medicine"]
}
```

PowerShell example:

```powershell
$body = @{ dryRun = $true; resourceOnly = $true } | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri "https://<your-backend>/api/admin/rebuild-question-bank" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer <admin_jwt>" } `
  -Body $body
```

## Step 2: Execute Live Rebuild

```json
{
  "dryRun": false,
  "resourceOnly": true,
  "keepQuestionSubjects": ["Medicine"]
}
```

PowerShell example:

```powershell
$body = @{ dryRun = $false; resourceOnly = $true; keepQuestionSubjects = @("Medicine") } | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri "https://<your-backend>/api/admin/rebuild-question-bank" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer <admin_jwt>" } `
  -Body $body
```

## Step 3: Verify in Neon via CLI

Run quick checks:

```powershell
neonctl sql "SELECT COUNT(*) AS subjects_count FROM subjects;"
neonctl sql "SELECT COUNT(*) AS chapters_count FROM chapters;"
neonctl sql "SELECT COUNT(*) AS questions_count FROM questions;"
```

Spot-check subject/chapter distribution:

```powershell
neonctl sql "
SELECT s.name AS subject, c.name AS chapter, COUNT(q.id) AS questions
FROM subjects s
LEFT JOIN chapters c ON c.subject_id = s.id
LEFT JOIN questions q ON q.chapter_id = c.id
GROUP BY s.name, c.name
ORDER BY s.name, c.name;
"
```

## Notes

- `resourceOnly=true` is recommended for high-quality curated rebuilds.
- `resourceOnly=false` allows fallback auto-generation when a chapter has no JSON questions.
- Use `keepQuestionSubjects` when you want full subject/chapter structure but only specific subject question banks retained.
- If your backend is local, use `http://localhost:8080` instead of deployed URL.
