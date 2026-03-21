# Azure Deployment Options - Quick Comparison

## Your Options for Deploying with Azure CLI

### Option 1: **Container Apps** ✅ RECOMMENDED
```
Pros:
✅ Modern, serverless container platform
✅ Pay per CPU-second (cheap for variable workloads)
✅ Auto-scaling built-in
✅ Great for microservices
✅ Managed by Microsoft

Cons:
❌ Slightly more complex setup
❌ Newer service (still evolving)

Best For: Your setup because you have variable student traffic
Cost: $0.40/vCPU/hour (student account has $50/month free tier)
Time to Deploy: 30-45 minutes
Recommended: YES ⭐⭐⭐⭐⭐
```

**Sample Command**:
```powershell
az containerapp create \
  --name neetpg-backend \
  --resource-group rg-neetpg-prod \
  --environment neetpg-env \
  --image acrneetpgprod.azurecr.io/neetpg-backend:latest \
  --target-port 8080
```

---

### Option 2: **App Service** (Simpler Alternative)
```
Pros:
✅ Simpler setup and management
✅ Long-standing service (mature)
✅ Easy FTP/Git deployment
✅ Built-in backup/restore

Cons:
❌ No auto-scaling (unless in Premium tier)
❌ Pay for entire instance even when idle
❌ Not as flexible for containers

Best For: Simple deployments, always-on apps
Cost: $30-100/month (even when idle)
Time to Deploy: 20-30 minutes
Recommended: NO (overkill for your needs)
```

**Sample Command**:
```powershell
az webapp create \
  --name neetpg-backend \
  --resource-group rg-neetpg-prod \
  --plan neetpg-appservice-plan \
  --deployment-container-image-name acrneetpgprod.azurecr.io/neetpg-backend:latest
```

---

### Option 3: **Kubernetes (AKS)** - NOT RECOMMENDED
```
Pros:
✅ Full control and flexibility
✅ Enterprise-grade orchestration

Cons:
❌ Overkill for your project
❌ Expensive ($50+/month minimum)
❌ Complex setup and management
❌ High operational overhead

Best For: Large-scale production systems
Cost: $50-200+/month
Time to Deploy: 2-3 hours
Recommended: NO (too complex for now)
```

---

## 🎯 QUICK DECISION GUIDE

```
Will your app have variable traffic?
    YES → Use Container Apps ✅
    NO  → Use App Service

Do you need auto-scaling?
    YES → Container Apps ✅
    NO  → App Service (but Container Apps still better)

Do you want lowest cost?
    YES → Container Apps ✅

Do you want simplest setup?
    YES → App Service (marginally simpler)

Will it be always-on 24/7?
    NO  → Container Apps ✅ (pay per second)
    YES → App Service (but Container Apps still cheaper for variable load)
```

---

## 📊 Cost Comparison (Estimated Monthly)

| Component | Container Apps | App Service | AKS |
|-----------|----------------|------------|-----|
| Compute | $15-40 | $40-100 | $50-200 |
| Container Registry | $5 | $5 | $5 |
| Database (Neon) | Variable | Variable | Variable |
| **Total** | **$20-50** | **$50-110** | **$60-210** |

---

## ⚡ CLI Deployment Roadmap

### Container Apps (Recommended Path)
```
1. Create Resource Group
   └─ az group create

2. Create Container Registry
   └─ az acr create

3. Build & Push Images
   └─ az acr build

4. Create Container App Environment
   └─ az containerapp env create

5. Deploy Backend Container App
   └─ az containerapp create (backend)

6. Deploy Frontend Container App
   └─ az containerapp create (frontend)

Total: ~20 CLI commands
```

### App Service (Alternative Path)
```
1. Create Resource Group
   └─ az group create

2. Create Container Registry
   └─ az acr create

3. Build & Push Images
   └─ az acr build

4. Create App Service Plan
   └─ az appservice plan create

5. Create Backend Web App
   └─ az webapp create (backend)

6. Create Frontend Web App
   └─ az webapp create (frontend)

Total: ~18 CLI commands (slightly fewer)
```

---

## 🔧 Quick Reference: What Each CLI Command Does

```powershell
# Create infrastructure containers
az group create              # Logical folder for all resources
az acr create                # Container registry (like DockerHub)
az keyvault create           # Secure storage for passwords/secrets

# Build containers
az acr build                 # Build Docker image in cloud
az acr run                   # Run command in container registry

# Deploy containers
az containerapp create       # Deploy to Container Apps
az containerapp update       # Update running container
az containerapp restart      # Restart container

# Monitoring
az containerapp logs show    # View application logs
az containerapp show         # View container details
az monitor metrics list      # View performance metrics
```

