# 🚀 Neon CLI Setup - Simple Steps

## 📝 Do This Now (In PowerShell)

Open PowerShell and run these commands ONE BY ONE:

### Command 1: Install Neon CLI
```powershell
npm install -g neonctl
```
**Wait** for it to complete (1-2 minutes). You should see `added X packages`.

**If you see error**: "npm not found" → Node.js isn't installed. Download from nodejs.org

---

### Command 2: Verify Installation
```powershell
neonctl --version
```

**Expected output**: Version number (e.g., `neonctl 0.25.0`)

---

### Command 3: Authenticate with Neon
```powershell
neonctl auth
```

**What happens**:
1. Browser opens automatically
2. Click **"Grant access"** button
3. Wait for confirmation message
4. Close browser

**Back in PowerShell**, you should see:
```
✅ Authentication successful
```

---

### Command 4: List Your Projects
```powershell
neonctl projects list
```

**Expected output**:
```
ID                   Name                Status
─────────────────    ──────────────      ──────
abc123xyz789...      My First Project    active
```

**If no projects**: Create one at https://neon.tech

---

### Command 5: Get Connection String
```powershell
neonctl connection-string
```

**Expected output** (will look like):
```
postgresql://neondb_owner:password@ep-xxxxx.us-east-1.neon.tech/neondb
```

**Copy this string** and save it somewhere safe.

---

### Command 6: List Databases
```powershell
neonctl databases list
```

**Expected output**:
```
Name              Owner
────────────      ──────────
neondb            neondb_owner
```

---

## ✅ If All Commands Work

Come back here and tell me:

> **I've set up Neon CLI successfully! Here's my info:**
>
> ```
> [Paste output from: neonctl connection-string]
> ```

Then I can:
- ✅ Access your database
- ✅ See table structure
- ✅ Query data
- ✅ Make changes
- ✅ Run migrations
- ✅ Optimize schema

---

## ⚠️ Troubleshooting

### Issue: "npm: command not found"
```powershell
# Node.js not installed properly
# Download from: https://nodejs.org/
# Install and restart PowerShell
# Try npm install again
```

### Issue: "neonctl: command not found"
```powershell
# Close and reopen PowerShell
# npm needs PATH to update
# Then try: neonctl --version again
```

### Issue: "Auth failed" or "No projects"
```powershell
# 1. Go to https://neon.tech
# 2. Create account if needed
# 3. Create a project
# 4. Create a database
# 5. Try: neonctl auth again
```

### Issue: Command hangs/freezes
```powershell
# Press Ctrl+C to stop
# Try: neonctl --version
# If still hangs, restart PowerShell
# Then try again
```

---

## 🎯 Quick Copy-Paste (All Commands)

```powershell
npm install -g neonctl
neonctl --version
neonctl auth
neonctl projects list
neonctl connection-string
neonctl databases list
```

Run each command and wait for it to complete before running the next.

---

## ✅ Success Checklist

When done, you should see:

- [ ] `npm install -g neonctl` → Completed
- [ ] `neonctl --version` → Shows version number
- [ ] `neonctl auth` → Shows "Authentication successful"
- [ ] `neonctl projects list` → Shows your projects
- [ ] `neonctl connection-string` → Shows PostgreSQL URL
- [ ] `neonctl databases list` → Shows your databases

---

## 🚀 Next Step

Once all above complete ✅:

Reply with:
```
I'm ready! Here's my connection string:
[paste output from neonctl connection-string]
```

Then I can start managing your database! 🗄️
