# 🚀 Quick Azure CLI Deployment - What You Need

## Current Status Check
```
✅ Azure CLI 2.84.0           - Ready
✅ Java 21 LTS                - Ready  
✅ Maven                       - Ready
✅ Node.js 20                  - Ready
✅ Git                         - Ready
✅ Azure Account Logged In     - Ready
✅ Dockerfiles Present         - Ready

❌ DOCKER NOT INSTALLED        - REQUIRED (BLOCKING)
❌ Container Registry          - Need to create
❌ Key Vault                   - Need to create
```

---

## 🔴 BLOCKING ISSUE: INSTALL DOCKER FIRST

```powershell
# Option 1: Chocolatey (if installed)
choco install docker-desktop

# Option 2: Direct Download
# Visit: https://www.docker.com/products/docker-desktop
# Download Docker Desktop for Windows
# Install and restart your Windows PC
# Open Docker Desktop (run as Admin)

# Verify installation:
docker --version
docker run hello-world
```

---

## 📋 Information You Need to Gather

### From Neon PostgreSQL
```
NEON_HOST = _________      # e.g., project.us-east-1.neon.tech
NEON_USER = _________      # e.g., neon_user
NEON_PASSWORD = _________  # Your Neon password
NEON_DB = neetpg           # Database name
```

### To Create/Decide
```
SUBSCRIPTION_ID = d94aa611-c391-4f7e-ac53-526d25ca20b5  ✅ (You have this)
RESOURCE_GROUP = _________             # e.g., rg-neetpg-prod
CONTAINER_REGISTRY = ________          # e.g., acrneetpgprod (must be unique globally)
APP_NAME = _________                   # e.g., neetpg-backend
LOCATION = eastus                      # Your preferred Azure region
JWT_SECRET = _________                 # Generate a strong secret (32+ chars)
CORS_ORIGINS = _________               # e.g., https://neetpg-frontend.azurewebsites.net
```

---

## 🎯 Simple 5-Step Deployment Plan

### Step 1: Install Docker ⏱️ 10-15 min
```powershell
# Download from: https://www.docker.com/products/docker-desktop
# Install and restart PC
# Verify: docker --version
```

### Step 2: Create Azure Resources ⏱️ 5 min
```powershell
$GROUP = "rg-neetpg-prod"
$ACR = "acrneetpgprod"

az group create -n $GROUP -l eastus
az acr create -g $GROUP -n $ACR --sku Basic
```

### Step 3: Build Docker Images ⏱️ 15 min
```powershell
az acr login --name $ACR

az acr build -r $ACR -t neetpg-backend:latest -f docker/Dockerfile.backend .
az acr build -r $ACR -t neetpg-frontend:latest -f docker/Dockerfile.frontend .
```

### Step 4: Deploy to Azure Container Apps ⏱️ 10 min
```powershell
# (See full script below)
az containerapp env create -n neetpg-env -g $GROUP -l eastus

az containerapp create -n neetpg-backend \
  -g $GROUP \
  --image <your-acr>.azurecr.io/neetpg-backend:latest \
  --env-vars PGHOST=<neon-host> PGPORT=5432 PGDATABASE=neetpg ...
```

### Step 5: Verify & Monitor ⏱️ 5 min
```powershell
az containerapp show -g $GROUP -n neetpg-backend
az containerapp logs show -g $GROUP -n neetpg-backend --tail 50
```

**Total Time**: ~45 minutes (first time)

---

## 🎛️ Azure Services You'll Use

| Service | Purpose | Cost | Status |
|---------|---------|------|--------|
| **Resource Group** | Organize resources | Free | Need to create |
| **Container Registry** | Store Docker images | ~$5/mo | Need to create |
| **Container Apps** | Run backend/frontend | ~$15-50/mo | Need to create |
| **Key Vault** | Store secrets safely | ~$0.60/mo | Optional but recommended |
| **Application Insights** | Monitor logs | Free tier available | Optional |

---

## 🔑 Environment Variables Mapping

### For Backend Container App
```
PGHOST          = <from Neon>
PGPORT          = 5432
PGDATABASE      = neetpg
PGUSER          = <from Neon>
PGPASSWORD      = <from Neon>
JWT_SECRET      = <generate strong one>
CORS_ORIGINS    = <your frontend URL>
GEMINI_API_KEY  = <optional>
GROQ_API_KEY    = <optional>
```

### For Frontend Container App
```
VITE_API_URL    = https://<backend-container-app-url>
```

---

## ✅ Pre-Deployment Checklist

Before running any CLI commands, have these ready:

- [ ] Docker installed and running
- [ ] Neon PostgreSQL credentials
- [ ] JWT secret generated
- [ ] Resource group name decided
- [ ] Container registry name (globally unique)
- [ ] AI API keys (if using)
- [ ] Decided on Container Apps vs App Service

---

## 📚 File References

See detailed docs in your project:
- **[AZURE_MIGRATION_READINESS.md](AZURE_MIGRATION_READINESS.md)** - Full architecture analysis
- **[AZURE_CLI_DEPLOYMENT_CHECKLIST.md](AZURE_CLI_DEPLOYMENT_CHECKLIST.md)** - Complete CLI commands

---

## 🆘 Troubleshooting

### Docker not found
```
-> Install Docker Desktop first
-> Restart your PC after installation
-> Test with: docker run hello-world
```

### ACR login fails
```
-> Run: az acr login --name <your-acr-name>
-> Or use: docker login <your-acr>.azurecr.io
```

### Container App won't start
```
-> Check logs: az containerapp logs show -n <app-name> -g <group>
-> Verify environment variables are set
-> Check database connectivity
```

### Connection to Neon fails
```
-> Add ?sslmode=require to JDBC URL
-> Verify SSL certificate
-> Check firewall rules
-> Test connection locally first
```

---

## 🚀 Ready to Deploy?

1. **Install Docker** (if not already done)
2. **Gather credentials** (Neon details)
3. **Run the deployment script** (see detailed checklist)
4. **Monitor with**: `az containerapp logs show ...`

**Total setup time: ~1 hour**

---

**Last Updated**: 2026-03-19  
**Your Azure Subscription**: Azure for Students (d94aa611-c391-4f7e-ac53-526d25ca20b5)
