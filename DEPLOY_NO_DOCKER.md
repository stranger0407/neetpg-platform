# 🚀 Deploy WITHOUT Docker - App Service & Static Web Apps

## 📊 What You'll Deploy

| Component | Service | Cost | Time |
|-----------|---------|------|------|
| **Backend (Java)** | App Service | $10-15/mo | 15 min |
| **Frontend (React)** | Static Web Apps | FREE | 10 min |
| **Database** | Neon (external) | FREE | Already set up |
| **TOTAL** | | $10-15/mo | 50 min |

---

## ✅ Prerequisites (Ready to Go)

- ✅ Maven installed (Java build tool)
- ✅ npm installed (Node build tool)
- ✅ Azure CLI logged in
- ✅ Neon database connected
- ✅ Resource group exists (neetpg-rg)

---

## 🎯 Phase 1: Build Backend (Maven) - 10 minutes

### Step 1: Navigate to Backend

```powershell
cd d:\devlopment\Neetpg\backend
```

### Step 2: Build JAR File

```powershell
mvn clean package -DskipTests
```

**What this does:**
- Compiles Java code
- Packages into JAR file
- Skips tests (faster)

**Expected output:**
```
BUILD SUCCESS
Total time: 2:45 min
```

**Where JAR is created:**
```
target/neetpg-0.0.1-SNAPSHOT.jar  (or similar name)
```

### Step 3: Verify JAR Exists

```powershell
Get-ChildItem target/*.jar
```

**Should show:**
```
neetpg-0.0.1-SNAPSHOT.jar
```

---

## 🎯 Phase 2: Build Frontend (npm) - 10 minutes

### Step 1: Navigate to Frontend

```powershell
cd d:\devlopment\Neetpg\frontend
```

### Step 2: Build React App

```powershell
npm run build
```

**What this does:**
- Compiles React/TypeScript
- Optimizes bundle
- Creates `dist/` folder

**Expected output:**
```
npm notice created a lockfile as package-lock.json
dist/ files created
```

### Step 3: Verify Build

```powershell
Get-ChildItem dist/
```

**Should show:**
```
index.html
assets/  (folder with JS/CSS)
```

---

## 🎯 Phase 3: Deploy Backend to App Service - 15 minutes

### Step 1: Navigate Back to Project Root

```powershell
cd d:\devlopment\Neetpg
```

### Step 2: Create App Service Plan

```powershell
az appservice plan create `
  --name neetpg-plan `
  --resource-group neetpg-rg `
  --sku B1 `
  --is-linux
```

**What this does:** Creates hosting infrastructure for your backend
**Cost:** Included in B1 ($10-15/mo)

### Step 3: Create Web App (Backend)

```powershell
az webapp create `
  --name neetpg-backend `
  --resource-group neetpg-rg `
  --plan neetpg-plan `
  --runtime "java:21-java21"
```

**What this does:** Creates web app container for your Java backend
**Wait:** 2-3 minutes for creation

### Step 4: Configure Database Environment Variables

Replace with YOUR Neon credentials:

```powershell
az webapp config appsettings set `
  --name neetpg-backend `
  --resource-group neetpg-rg `
  --settings `
    PGHOST="ep-lively-silence-a8rqu8ps.eastus2.azure.neon.tech" `
    PGPORT="5432" `
    PGDATABASE="neetpg" `
    PGUSER="neondb_owner" `
    PGPASSWORD="npg_kMYt9CQOgF2c" `
    PORT="8080" `
    CORS_ORIGINS="*" `
    JWT_SECRET="your-jwt-secret-key"
```

### Step 5: Deploy JAR to App Service

```powershell
# Get JAR file path
$jarFile = Get-ChildItem -Path "backend/target" -Filter "*.jar" -Recurse | Select -First 1

# Deploy
az webapp up `
  --name neetpg-backend `
  --resource-group neetpg-rg `
  --src-path $jarFile.FullName
```

**Wait:** 3-5 minutes for deployment

### Step 6: Check Deployment Status

```powershell
az webapp show `
  --name neetpg-backend `
  --resource-group neetpg-rg `
  --query "state"
```

**Should show:**
```
"Running"
```

### Step 7: Get Backend URL

```powershell
az webapp show `
  --name neetpg-backend `
  --resource-group neetpg-rg `
  --query "defaultHostName" `
  --output tsv
```

**You'll get:** `neetpg-backend.azurewebsites.net` (Save this!)

### Step 8: Test Backend is Running

```powershell
$backendUrl = "https://neetpg-backend.azurewebsites.net"
Invoke-WebRequest "$backendUrl/api/health" -SkipHttpsValidation
```

**Expected:** Response code 200 (if health endpoint exists)

---

## 🎯 Phase 4: Deploy Frontend to Static Web Apps - 10 minutes

### Step 1: Create Static Web App

```powershell
az staticwebapp create `
  --name neetpg-frontend `
  --resource-group neetpg-rg `
  --location eastus2
```

**Wait:** 2-3 minutes for creation

### Step 2: Get Frontend URL

```powershell
az staticwebapp show `
  --name neetpg-frontend `
  --resource-group neetpg-rg `
  --query "defaultHostName" `
  --output tsv
```

**You'll get:** `neetpg-frontend.azurestaticapps.net` (Save this!)