---

## 📝 Your Recommended Deployment Script (Container Apps)

Save this as `deploy.ps1`:

```powershell
#!/usr/bin/env pwsh

# Configuration
$SUBSCRIPTION = "d94aa611-c391-4f7e-ac53-526d25ca20b5"
$RESOURCE_GROUP = "rg-neetpg-prod"
$ACR_NAME = "acrneetpgprod"
$LOCATION = "eastus"
$APP_ENV = "neetpg-env"

# Neon Database
$NEON_HOST = Read-Host "Enter Neon host"
$NEON_USER = Read-Host "Enter Neon user"
$NEON_PASSWORD = Read-Host "Enter Neon password" -AsSecureString
$NEON_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemPtr($NEON_PASSWORD))

# Generated
$JWT_SECRET = [System.Web.Security.Membership]::GeneratePassword(32, 4)

Write-Host "🚀 Starting Azure Deployment..." -ForegroundColor Cyan

# Step 1: Create Resource Group
Write-Host "1️⃣  Creating Resource Group..." -ForegroundColor Yellow
az group create -n $RESOURCE_GROUP -l $LOCATION

# Step 2: Create Container Registry
Write-Host "2️⃣  Creating Container Registry..." -ForegroundColor Yellow
az acr create -g $RESOURCE_GROUP -n $ACR_NAME --sku Basic

# Step 3: Build & Push Images
Write-Host "3️⃣  Building & Pushing Docker Images..." -ForegroundColor Yellow
az acr login --name $ACR_NAME
az acr build -r $ACR_NAME -t neetpg-backend:latest -f docker/Dockerfile.backend .
az acr build -r $ACR_NAME -t neetpg-frontend:latest -f docker/Dockerfile.frontend .

# Step 4: Create Container App Environment
Write-Host "4️⃣  Creating Container App Environment..." -ForegroundColor Yellow
az containerapp env create -n $APP_ENV -g $RESOURCE_GROUP -l $LOCATION

# Step 5: Deploy Backend
Write-Host "5️⃣  Deploying Backend Container App..." -ForegroundColor Yellow
$ACR_URL = "$(az acr show -n $ACR_NAME -g $RESOURCE_GROUP --query loginServer -o tsv)"

az containerapp create `
  -n neetpg-backend `
  -g $RESOURCE_GROUP `
  --environment $APP_ENV `
  --image "$ACR_URL/neetpg-backend:latest" `
  --registry-server $ACR_URL `
  --target-port 8080 `
  --ingress external `
  --env-vars `
    PGHOST=$NEON_HOST `
    PGPORT=5432 `
    PGDATABASE=neetpg `
    PGUSER=$NEON_USER `
    PGPASSWORD=$NEON_PASSWORD `
    JWT_SECRET=$JWT_SECRET `
    CORS_ORIGINS="*"

# Step 6: Deploy Frontend
Write-Host "6️⃣  Deploying Frontend Container App..." -ForegroundColor Yellow
$BACKEND_URL = "$(az containerapp show -n neetpg-backend -g $RESOURCE_GROUP --query 'properties.configuration.ingress.fqdn' -o tsv)"

az containerapp create `
  -n neetpg-frontend `
  -g $RESOURCE_GROUP `
  --environment $APP_ENV `
  --image "$ACR_URL/neetpg-frontend:latest" `
  --registry-server $ACR_URL `
  --target-port 80 `
  --ingress external `
  --env-vars VITE_API_URL="https://$BACKEND_URL"

Write-Host "✅ Deployment Complete!" -ForegroundColor Green
Write-Host "Backend URL: https://$BACKEND_URL" -ForegroundColor Cyan
Write-Host "Frontend URL: https://$(az containerapp show -n neetpg-frontend -g $RESOURCE_GROUP --query 'properties.configuration.ingress.fqdn' -o tsv)" -ForegroundColor Cyan
```

---

## 🎯 DECISION: Which Option to Choose?

**For NeetPG Platform**: **Choose Container Apps** ✅

**Why**:
1. Perfect for variable/educational workloads
2. Cheapest option ($0-50/month)
3. Auto-scaling for traffic spikes
4. Modern Azure platform
5. Easy to scale up/down
6. Good learning experience

---

**Status**: Ready to proceed  
**Time to Deploy**: 45 minutes (once Docker is installed)  
**Next Step**: Install Docker Desktop, then gather Neon credentials
