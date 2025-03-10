@echo off
java --version >nul 2>&1

IF %ERRORLEVEL% NEQ 0 (
    echo Java is not installed in the system
    exit /b 1
) ELSE (
    echo Detected Java installation
    java --version
)

java -jar ../lib/spring-archaius-docgen.jar %*