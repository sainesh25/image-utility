# Railway Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying the image-utility application to Railway platform.

**Railway** is a modern cloud platform that automatically detects and deploys Maven projects without requiring Docker configuration.

---

## Prerequisites

### 1. Accounts Required

- **GitHub Account** - For code hosting
- **Railway Account** - Sign up at https://railway.app

### 2. Local Setup

- Git installed
- Maven installed (optional - Railway builds remotely)
- Project with `pom.xml` file

---

## Deployment Steps

### Step 1: Prepare Your Code

#### 1.1 Initialize Git Repository

```bash
cd d:\java_practicals\image-utility

# Initialize git (if not already done)
git init

# Create .gitignore
```

Create `.gitignore` file:

```
# Build outputs
target/
build/
*.war
*.jar

# IDE files
.idea/
.vscode/
.settings/
.classpath
.project
*.iml

# OS files
.DS_Store
Thumbs.db

# Tomcat
TOMCAT_HOME/

# Logs
*.log

# Local configuration
*.local
```

#### 1.2 Commit Your Code

```bash
# Add all files
git add .

# Commit
git commit -m "Add Maven support for Railway deployment"
```

---

### Step 2: Create GitHub Repository

#### 2.1 On GitHub

1. Go to https://github.com
2. Click **"New repository"**
3. Repository name: `image-utility` (or your choice)
4. Keep it **Public** or **Private**
5. **Don't** initialize with README (you already have code)
6. Click **"Create repository"**

#### 2.2 Push to GitHub

```bash
# Add remote
git remote add origin https://github.com/YOUR_USERNAME/image-utility.git

# Push code
git branch -M main
git push -u origin main
```

---

### Step 3: Deploy to Railway

#### 3.1 Sign Up / Login

1. Visit https://railway.app
2. Click **"Login"**
3. Sign in with **GitHub**

#### 3.2 Create New Project

1. Click **"New Project"**
2. Select **"Deploy from GitHub repo"**
3. **Authorize Railway** to access your GitHub account
4. Select your **`image-utility`** repository

#### 3.3 Automatic Detection

Railway will:
- ✅ Detect `pom.xml` automatically
- ✅ Use **Nixpacks** build system
- ✅ Run `mvn clean install`
- ✅ Deploy the WAR file to Tomcat
- ✅ Assign a public URL

#### 3.4 Monitor Deployment

Watch the deployment logs:
- Build phase (Maven compilation)
- Deploy phase (Tomcat startup)
- Success message with URL

**Expected logs:**
```
[INFO] Building war: /app/target/image-utility.war
[INFO] BUILD SUCCESS
Starting Tomcat...
Server startup in [2345] milliseconds
```

---

### Step 4: Access Your Application

#### 4.1 Get Your URL

Railway provides a URL like:
```
https://image-utility-production.up.railway.app
```

or

```
https://your-project-name.railway.app
```

#### 4.2 Test the Application

1. Open the Railway-provided URL
2. Upload test images
3. Convert to PDF
4. Download and verify

---

## Configuration

### Environment Variables (Optional)

If you need to configure the app:

1. Go to Railway dashboard
2. Select your project
3. Click **"Variables"** tab
4. Add variables:

| Variable | Value | Purpose |
|----------|-------|---------|
| `JAVA_OPTS` | `-Xmx512m` | Limit memory usage |
| `PORT` | `8080` | Application port (auto-set) |

**Note:** Railway automatically sets `$PORT` - your app should use it.

---

## Custom Domain (Optional)

### Add Your Domain

1. Go to **Settings** → **Domains**
2. Click **"Add Custom Domain"**
3. Enter your domain: `yourdomain.com`
4. Update DNS records:
   ```
   CNAME record: yourdomain.com → your-project.up.railway.app
   ```
5. Wait for DNS propagation (~5-30 minutes)

---

## Monitoring

### View Logs

**Real-time logs:**
1. Railway dashboard
2. Select your project
3. Click **"Deployments"**
4. Click on the deployment
5. View **"Logs"** tab

**Example logs:**
```
[Servlet] Image uploaded: 3 files
[ImageUtils] Processing image: IMG_1234.jpg
[ImageUtils] EXIF Orientation: 6
[ImageUtils] PDF created: output_1703516789123.pdf
```

### Metrics

