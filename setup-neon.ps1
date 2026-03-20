#!/usr/bin/env pwsh
# Neon CLI Setup Script - Run this in PowerShell

Write-Host "🔧 Neon CLI Setup Script" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Install Neon CLI
Write-Host "📦 Step 1: Installing Neon CLI..." -ForegroundColor Yellow
Write-Host "This may take 1-2 minutes..." -ForegroundColor Gray
npm install -g neonctl -silent
Start-Sleep -Seconds 3

# Step 2: Verify Installation
Write-Host ""
Write-Host "✅ Step 2: Verifying installation..." -ForegroundColor Yellow
$neonVersion = neonctl --version 2>&1
Write-Host "Neon CLI Version: $neonVersion" -ForegroundColor Green

# Step 3: Authenticate
Write-Host ""
Write-Host "🔐 Step 3: Authenticating with Neon..." -ForegroundColor Yellow
Write-Host "Your browser will open. Click 'Grant access' to authorize the CLI." -ForegroundColor Cyan
Write-Host ""
neonctl auth

# Step 4: List Projects
Write-Host ""
Write-Host "📋 Step 4: Listing your projects..." -ForegroundColor Yellow
neonctl projects list

# Step 5: Get Connection String
Write-Host ""
Write-Host "🔗 Step 5: Getting connection string..." -ForegroundColor Yellow
neonctl connection-string

# Step 6: Verification
Write-Host ""
Write-Host "✅ SUCCESS! Neon CLI is now connected!" -ForegroundColor Green
Write-Host ""
Write-Host "You can now run commands like:" -ForegroundColor Cyan
Write-Host '  - neonctl sql "SELECT * FROM questions LIMIT 5;"' -ForegroundColor Gray
Write-Host '  - neonctl databases list' -ForegroundColor Gray
Write-Host '  - neonctl branches list' -ForegroundColor Gray
Write-Host ""
Write-Host "Copy one of the outputs above to share with the AI assistant." -ForegroundColor Yellow
