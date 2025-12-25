@echo off
set "TOMCAT_HOME=C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "APP_NAME=project-image-utility-tool"
set "APP_DIR=%TOMCAT_HOME%\webapps\%APP_NAME%"

echo [1/4] Cleaning previous deployment...
if exist "%APP_DIR%" rmdir /S /Q "%APP_DIR%"

echo [2/4] Compiling Java sources...
if not exist build\classes mkdir build\classes
javac -verbose -source 11 -target 11 -d build\classes -cp "%TOMCAT_HOME%\lib\servlet-api.jar;src\main\webapp\WEB-INF\lib\*" src\main\java\util\*.java src\main\java\controller\*.java
if %errorlevel% neq 0 (
    echo Compilation FAILED!
    pause
    exit /b %errorlevel%
)

echo [3/4] Deploying to %APP_DIR%...
mkdir "%APP_DIR%"
xcopy /E /Y /Q "src\main\webapp\*" "%APP_DIR%\"
if not exist "%APP_DIR%\WEB-INF\classes" mkdir "%APP_DIR%\WEB-INF\classes"
xcopy /E /Y /Q "build\classes\*" "%APP_DIR%\WEB-INF\classes\"

echo [4/4] Starting Tomcat in current terminal...
echo Press Ctrl+C to stop the server.
call "%TOMCAT_HOME%\bin\catalina.bat" run