Railway provides:
- **CPU usage**
- **Memory usage**
- **Network traffic**
- **Build time**
- **Request count**

Access via **"Metrics"** tab.

---

## Cost & Limits

### Free Tier

Railway offers:
- **$5 free credit/month**
- **500 hours execution time**
- **100 GB bandwidth**

**Typical usage for this app:**
- Small traffic: ~$1-2/month
- Medium traffic: ~$3-5/month

### Hobby Plan

- **$5/month minimum**
- Pay-as-you-go beyond free tier
- No time limits

---

## Troubleshooting

### Issue 1: Build Fails

**Error:** `Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin`

**Solution:**
1. Check Java version in `pom.xml` matches available Java on Railway
2. Verify all dependencies are correct
3. Check compilation errors in logs

---

### Issue 2: App Doesn't Start

**Error:** `Application failed to respond`

**Solution:**
1. Check Railway logs for startup errors
2. Verify Tomcat is starting correctly
3. Ensure `PORT` environment variable is respected

---

### Issue 3: File Upload Fails

**Error:** `No such file or directory: /uploads`

**Solution:**
Railway uses **ephemeral storage** - this is expected.
Your app already handles this by:
1. Creating upload directory on demand
2. Deleting files after PDF creation

**Check:** `ImageServlet.java` line 28-30 creates directory automatically.

---

### Issue 4: OutOfMemoryError

**Error:** `java.lang.OutOfMemoryError`

**Solution:**
Add environment variable:
```
JAVA_OPTS=-Xmx512m -Xms256m
```

Or upgrade Railway plan for more memory.

---

## Updating Your Deployment

### Push Changes

```bash
# Make code changes
# Commit changes
git add .
git commit -m "Update feature X"

# Push to GitHub
git push origin main
```

**Railway auto-deploys** on every push to main branch!

### Manual Redeploy

1. Railway dashboard
2. Click **"Deployments"**
3. Click **"Redeploy"** on desired deployment

---

## Best Practices

### 1. Use Environment Variables

Don't hardcode:
- API keys
- Database credentials
- Configuration values

Use Railway environment variables instead.

### 2. Monitor Resource Usage

Check **Metrics** tab regularly:
- High memory usage → optimize image processing
- High CPU → check for infinite loops
- High bandwidth → large files being transferred

### 3. Set Up Alerts

Railway can notify you:
- Deployment failures
- High resource usage
- Downtime

Configure in **Settings** → **Notifications**

### 4. Regular Backups

Railway storage is ephemeral:
- Don't store important data on filesystem
- Use external storage (S3, Cloudinary) for persistence

---

## Advanced Configuration

### Health Checks

Railway automatically health checks:
- HTTP GET to your domain
- Expects 200 OK response

**Your app is healthy if:**
- `index.jsp` loads successfully
- Tomcat responds on port

### Scaling

**Vertical scaling:**
- Upgrade Railway plan for more CPU/RAM

**Horizontal scaling:**
- Not directly supported by Railway free tier
- Consider upgrading to Pro plan

---

## Comparison: Railway vs Alternatives

| Feature | Railway | Heroku | Render |
|---------|---------|--------|--------|
| Free tier | $5 credit | Limited | Yes |
| Maven support | ✅ Auto | ✅ Buildpack | ✅ Auto |
| Setup time | 5 min | 10 min | 10 min |
| Custom domain | ✅ Free | ❌ Paid only | ✅ Free |
| Deploy from Git | ✅ | ✅ | ✅ |

---

## Getting Help

### Railway Resources

- **Documentation:** https://docs.railway.app
- **Discord:** https://discord.gg/railway
- **GitHub:** https://github.com/railwayapp

### Project Resources

- **Project brain docs:** `d:\java_practicals\image-utility\brain\`
- **Deployment guide:** `brain/deployment.md`
- **Architecture:** `brain/architecture.md`

---

## Summary

**Deployment checklist:**

- [x] Create `pom.xml`
- [ ] Push to GitHub
- [ ] Connect Railway to GitHub repo
- [ ] Wait for automatic deployment
- [ ] Test live application
- [ ] (Optional) Add custom domain
- [ ] Monitor logs and metrics

**That's it!** Railway makes deployment simple - just push your code and it handles the rest.

---

**Next Steps:**

1. Push your code to GitHub
2. Connect to Railway
3. Share your live URL! 🚀
