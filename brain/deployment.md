# Deployment Guide

## Overview

This guide covers building, deploying, and running the image-utility application.

---

## Prerequisites

### Required Software

| Software | Minimum Version | Download |
|----------|----------------|----------|
| **Java JDK** | 11+ | https://adoptium.net/ |
| **Apache Tomcat** | 9.0.113 | https://tomcat.apache.org/ |
| **Git** (optional) | Latest | https://git-scm.com/ |

### Verify Installations

```bash
# Check Java version
java -version
# Should show: openjdk version "11" or higher

# Check javac (compiler)
javac -version
# Should show: javac 11 or higher

# Check Tomcat installation
dir "C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113"
# Should list: bin/, lib/, webapps/, etc.
```

---

## Project Setup

### 1. Clone/Download Project

```bash
# Option 1: Git clone
git clone <repository-url>
cd image-utility

# Option 2: Extract ZIP
# Extract to: D:\java_practicals\image-utility
```

### 2. Configure Tomcat Path

**Edit:** `deploy_and_run.bat` (line 2)

```bat
set "TOMCAT_HOME=C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113"
```

**Update to your Tomcat installation path.**

### 3. Verify Directory Structure

```
image-utility/
├── deploy_and_run.bat       ✓ Deployment script
├── src/
│   └── main/
│       ├── java/            ✓ Source code
│       └── webapp/          ✓ Web resources
├── build/                   ? Auto-created
└── brain/                   ✓ Documentation
```

---

## Build Process

### Automated Build (Recommended)

**Run:** `deploy_and_run.bat`

This script performs all build steps automatically:

```bat
.\deploy_and_run.bat
```

### Manual Build Steps

**If you need to build manually:**

#### Step 1: Create Build Directory

```bash
mkdir build\classes
```

#### Step 2: Compile Java Sources

```bash
javac -verbose ^
      -source 11 -target 11 ^
      -d build\classes ^
      -cp "C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\lib\servlet-api.jar;src\main\webapp\WEB-INF\lib\*" ^
      src\main\java\util\*.java ^
      src\main\java\controller\*.java
```

**Parameters Explained:**

| Parameter | Purpose |
|-----------|---------|
| `-verbose` | Show compilation details |
| `-source 11` | Java source compatibility |
| `-target 11` | Java bytecode version |
| `-d build\classes` | Output directory |
| `-cp ...` | Classpath (Servlet API + libraries) |

**Expected Output:**

```
[parsing started src\main\java\util\ImageUtils.java]
[parsing completed 25ms]
[parsing started src\main\java\controller\ImageServlet.java]
[parsing completed 10ms]
[parsing started src\main\java\controller\DownloadServlet.java]
[parsing completed 8ms]
...
[wrote build\classes\util\ImageUtils.class]
[wrote build\classes\controller\ImageServlet.class]
[wrote build\classes\controller\DownloadServlet.class]
```

#### Step 3: Create Deployment Directory

```bash
set APP_DIR=C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\webapps\project-image-utility-tool

mkdir "%APP_DIR%"
```

#### Step 4: Copy Web Resources

```bash
xcopy /E /Y /Q "src\main\webapp\*" "%APP_DIR%\"
```

#### Step 5: Copy Compiled Classes

```bash
mkdir "%APP_DIR%\WEB-INF\classes"
xcopy /E /Y /Q "build\classes\*" "%APP_DIR%\WEB-INF\classes\"
```

---

## Deployment

### Deploy to Tomcat

**Automated (via script):**

```bash
.\deploy_and_run.bat
```

**Manual Steps:**

1. **Stop Tomcat** (if running)
   ```bash
   C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\bin\shutdown.bat
   ```

2. **Clean Previous Deployment**
   ```bash
   rmdir /S /Q "C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\webapps\project-image-utility-tool"
   ```

3. **Copy Application**
   - Follow manual build steps above

4. **Start Tomcat**
   ```bash
   C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\bin\catalina.bat run
   ```

### Deployment Structure

**Final deployment in Tomcat webapps:**

