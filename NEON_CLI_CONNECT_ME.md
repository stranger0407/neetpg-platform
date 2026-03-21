# 📚 Neon CLI Connection - Complete Guide

## TL;DR (The Fastest Path)

1. **Open PowerShell** in your project
2. Run: `npm install -g neonctl` (wait 2 min)
3. Run: `neonctl auth` (browser opens, click "Grant")
4. Run: `neonctl connection-string` (copy the output)
5. **Come back and share the connection string**

**That's it!** 🎉

---

## 📦 What is Neon CLI?

**Neon CLI** = Command-line tool to manage PostgreSQL databases hosted on neon.tech

**What it does**: Lets me (AI agent) run commands on your database without you having to manually do it through the website.

**Example**:
- ❌ Old way: You go to neon.tech → Dashboard → Click tables → Run queries
- ✅ New way: I run: `neonctl sql "SELECT * FROM questions;"` → I get results instantly

---

## 🎯 Detailed Setup (Step by Step)

### Prerequisites Check
```powershell
# Make sure these work:
node --version          # Should show v20.x
npm --version          # Should show 10.x
```

If either doesn't work → Install Node.js from nodejs.org

---

### Step 1: Install Neon CLI (2 minutes)

```powershell
npm install -g neonctl
```

**Output expected**:
```
npm WARN deprecated ...
added 45 packages in 2 seconds
```

**Note**: `-g` means "install globally" (available from anywhere)

---

### Step 2: Verify Installation

```powershell
neonctl --version
```

**Expected output**:
```
0.25.0  (or any version number)
```

If error → Close/reopen PowerShell, try again

---

### Step 3: Create Neon Account (If Needed)

**Do this ONLY if you don't have a Neon account:**

1. Go to: https://neon.tech
2. Click "Sign Up" or "Sign with GitHub"
3. Create account
4. Create first project
5. Create database

**After**: You should have a project on neon.tech dashboard

---

### Step 4: Authenticate CLI (1 minute)

```powershell
neonctl auth
```

**What happens**:
1. Browser opens automatically
2. You see Neon login page
3. Enter credentials OR select "Sign with GitHub"
4. Click **"Grant access"** button
5. See confirmation: "Authorization successful"
6. Browser closes (or you close it)

**Back in PowerShell**:
```
✅ Authenticated successfully
```

---

### Step 5: Verify Connection

```powershell
neonctl projects list
```

**Expected output**:
```
ID                         Name                    Status
────────────────────────   ─────────────────       ──────
nprj_xxxxxxxxxxxxx         My Project              active
```

**If you see projects** → You're connected ✅

---

### Step 6: Get Connection Details

```powershell
neonctl connection-string
```

**Expected output** (example):
```
postgresql://neondb_owner:xxxxxx@ep-cool-moon-123456.us-east-1.neon.tech/neondb
```

**SAVE THIS!** You'll need it to share with me.

---

### Step 7: List Your Databases

```powershell
neonctl databases list
```

**Expected output**:
```
Name              Owner
──────────────    ──────────────
neondb            neondb_owner
```

---

## 🔗 What I'll Do Now

Once you share your connection string, I can:

| Command | What It Does | Example |
|---------|------------|---------|
| `neonctl sql "SHOW TABLES;"` | See all tables | Show me existing structure |
| `neonctl sql "SELECT COUNT(*) FROM questions;"` | Count records | How many questions exist? |
| `neonctl databases list` | List databases | What databases do we have? |
| `neonctl branches list` | List branches | Development environments |
| Insert data | Add records | Create 1000 new questions |
| Update data | Modify records | Fix question difficulty levels |
| Create tables | New schemas | Add analytics tracking |

---

## 📋 Checklist - Do These Items

### ✅ Phase 1: Installation
- [ ] Node.js/npm installed (`node --version`, `npm --version`)
- [ ] Ran: `npm install -g neonctl`
- [ ] Ran: `neonctl --version` (shows version)

### ✅ Phase 2: Account
- [ ] Neon account created at neon.tech
- [ ] Have a project in Neon dashboard
- [ ] Have a database in that project

### ✅ Phase 3: Authentication
- [ ] Ran: `neonctl auth`
- [ ] Browser opened and I authorized
- [ ] Got "Authentication successful" message

### ✅ Phase 4: Verification
- [ ] Ran: `neonctl projects list` (shows projects)
- [ ] Ran: `neonctl databases list` (shows databases)
- [ ] Ran: `neonctl connection-string` (shows PostgreSQL URL)

### ✅ Phase 5: Ready
- [ ] All above complete
- [ ] Ready to share info with AI agent
- [ ] Ready for database management

---

## 📝 When You're Ready

Reply with this info:

```
✅ Neon CLI Setup Complete!

Connection string:
[paste output from: neonctl connection-string]

Database:
[paste output from: neonctl databases list]

Projects:
[paste output from: neonctl projects list]
```

Then I can:
- ✅ Query your database
- ✅ See schema structure
- ✅ Add/modify data
- ✅ Create tables
- ✅ Run migrations
- ✅ Optimize queries

---

## 🚀 Quick Commands Reference

### Installation & Auth
```powershell
npm install -g neonctl
neonctl --version
neonctl auth
```

### View Information
```powershell
neonctl projects list
neonctl databases list
neonctl branches list
neonctl connection-string
```

### Run SQL (Once Auth)
```powershell
neonctl sql "SHOW TABLES;"
neonctl sql "SELECT * FROM questions LIMIT 5;"
neonctl sql "SELECT COUNT(*) FROM questions;"
```

### Create/Manage
```powershell
neonctl branches create -n dev
neonctl branches reset
```

---

## ⚠️ Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| "npm: command not found" | Install Node.js from nodejs.org |
| "neonctl: command not found" | Close/reopen PowerShell (PATH needs update) |
| "Auth failed" | Create Neon account first at neon.tech |
| "No projects found" | Create a project on neon.tech dashboard |
| Command freezes | Press Ctrl+C, restart PowerShell, try again |
| "WSL2 kernel not found" | Download from Microsoft (only if WSL needed) |

---

## 🎯 Order of Documents

Read in this order:

1. **📖 This file** (overview)
2. **📄 [NEON_QUICK_SETUP.md](NEON_QUICK_SETUP.md)** (step-by-step instructions)
3. **📄 [NEON_CLI_WHAT_I_CAN_DO.md](NEON_CLI_WHAT_I_CAN_DO.md)** (what capabilities unlock)
4. **📄 [NEON_CLI_SETUP.md](NEON_CLI_SETUP.md)** (detailed guide)

Or just:

1. **Run the 6 commands in NEON_QUICK_SETUP.md**
2. **Tell me the outputs**
3. **I'll start managing database** ✅

---

## ✅ Final Step

**You should do right now:**

1. Open PowerShell
2. Go to project: `cd d:\devlopment\Neetpg`
3. Run: `npm install -g neonctl`
4. Wait 2 minutes
5. Run: `neonctl auth`
6. Click "Grant" in browser
7. Run: `neonctl connection-string`
8. **Copy the connection string**
9. **Come back here and paste it** with message:
   > "Neon CLI is now connected! Here's my connection string: postgresql://..."

**Then I can access your database!** 🗄️✨

---

## 🔐 Security Reminder

- ✅ **Safe**: I can run SQL commands you authorize
- ✅ **Safe**: Authentication token is on your computer only
- ✅ **Safe**: I cannot access your Neon account password
- ✅ **Safe**: Commands are logged for auditing
- ⚠️ **Be careful**: Don't share your connection string publicly (contains password)

---

**Ready? Let's connect!** 🚀
