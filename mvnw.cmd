@REM Maven Wrapper script for Windows
@REM Uses Maven from PATH if available; otherwise uses wrapper jar

setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if exist "%WRAPPER_JAR%" goto runWrapper

echo Downloading Maven Wrapper...
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%WRAPPER_JAR%' -UseBasicParsing"
if errorlevel 1 (
    echo Maven was not found in PATH and the wrapper could not be downloaded.
    echo Install Maven from https://maven.apache.org/download.cgi and add it to PATH.
    exit /b 1
)

:runWrapper
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    call mvn %*
) else (
    java -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %MAVEN_OPTS% -cp "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
)
endlocal