```
webapps/
└── project-image-utility-tool/
    ├── WEB-INF/
    │   ├── classes/
    │   │   ├── controller/
    │   │   │   ├── ImageServlet.class
    │   │   │   └── DownloadServlet.class
    │   │   └── util/
    │   │       └── ImageUtils.class
    │   ├── lib/
    │   │   ├── pdfbox-2.0.30.jar
    │   │   ├── commons-logging-1.2.jar
    │   │   └── webp-imageio-0.1.6.jar
    │   └── uploads/              (created at runtime)
    ├── index.jsp
    └── result.jsp
```

---

## Running the Application

### Start Server

**Using deployment script:**

```bash
.\deploy_and_run.bat
```

**Manually:**

```bash
cd C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\bin
catalina.bat run
```

**Output:**

```
[main] org.apache.catalina.startup.Catalina.start Server startup in [2345] milliseconds
```

### Access Application

**Local URL:**
```
http://localhost:8080/project-image-utility-tool/
```

**Alternative (explicit index.jsp):**
```
http://localhost:8080/project-image-utility-tool/index.jsp
```

### Verify Deployment

**Check Tomcat logs:**

```bash
type C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\logs\catalina.YYYY-MM-DD.log
```

**Look for:**
```
INFO: Deployment of web application directory [...] has finished in [...] ms
```

---

## Stop Server

### Stop Tomcat

**Graceful shutdown:**

```bash
C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\bin\shutdown.bat
```

**Force stop:**

```bash
# Press Ctrl+C in terminal running catalina.bat run
```

**Kill process (if stuck):**

```powershell
# Find process
Get-Process -Name java

# Kill it
Stop-Process -Name java -Force
```

---

## Configuration

### Application Configuration

**Edit:** `deploy_and_run.bat`

```bat
# Line 2: Tomcat installation path
set "TOMCAT_HOME=C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113"

# Line 4: Application name (URL context path)
set "APP_NAME=project-image-utility-tool"
```

### Tomcat Configuration

**Server port:**

Edit `TOMCAT_HOME/conf/server.xml`:

```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

**Memory settings:**

Edit `TOMCAT_HOME/bin/catalina.bat`:

```bat
set JAVA_OPTS=-Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m
```

---

## Troubleshooting

### Common Issues

#### 1. Compilation Errors

**Error:** `javac: command not found`

**Solution:**
```bash
# Add Java to PATH
set PATH=%PATH%;C:\Program Files\Java\jdk-11\bin

# Verify
javac -version
```

---

#### 2. Tomcat Not Starting

**Error:** `Address already in use: bind`

**Solution:**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <process_id> /F
```

---

#### 3. Application Not Deployed

**Error:** 404 Not Found

**Check:**

1. **Verify deployment directory exists:**
   ```bash
   dir "C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113\webapps\project-image-utility-tool"
   ```

2. **Check Tomcat logs:**
   ```bash
   type TOMCAT_HOME\logs\catalina.YYYY-MM-DD.log
   ```

3. **Verify compiled classes exist:**
   ```bash
   dir "...\webapps\project-image-utility-tool\WEB-INF\classes\controller"
   ```

---

#### 4. Servlet Not Found

**Error:** HTTP Status 404 - /ImageServlet

**Solution:**

1. **Check servlet class compiled:**
   ```bash
   dir build\classes\controller\ImageServlet.class
   ```

2. **Verify @WebServlet annotation:**
   ```java
   @WebServlet("/ImageServlet")
   ```

3. **Restart Tomcat**

---

#### 5. OutOfMemoryError

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution:**

Increase Tomcat heap size:

Edit `TOMCAT_HOME/bin/catalina.bat`:

```bat
rem Add before "set MAINCLASS=..." line
set JAVA_OPTS=-Xms512m -Xmx2048m
```

---

#### 6. WebP Not Working

**Error:** "No ImageIO readers for WebP"

**Solution:**

1. **Verify WebP library exists:**
   ```bash
   dir src\main\webapp\WEB-INF\lib\webp-imageio-*.jar
   ```

2. **Check deployment:**
   ```bash
   dir C:\...\webapps\project-image-utility-tool\WEB-INF\lib\webp-imageio-*.jar
   ```

