# Azure CLI Deployment Requirements Checklist

**Date**: March 19, 2026  
**Status**: Checking your local environment...

---

## 📊 Your Current Environment Status

### ✅ Already Installed & Ready
```
Azure CLI:           2.84.0 ✅
  - containerapp:    1.3.0b4 ✅
Java Version:        21.0.9 ✅
Azure Account:       Logged in ✅
  - Subscription:    "Azure for Students"
  - User:            21csraj062@ldce.ac.in
```

### ❌ Missing / Not Installed
```
Docker:              NOT INSTALLED ❌ (REQUIRED)
Docker Desktop:      NOT INSTALLED ❌ (REQUIRED)
```

---

## 📋 REQUIRED PREREQUISITES FOR AZURE CLI DEPLOYMENT

### 1️⃣ **CRITICAL: Install Docker & Docker Desktop**
| Requirement | Current | Status | Impact |
|-------------|---------|--------|--------|
| Docker CLI | ❌ Not found | **REQUIRED** | 🔴 Blocks deployment |
| Docker Desktop | ❌ Not found | **REQUIRED** | 🔴 Blocks container build |
| Multi-stage build support | ✅ Configured | Ready | 🟢 Good |

**Installation Steps**:
```powershell
# Option 1: Download Docker Desktop
# Visit: https://www.docker.com/products/docker-desktop
# Install and restart

# Option 2: WSL2 Backend (Recommended for Windows)
# 1. Enable WSL2
# 2. Install Docker Desktop
# 3. Configure Docker Desktop to use WSL2 backend
```

---

## 📦 ALL COMPONENTS NEEDED FOR CLI DEPLOYMENT

### ✅ What You Already Have
```
[✅] Azure CLI 2.84.0
[✅] Java 21 LTS
[✅] Maven (in backend/)
[✅] Git & GitHub authentication
[✅] Node.js 20 (for frontend build)
[✅] Azure Account & Active Subscription
[✅] Dockerfiles (Root + Backend + Frontend)
```

### ❌ What You Need to Get/Create
```
[❌] Docker Desktop (INSTALL FIRST)
[❌] Docker CLI (comes with Desktop)
[❌] Azure Resource Group (az group create)
[❌] Azure Container Registry (az acr create)
[❌] Neon PostgreSQL account credentials
[❌] Azure Key Vault (for secrets)
[❌] Deployment script (we can generate)
```

### 🆕 What You Need to Create
```
[🆕] .env.azure - Azure configuration
[🆕] deploy.sh / deploy.ps1 - CLI deployment script
[🆕] .azure/cli-deploy.sh - CLI deployment automation
```

---

## 🔐 CREDENTIALS & INFORMATION NEEDED

### From Your Setup
- [❓] Neon PostgreSQL connection details:
  - `NEON_HOST`: (e.g., xyz.us-east-1.neon.tech)
  - `NEON_USER`: (e.g., neon_user)
  - `NEON_PASSWORD`: (your password)
- [❓] JWT Secret (for backend)
- [❓] CORS Origins (frontend domain)
- [❓] Gemini API Key (optional, for AI features)
- [❓] Groq API Key (optional, for AI fallback)

### To Generate
- [🆕] Azure Subscription ID: `d94aa611-c391-4f7e-ac53-526d25ca20b5`
- [🆕] Resource Group Name: (you decide, e.g., `rg-neetpg-prod`)
- [🆕] Container Registry Name: (you decide, e.g., `acrneetpgprod`)
- [🆕] App Service/Container App Name: (you decide, e.g., `neetpg-backend`)

---

## 🚀 COMPLETE CLI DEPLOYMENT WORKFLOW

### Phase 1: Environment Setup (Before running scripts)
```bash
# 1. Install Docker Desktop
# 2. Start Docker Desktop
# 3. Test Docker:
docker --version

# 4. Login to Azure (already done ✅)
az account show

# 5. Set variables
$SUBSCRIPTION_ID = "d94aa611-c391-4f7e-ac53-526d25ca20b5"
$RESOURCE_GROUP = "rg-neetpg-prod"
$ACR_NAME = "acrneetpgprod"
$LOCATION = "eastus"  # or your preferred region
```

