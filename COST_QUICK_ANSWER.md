# ⚡ Azure Cost: Quick Answer

## Your Question: "Why so many Azure services? Can't we use free only?"

### **YES! Use FREE only** ✅

---

## 📋 CREATE ONLY THESE (Minimal Cost)

| # | Service | Cost | Create? | Why |
|---|---------|------|---------|-----|
| 1 | Resource Group | $0 | ✅ YES | Required container |
| 2 | Container Registry | $0 (yr1) | ✅ YES | Store your images |
| 3 | Container Apps | $0 (yr1) | ✅ YES | Run backend |
| 4 | Static Web Apps | $0 | ✅ YES | Run frontend |
| 5 | Neon PostgreSQL | $0 | ✅ YES | Database (free tier) |

**TOTAL COST YEAR 1**: **$0** ✅

**TOTAL COST YEAR 2+**: **$5-30/month** (Registry $5 + usage)

---

## ❌ SKIP THESE (Wastes Money)

| Service | Typical Cost | Why Skip | Better Alternative |
|---------|------------|----------|-------------------|
| **Key Vault** | $0.60/mo | Not needed yet | Use env variables |
| **Application Insights** | $20/mo | Use free tier instead | Basic logging |
| **Azure Database** | $15+/mo | WAY too expensive | Use Neon (free) |
| **App Service** | $30-100/mo | Always charges | Use Container Apps |

**MONEY SAVED BY SKIPPING**: **$65-135/month** 💰

---

## 💰 YOUR ACTUAL COSTS

### Year 1 (Student Account)
```
What you spend:    $0
Why:               Student credits cover everything
How long:          12 months (while enrolled)
```

### Year 2+ (After Student Credits)
```
What you spend:    $5-30/month
Why:               Container Registry $5 + Container Apps usage
How to reduce:     Scale down, use auto-scaling, delete when not needed
```

---

## 🎯 EXACT SETUP (No Expensive Services)

```powershell
# 1. Create Resource Group (FREE)
az group create -n rg-neetpg -l eastus

# 2. Create Container Registry (FREE for 12 months)
az acr create -g rg-neetpg -n acrneetpg --sku Basic

# 3. Create Container Apps Environment (FREE)
az containerapp env create -n neetpg-env -g rg-neetpg -l eastus

# 4. Deploy Backend (FREE with student credits)
az containerapp create \
  -n neetpg-backend \
  -g rg-neetpg \
  --environment neetpg-env \
  --image acrneetpg.azurecr.io/neetpg-backend:latest \
  --target-port 8080 \
  --ingress external \
  --cpu 0.25 \
  --memory 0.5Gi

# 5. Deploy Frontend (FREE)
az staticwebapp create -n neetpg-frontend -g rg-neetpg

# 6. Database (EXTERNAL - NOT AZURE)
# Use Neon: Create free account at neon.tech

# DONE! Cost: $0/month ✅
```

---

## 📊 Cost Comparison

### What I Originally Suggested (Too Much)
```
Resource Group         $0
Container Registry     $5
Container Apps         $30
Key Vault              $2
App Insights           $20
Database (Azure)       $30
─────────────────────
TOTAL:                 $87/month ❌
```

### Minimal Cost Setup (Recommended)
```
Resource Group         $0
Container Registry     $0 (yr1)
Container Apps         $0 (student credits yr1)
Neon Database          $0 (free tier)
─────────────────────
TOTAL:                 $0/month ✅
```

**DIFFERENCE**: Save **$87/month** 💰

---

## ✅ Why This Works

```
✅ Resource Group        - Just organization, required by Azure
✅ Container Registry    - Free first year, cheap after ($5)
✅ Container Apps        - Pay-for-what-you-use (cheap!)
✅ Static Web Apps       - Permanently free tier available
✅ Neon Database         - Free 5GB tier, no Azure charge
```

```
❌ Key Vault             - Not needed for dev/test
❌ App Insights          - Cool but not essential
❌ Azure Database        - Expensive and unnecessary
❌ App Service           - Wrong tool, too expensive
```

---

## 🎁 Student Benefits

You have: ~$50-100/month free credits

This covers:
```
✅ Container Apps usage       (~$30-40/month)
✅ Any other Azure services   (remaining credits)
```

**Result**: Everything free during school! 🎓

---

## 📈 Long-Term Costs

| Period | Cost/Month | Notes |
|--------|-----------|-------|
| Year 1 (Student) | $0 | All covered by credits |
| Year 2 (Post-Student) | $5-30 | Registry $5 + usage $0-25 |
| Year 3+ (Production) | $20-50 | If more users/data |

---

## 🚀 ABSOLUTE MINIMUM

Just deploy with:
1. Docker images (local build)
2. Container Registry ($0)
3. Container Apps ($0 yr1)
4. Neon Database ($0)

That's it. Everything else is extra.

---

## 💡 Three Words

**Use. Free. Tiers.**

Stop. 🛑

---

**Your Real Cost**: $0/month (Year 1) → $5-30/month (Year 2+)

**Don't pay for**: Key Vault, App Insights, Azure Database, App Service

**Use instead**: Container Apps + Static Web Apps + Neon

✅ **Ready?** Go deploy! It's FREE!
