# Docker Installation - Step-by-Step for Windows

## 🎯 Goal: Install Docker Desktop in 15-20 Minutes

---

## STEP 1: Check Your System ✅ (2 minutes)

### Check Windows Version
```powershell
# Open PowerShell and run:
Get-WmiObject -Class Win32_OperatingSystem | Select-Object Caption

# You should see:
# Caption
# -------
# Microsoft Windows 11 (or Windows 10)

# ✅ If you see Windows 10 or 11: You're good!
# ❌ If older Windows: Docker Desktop may not work
```

### Check Available RAM
```powershell
Get-WmiObject -Class Win32_ComputerSystem | Select-Object TotalPhysicalMemory

# Convert to GB:
# Divide result by 1073741824

# ✅ If you have 4GB+: You're good!
# ⚠️ If less than 4GB: Docker will be slower but still work
```

### Check Available Disk Space
```powershell
# In File Explorer:
# Right-click C: drive → Properties
# Look for "Free space"

# ✅ If you have 5GB+ free: You're good!
# ❌ If less than 3GB: Free up space first
```

---

## STEP 2: Download Docker Desktop 📥 (2-3 minutes)

### Method A: Direct Download (Recommended)
```
1. Open browser → Visit: https://www.docker.com/products/docker-desktop
2. Click: "Download for Windows"
3. Save file: DockerDesktopInstaller.exe
   Location: Downloads folder
4. Wait for download to complete
```

### Method B: Alternative Download URL
```
https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe
```

### Verify Download
```powershell
# Check if file exists:
Test-Path "$env:USERPROFILE\Downloads\DockerDesktopInstaller.exe"

# Should return: True
```

---

## STEP 3: Run Installer 🚀 (5-10 minutes)

### Run as Administrator
```
1. Open File Explorer
2. Navigate to: C:\Users\<YourUsername>\Downloads
3. Find: DockerDesktopInstaller.exe
4. Right-click → "Run as Administrator"
5. Click "Yes" when prompted by UAC
```

### Installation Progress
```
The installer will:
  1. Extract files (~1-2 min)
  2. Install Docker Engine (~2-3 min)
  3. Configure WSL2 (~1-2 min)
  4. Set up integration (~1 min)
  5. Ask to restart Windows
```

### During Installation
```
✅ Do: Wait for installation to complete
✅ Do: Say "Yes" to administrator prompts
✅ Do: Allow Windows Defender/Firewall permission

❌ Don't: Close installer window
❌ Don't: Interrupt the process
❌ Don't: Remove any files
```

---

## STEP 4: Restart Windows 🔄 (5 minutes)

### Complete Restart Required
```
1. When installer says "Restart required"
2. Click: "Close and Restart" (or Restart Now)
3. Windows will restart
4. Log back in when Windows boots
5. Wait 1-2 minutes for Docker to start
```

### Avoid Skipping Restart
```
⚠️ Important: Full restart is REQUIRED
   • Partial restart/sign-out won't work
   • Only way to enable WSL2
   • Only way to register Docker CLI
```

---

## STEP 5: Verify Installation ✅ (3-5 minutes)

### Open PowerShell
```powershell
# Click Start button
# Type: PowerShell
# Open: Windows PowerShell (or PowerShell)
```

### Test 1: Check Docker CLI
```powershell
docker --version

# Expected output:
# Docker version 27.0.1, build abc1234

# ✅ Success: Version number shown
# ❌ Error "docker not found": 
#    → Restart PowerShell completely
#    → If still fails, restart Windows again
```

### Test 2: Run Hello World
```powershell
docker run hello-world

# First time: Will download image (~1-2 min)
# Then: Shows success message

# Expected output:
# Hello from Docker!
# This message shows that your installation appears to be working correctly.

# ✅ Success: Message displayed
# ❌ Error "Cannot connect to Docker daemon":
#    → Docker Desktop app not running
#    → See troubleshooting below
```

### Test 3: Check Docker Info
```powershell
docker info

# Expected output:
# Client:
#  Version: 27.0.1
#  ...
# Server:
#  Engine:
#   Containers: 1
#   ...

# ✅ Success: Shows system information
```

### Test 4: List Images
```powershell
docker images

# Expected output:
# REPOSITORY    TAG      IMAGE ID       CREATED
# hello-world   latest   1234567...     3 days ago

# ✅ Success: Shows images
```

---

## STEP 6: Start Using Docker 🐳 (Immediate)

### Build Your First Image
```powershell
# Navigate to project:
cd d:\devlopment\Neetpg

# Build backend image:
docker build -f docker/Dockerfile.backend `
  -t neetpg-backend:latest `
  .

# Expected: "Successfully built neetpg-backend:latest"
# Time: 3-5 minutes first time (downloads base images)

# Build frontend image:
docker build -f docker/Dockerfile.frontend `
  -t neetpg-frontend:latest `
  .

# Expected: "Successfully built neetpg-frontend:latest"
# Time: 2-3 minutes
```

### Verify Your Built Images
```powershell
docker images

# Should show:
# REPOSITORY         TAG       IMAGE ID       CREATED
# neetpg-backend     latest    abc123...      1 min ago
# neetpg-frontend    latest    def456...      30 sec ago
# hello-world        latest    1234567...     3 days ago

# ✅ Success: Your images are ready!
```