### Phase 2: Create Azure Resources
```bash
# 1. Create Resource Group
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

# 2. Create Container Registry
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic

# 3. Create Key Vault
az keyvault create \
  --resource-group $RESOURCE_GROUP \
  --name "kv-neetpg-prod" \
  --location $LOCATION

# 4. Add secrets to Key Vault
az keyvault secret set \
  --vault-name "kv-neetpg-prod" \
  --name "DATABASE-URL" \
  --value "jdbc:postgresql://<neon-host>:5432/neetpg?sslmode=require"

az keyvault secret set \
  --vault-name "kv-neetpg-prod" \
  --name "DATABASE-USER" \
  --value "<neon-user>"

az keyvault secret set \
  --vault-name "kv-neetpg-prod" \
  --name "DATABASE-PASSWORD" \
  --value "<neon-password>"

az keyvault secret set \
  --vault-name "kv-neetpg-prod" \
  --name "JWT-SECRET" \
  --value "<your-jwt-secret>"
```

### Phase 3: Build & Push Docker Images
```bash
# 1. Login to Container Registry
az acr login --name $ACR_NAME

# 2. Build Backend Image
az acr build \
  --registry $ACR_NAME \
  --image neetpg-backend:latest \
  --file docker/Dockerfile.backend \
  .

# 3. Build Frontend Image
az acr build \
  --registry $ACR_NAME \
  --image neetpg-frontend:latest \
  --file docker/Dockerfile.frontend \
  .
```

### Phase 4: Deploy to Azure Container Apps
```bash
# 1. Get ACR credentials
$ACR_URL = "$(az acr show -n $ACR_NAME -g $RESOURCE_GROUP --query loginServer -o tsv)"
$ACR_USER = "$(az acr credential show -n $ACR_NAME --query username -o tsv)"
$ACR_PASS = "$(az acr credential show -n $ACR_NAME --query 'passwords[0].value' -o tsv)"

# 2. Create Container App Environment
az containerapp env create \
  --resource-group $RESOURCE_GROUP \
  --name neetpg-env \
  --location $LOCATION

# 3. Deploy Backend Container App
az containerapp create \
  --resource-group $RESOURCE_GROUP \
  --name neetpg-backend \
  --environment neetpg-env \
  --image "$ACR_URL/neetpg-backend:latest" \
  --registry-server $ACR_URL \
  --registry-username $ACR_USER \
  --registry-password $ACR_PASS \
  --target-port 8080 \
  --ingress external \
  --env-vars \
    PGHOST=<neon-host> \
    PGPORT=5432 \
    PGDATABASE=neetpg \
    PGUSER=<neon-user> \
    PGPASSWORD=<neon-password> \
    JWT_SECRET=<your-secret> \
    CORS_ORIGINS=<frontend-url>

# 4. Deploy Frontend Container App
$BACKEND_URL = "$(az containerapp show -g $RESOURCE_GROUP -n neetpg-backend --query 'properties.configuration.ingress.fqdn' -o tsv)"

az containerapp create \
  --resource-group $RESOURCE_GROUP \
  --name neetpg-frontend \
  --environment neetpg-env \
  --image "$ACR_URL/neetpg-frontend:latest" \
  --registry-server $ACR_URL \
  --registry-username $ACR_USER \
  --registry-password $ACR_PASS \
  --target-port 80 \
  --ingress external \
  --env-vars \
    VITE_API_URL=https://$BACKEND_URL
```

### Phase 5: Verification & Monitoring
```bash
# Check deployment status
az containerapp show -g $RESOURCE_GROUP -n neetpg-backend

# View logs
az containerapp logs show \
  --name neetpg-backend \
  --resource-group $RESOURCE_GROUP \
  --tail 50

# Get application URLs
echo "Backend URL: https://$(az containerapp show -g $RESOURCE_GROUP -n neetpg-backend --query 'properties.configuration.ingress.fqdn' -o tsv)"
echo "Frontend URL: https://$(az containerapp show -g $RESOURCE_GROUP -n neetpg-frontend --query 'properties.configuration.ingress.fqdn' -o tsv)"
```

---

