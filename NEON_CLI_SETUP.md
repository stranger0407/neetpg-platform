# 🗄️ Connect Neon CLI - Complete Setup Guide

## ⚡ Quick Setup (5 Steps, 10 Minutes)

### Step 1: Install Neon CLI

Open PowerShell and run:

```powershell
# Install Neon CLI via npm
npm install -g neonctl
```

**Expected Output**:
```
added X packages in X seconds
```

### Step 2: Verify Installation

```powershell
neonctl --version
```

**Expected Output**:
```
neonctl version X.X.X
```

---

### Step 3: Create Neon Account (if you don't have one)

1. Go to: **https://neon.tech**
2. Click **"Sign Up"** or **"Sign in with GitHub"** (recommended)
3. Provide email and password (or use GitHub)
4. Verify email
5. Create first project
6. Create database (accept defaults)

**Expected**: You'll see Neon dashboard with database connection string

---

### Step 4: Authenticate Neon CLI with Your Account

Run this command:

```powershell
neonctl auth
```

**What happens**:
1. Browser opens automatically
2. Click **"Grant access"** to authorize CLI
3. You'll see: "The CLI has been successfully authenticated"
4. Close browser tab

**Expected**: Command returns successfully

```powershell
Authenticated successfully ✅
```

---

### Step 5: Test Connection

List your projects:

```powershell
neonctl projects list
```

**Expected Output**:
```
ID                 Name      Region
─────────────────  ────────  ──────
xxxxxxxxxxxxxx     My Project  us-east-1
```

If you see projects → **YOU'RE CONNECTED** ✅

---

## 🔗 What We Can Do Now

Once connected, I can:

### ✅ Database Information
```powershell
neonctl databases list
neonctl branches list
neonctl connection-string
```

### ✅ Run SQL Queries
```powershell
neonctl sql "SELECT * FROM questions LIMIT 5;"
neonctl sql "SHOW TABLES;"
```

### ✅ Manage Branches
```powershell
neonctl branches create
neonctl branches reset
```

### ✅ Get Connection Details
```powershell
neonctl connection-string --app
```

---

## 📋 Installation Checklist

After each step, verify:

- [ ] Step 1: `npm install -g neonctl` completed
- [ ] Step 2: `neonctl --version` shows version
- [ ] Step 3: Neon account created at neon.tech
- [ ] Step 4: `neonctl auth` authenticated successfully
- [ ] Step 5: `neonctl projects list` shows your projects

---

## ⚠️ If Something Goes Wrong

### Problem 1: "npm: command not found"
**Solution**:
- Node.js not installed properly
- Run: `node --version`
- If error, reinstall Node.js from nodejs.org

### Problem 2: "neonctl: command not found"
**Solution**:
- Restart PowerShell after npm install
- Or run: `npm install -g neonctl` again

### Problem 3: "Auth failed" or "Unauthorized"
**Solution**:
1. Make sure Neon account is created at neon.tech
2. Try: `neonctl auth` again
3. When browser opens, click **"Grant access"**

### Problem 4: No projects shown
**Solution**:
1. Go to https://neon.tech/app/projects
2. Create a project if none exists
3. Create a database
4. Try `neonctl projects list` again

---

## 🎯 After Setup Complete

Once all checks pass ✅, come back and tell me:

```powershell
# I'll ask you to run this and share output:
neonctl projects list
neonctl databases list
neonctl connection-string
```

Then I can:
- ✅ Access your database
- ✅ Get schema information
- ✅ Run queries
- ✅ Make changes
- ✅ Create tables/data

---

## 📝 Quick Command Reference

Once authenticated, these commands work:

```powershell
# List everything
neonctl projects list          # See all projects
neonctl branches list          # See all branches
neonctl databases list         # See all databases
neonctl roles list             # See all users

# Get credentials
neonctl connection-string      # Get connection string

# Run SQL
neonctl sql "SELECT 1;"        # Test query
neonctl sql "SHOW TABLES;"     # List tables

# Create/manage
neonctl backends create        # Create back-up
neonctl branches create        # Create branch
```

---

## ✅ Next Steps

1. **Run the 5 installation steps above**
2. **Complete all checklist items**
3. **Come back here** and run:
   ```powershell
   neonctl projects list
   neonctl connection-string
   ```
4. **Share the output** with me
5. **I'll connect and manage your database** 🗄️

---

## 🚀 Start Installation Now

```powershell
# Copy and paste this entire block:
npm install -g neonctl
neonctl --version
neonctl auth
neonctl projects list
neonctl connection-string
```

After these 5 commands work, come back! 🎯