---

## 🆘 Troubleshooting

### Issue 1: "docker: The term 'docker' is not recognized"

**Cause**: Docker CLI not in PATH or PowerShell not restarted

**Solution**:
```powershell
# 1. Close PowerShell completely
# 2. Open fresh PowerShell window
# 3. Try again: docker --version

# If still fails:
# 1. Restart Windows completely
# 2. Open PowerShell
# 3. Try: docker --version
```

---

### Issue 2: "Cannot connect to Docker daemon"

**Cause**: Docker Desktop not running

**Solution**:
```
1. Look at taskbar (bottom right)
2. Find whale icon (🐳)
3. If not visible: Click Start → Search "Docker Desktop"
4. Open Docker Desktop
5. Wait 1-2 minutes for startup
6. Try docker command again

Visual check:
  ✅ Whale icon visible = Docker running
  ❌ Whale icon missing = Docker not running
```

---

### Issue 3: "Docker Desktop won't start"

**Cause**: WSL2 not properly installed or BIOS virtualization disabled

**Solution Option 1 - Restart**:
```
1. Restart Windows
2. Open Docker Desktop
3. Wait 2-3 minutes
4. Check if running (whale icon)
```

**Solution Option 2 - Check Virtualization**:
```powershell
# Check if WSL2 is working:
wsl -l -v

# Should show:
# NAME      STATE    VERSION
# Docker    Running  2

# If not:
# 1. Ctrl+Shift+Esc → Task Manager
# 2. Performance tab
# 3. Check "Virtualization: Enabled"
```

**Solution Option 3 - Reinstall**:
```
1. Uninstall Docker Desktop
   → Settings → Apps → Find "Docker Desktop"
   → Uninstall
2. Restart Windows
3. Download Docker Desktop again
4. Install fresh
```

---

### Issue 4: Permission Denied Error

**Cause**: PowerShell not running as Administrator

**Solution**:
```
1. Click Start
2. Type: PowerShell
3. Right-click Windows PowerShell
4. "Run as Administrator"
5. Click Yes
6. Try Docker command again
```

---

### Issue 5: Docker Commands Very Slow

**Cause**: Low disk space, low RAM, or network issue

**Solution**:
```powershell
# Check disk space:
Get-Volume

# Check memory:
Get-WmiObject -Class Win32_ComputerSystem | Select-Object TotalPhysicalMemory

# Check Docker resources:
docker system df

# Free up space if needed:
# 1. Delete large files
# 2. Empty Recycle Bin
# 3. Restart Docker Desktop
```

---

### Issue 6: Image Build Fails

**Cause**: Missing files, network issue, or Java/Node problem

**Solution**:
```powershell
# Try again (might be temporary):
docker build -f docker/Dockerfile.backend -t neetpg-backend:latest .

# If still fails, check:
# 1. All files exist: dir docker/Dockerfile.backend
# 2. Maven available: mvn --version
# 3. Node available: node --version
# 4. Internet connection working
```

---

## ✅ Success Checklist

- [ ] Windows 10/11 installed
- [ ] 5GB+ free disk space
- [ ] 4GB+ RAM available
- [ ] Docker Desktop downloaded
- [ ] Installer run as Administrator
- [ ] Windows restarted after install
- [ ] `docker --version` works
- [ ] `docker run hello-world` succeeds
- [ ] Docker Desktop icon in taskbar
- [ ] `docker images` shows hello-world
- [ ] Backend image builds successfully
- [ ] Frontend image builds successfully

---

## 🚀 You're Ready When:

```
✅ docker --version returns a version
✅ docker run hello-world shows success message
✅ docker images shows your built images
✅ Docker Desktop app is running (whale icon visible)
✅ No error messages in PowerShell
```

---

## 📊 Time Breakdown

| Step | Time | What to Do |
|------|------|-----------|
| 1. Check System | 2 min | Verify Windows/RAM/disk |
| 2. Download | 3 min | Download installer |
| 3. Install | 8 min | Run installer (auto) |
| 4. Restart | 5 min | Restart Windows |
| 5. Verify | 3 min | Test docker commands |
| 6. Build Images | 5 min | Test with your project |
| **Total** | **~25 min** | **Ready to deploy** |

---

## 🎯 Next Steps After Installation

1. ✅ Docker verified working
2. ✅ Your images built successfully
3. ➡️ **Proceed with Azure deployment**
   - See: `AZURE_CLI_DEPLOYMENT_CHECKLIST.md`
   - Time: ~45 minutes to deploy

---

## 📝 Keep This Handy

After installation, save these commands:

```powershell
# Check Docker status:
docker --version

# Build your images:
docker build -f docker/Dockerfile.backend -t neetpg-backend:latest .
docker build -f docker/Dockerfile.frontend -t neetpg-frontend:latest .

# List your images:
docker images

# Push to Azure (after creating registry):
docker login <your-acr>.azurecr.io
docker push <your-acr>.azurecr.io/neetpg-backend:latest

# View running containers:
docker ps

# View logs:
docker logs <container-name>
```

---

**Installation Time**: 15-25 minutes  
**Status**: Follow these steps in order  
**Support**: If stuck, check Troubleshooting section above