## 🛠️ ALTERNATIVE: Azure App Service (Simpler Option)

If you prefer a simpler setup without Container Apps:

```bash
# 1. Create App Service Plan
az appservice plan create \
  --name neetpg-plan \
  --resource-group $RESOURCE_GROUP \
  --sku B2 \
  --is-linux

# 2. Deploy Backend
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan neetpg-plan \
  --name neetpg-backend-app \
  --deployment-container-image-name $ACR_URL/neetpg-backend:latest

# 3. Deploy Frontend
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan neetpg-plan \
  --name neetpg-frontend-app \
  --deployment-container-image-name $ACR_URL/neetpg-frontend:latest

# 4. Configure app settings
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name neetpg-backend-app \
  --settings \
    PGHOST=<neon-host> \
    PGport=5432 \
    PGDATABASE=neetpg \
    PGUSER=<neon-user> \
    PGPASSWORD=<neon-password> \
    WEBSITES_PORT=8080
```

---

## 📋 COMPLETE CHECKLIST BEFORE DEPLOYMENT

### Must-Have (Blocking)
- [ ] Docker Desktop installed and running
- [ ] Docker CLI working (`docker --version` returns version)
- [ ] Azure CLI authenticated (`az account show` returns account)
- [ ] Neon PostgreSQL account created with connection details
- [ ] Database name & credentials ready
- [ ] JWT secret generated (at least 32 characters)
- [ ] CORS origins decided (e.g., frontend domain)

### Should-Have (Recommended)
- [ ] Azure Key Vault created for secrets
- [ ] Resource group name decided
- [ ] Container registry name decided (must be unique globally)
- [ ] Azure region selected (default: eastus)
- [ ] API keys (Gemini/Groq) if using AI features
- [ ] Deployment script created

### Nice-to-Have (Optional)
- [ ] GitHub Actions workflow for auto-deployment
- [ ] Azure Monitor configured for logging
- [ ] Custom domain configured
- [ ] HTTPS certificates configured

---

## ⚠️ IMPORTANT NOTES

### Cost Implications
- **Container Registry (Basic)**: ~$5/month
- **Container Apps**: $0.40/vCPU/hour (first 4vCPU free with student account)
- **Key Vault**: $0.6/10K operations
- **Neon PostgreSQL**: Free tier + pay-as-you-go

**Estimated Monthly Cost**: $10-50 for small usage

### Security Best Practices
1. **Never hardcode secrets** - use Key Vault
2. **Use Managed Identity** when available
3. **Enable HTTPS** on all endpoints
4. **Restrict ACR access** to authorized IPs
5. **Enable audit logging** for Key Vault

### Common Pitfalls
1. ❌ Using `--output tsv` without proper parsing
2. ❌ Forgetting to login to ACR before push
3. ❌ Not setting environment variables in container app
4. ❌ Firewall rules blocking Neon connections
5. ❌ Container app not exposing ingress (external)

---

## 🎯 NEXT STEPS

1. **IMMEDIATE** (Today):
   - [ ] Install Docker Desktop
   - [ ] Test Docker with `docker --version`
   - [ ] Gather Neon PostgreSQL credentials
   - [ ] Generate JWT secret

2. **SHORT-TERM** (Tomorrow):
   - [ ] Create Azure Resource Group
   - [ ] Set up Container Registry
   - [ ] Create Key Vault with secrets
   - [ ] Build and push Docker images

3. **DEPLOYMENT** (This week):
   - [ ] Deploy to Container Apps or App Service
   - [ ] Verify backend connectivity
   - [ ] Test database migrations
   - [ ] Verify frontend can talk to backend

---

## 📞 Quick Reference Commands

```powershell
# Install Docker (Windows)
choco install docker-desktop

# Start Docker
docker run hello-world

# Login to Azure
az login

# Set subscription
az account set --subscription "d94aa611-c391-4f7e-ac53-526d25ca20b5"

# Check resource groups
az group list -o table

# View container apps logs
az containerapp logs show -n neetpg-backend -g rg-neetpg-prod
```

---

**Document Version**: 1.0  
**Last Updated**: 2026-03-19  
**Status**: Ready for action