3. **Redeploy if missing:**
   ```bash
   .\deploy_and_run.bat
   ```

See `WEBP_LIBRARY_SETUP.md` for details.

---

## Production Deployment

### Security Checklist

**Before deploying to production:**

- [ ] Disable Tomcat manager app
- [ ] Set strong admin passwords
- [ ] Enable HTTPS (SSL/TLS)
- [ ] Configure firewall rules
- [ ] Implement rate limiting
- [ ] Add file size limits enforcement
- [ ] Enable access logging
- [ ] Implement file cleanup scheduled task
- [ ] Add virus scanning (optional)
- [ ] Set up monitoring

### Production Configuration

**web.xml additions:**

```xml
<!-- Add session timeout -->
<session-config>
    <session-timeout>30</session-timeout>
</session-config>

<!-- Add error pages -->
<error-page>
    <error-code>404</error-code>
    <location>/error404.jsp</location>
</error-page>

<error-page>
    <error-code>500</error-code>
    <location>/error500.jsp</location>
</error-page>
```

**Tomcat production settings:**

```xml
<!-- server.xml -->
<!-- Disable shutdown port -->
<Server port="-1">

<!-- Enable access logs -->
<Valve className="org.apache.catalina.valves.AccessLogValve"
       directory="logs"
       prefix="access_log"
       suffix=".txt"
       pattern="%h %l %u %t &quot;%r&quot; %s %b" />
```

---

## Monitoring

### Log Locations

| Log File | Location | Purpose |
|----------|----------|---------|
| Catalina | `TOMCAT_HOME/logs/catalina.YYYY-MM-DD.log` | Server startup/shutdown |
| Localhost | `TOMCAT_HOME/logs/localhost.YYYY-MM-DD.log` | Application errors |
| Access Log | `TOMCAT_HOME/logs/access_log.YYYY-MM-DD.txt` | HTTP requests |

### Monitor Disk Usage

**uploads/ directory:**

```bash
# Check size
dir /s "TOMCAT_HOME\webapps\project-image-utility-tool\uploads"

# Monitor growth
# Set up scheduled task to clean old files
```

**Recommended:** Delete files older than 1 hour

```bash
# PowerShell script
Get-ChildItem -Path "uploads\" -Recurse | 
    Where-Object {$_.LastWriteTime -lt (Get-Date).AddHours(-1)} | 
    Remove-Item
```

---

## Backup and Recovery

### Backup

**What to backup:**

1. **Source code:** `D:\java_practicals\image-utility\`
2. **Configuration:** `deploy_and_run.bat`
3. **Dependencies:** `src\main\webapp\WEB-INF\lib\`

**Backup script:**

```bash
# Create backup
set BACKUP_DIR=D:\backups\image-utility_%date:~-4,4%%date:~-10,2%%date:~-7,2%
xcopy /E /I "D:\java_practicals\image-utility" "%BACKUP_DIR%"
```

### Recovery

```bash
# Restore from backup
xcopy /E /Y "D:\backups\image-utility_YYYYMMDD\*" "D:\java_practicals\image-utility\"

# Redeploy
cd D:\java_practicals\image-utility
.\deploy_and_run.bat
```

---

## Continuous Deployment

### Git Workflow

```bash
# Pull latest changes
git pull origin main

# Rebuild and deploy
.\deploy_and_run.bat
```

### Automated Deployment Script

```bat
@echo off
echo Pulling latest changes...
git pull origin main

echo Building and deploying...
call deploy_and_run.bat

echo Deployment complete!
```

---

## Scaling

### Horizontal Scaling

**Requirements:**
- Shared storage for uploads/
- Load balancer
- Session replication (if sessions added)

**Setup:**

1. **Multiple Tomcat instances** on different ports
2. **Shared uploads directory** (NFS, S3, etc.)
3. **Load balancer** (nginx, Apache, etc.)

**nginx config example:**

```nginx
upstream image_utility {
    server localhost:8080;
    server localhost:8081;
    server localhost:8082;
}

server {
    listen 80;
    
    location / {
        proxy_pass http://image_utility;
    }
}
```

---

**Next:** [Development Guide](./development-guide.md)