### Step 3: Configure Frontend Environment

Open `frontend/src/api.js` and update the backend URL:

```javascript
// OLD:
const API_BASE_URL = "http://localhost:8080"

// NEW:
const API_BASE_URL = "https://neetpg-backend.azurewebsites.net"
```

### Step 4: Rebuild Frontend

```powershell
cd frontend
npm run build
```

### Step 5: Deploy Frontend Files

```powershell
# Compress dist folder
$distPath = "./dist"
$zipPath = "./dist.zip"
Compress-Archive -Path $distPath -DestinationPath $zipPath -Force

# Deploy to Static Web Apps
az staticwebapp file upload `
  --name neetpg-frontend `
  --resource-group neetpg-rg `
  --source-path $zipPath
```

**Wait:** 1-2 minutes for upload

---

## 🎯 Phase 5: End-to-End Testing - 5 minutes

### Test 1: Backend is Running

```powershell
Invoke-WebRequest "https://neetpg-backend.azurewebsites.net" -SkipHttpsValidation
```

**Should get:** 200 response

### Test 2: Frontend is Accessible

```powershell
Invoke-WebRequest "https://neetpg-frontend.azurestaticapps.net" -SkipHttpsValidation
```

**Should get:** HTML page

### Test 3: Database Connection

In PowerShell, test your database:

```powershell
neonctl sql "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema='public';"
```

**Should show:** Number of tables

### Test 4: Open in Browser

1. Go to: `https://neetpg-frontend.azurestaticapps.net`
2. Try login (if you have auth)
3. Check if it connects to backend
4. Verify data loads

---

## 📋 Complete Commands (Copy-Paste)

### Build Phase

```powershell
# Build backend
cd d:\devlopment\Neetpg\backend
mvn clean package -DskipTests

# Build frontend
cd d:\devlopment\Neetpg\frontend
npm run build

# Back to root
cd d:\devlopment\Neetpg
```

### Deploy Phase

```powershell
# Create infrastructure
az appservice plan create --name neetpg-plan --resource-group neetpg-rg --sku B1 --is-linux

az webapp create --name neetpg-backend --resource-group neetpg-rg --plan neetpg-plan --runtime "java:21-java21"

# Configure database (UPDATE CREDENTIALS!)
az webapp config appsettings set --name neetpg-backend --resource-group neetpg-rg --settings `
  PGHOST="ep-lively-silence-a8rqu8ps.eastus2.azure.neon.tech" `
  PGPORT="5432" `
  PGDATABASE="neetpg" `
  PGUSER="neondb_owner" `
  PGPASSWORD="npg_kMYt9CQOgF2c" `
  PORT="8080" `
  CORS_ORIGINS="*"

# Deploy backend
$jarFile = Get-ChildItem -Path "backend/target" -Filter "*.jar" -Recurse | Select -First 1
az webapp up --name neetpg-backend --resource-group neetpg-rg --src-path $jarFile.FullName

# Create static web app
az staticwebapp create --name neetpg-frontend --resource-group neetpg-rg --location eastus2

# Deploy frontend
$distPath = "./frontend/dist"
$zipPath = "./dist.zip"
Compress-Archive -Path $distPath -DestinationPath $zipPath -Force
az staticwebapp file upload --name neetpg-frontend --resource-group neetpg-rg --source-path $zipPath
```

---

## ⏱️ Timeline

| Phase | Task | Time |
|-------|------|------|
| 1 | Build backend JAR | 5 min |
| 2 | Build frontend | 5 min |
| 3 | Create App Service | 5 min |
| 4 | Deploy backend | 5 min |
| 5 | Create Static Web App | 3 min |
| 6 | Deploy frontend | 3 min |
| 7 | Test everything | 5 min |
| **TOTAL** | **Live in Azure** | **~40 min** |

---

## 💰 Cost Breakdown

### Monthly Cost

```
App Service (B1):      $10-15/mo
Static Web Apps:       FREE
Neon Database:         FREE
─────────────────────────────
TOTAL:                 $10-15/mo
```

### Your Student Credits

- Azure for Students: $50-100/month
- Your usage: $10-15/month
- **Balance: $35-90/month remaining** ✅

---

## 🔄 After Deployment

You can:

1. **View logs:**
   ```powershell
   az webapp log tail --name neetpg-backend --resource-group neetpg-rg
   ```

2. **Update backend:**
   ```powershell
   # Rebuild JAR and redeploy
   mvn clean package -DskipTests
   az webapp up --name neetpg-backend --resource-group neetpg-rg --src-path <jar-path>
   ```

3. **Update frontend:**
   ```powershell
   # Rebuild and redeploy
   npm run build
   # Deploy new dist folder
   ```

4. **Scale up if needed:**
   ```powershell
   az appservice plan update --name neetpg-plan --sku S1
   ```

---

## ✅ URLs After Deployment

**Backend:** `https://neetpg-backend.azurewebsites.net`

**Frontend:** `https://neetpg-frontend.azurestaticapps.net`

**Neon Dashboard:** `https://neon.tech/app/projects`

---

## 🚀 Ready to Deploy?

**Start with Phase 1:** Build backend!

```powershell
cd d:\devlopment\Neetpg\backend
mvn clean package -DskipTests
```

**Tell me when ready for next phase!**
