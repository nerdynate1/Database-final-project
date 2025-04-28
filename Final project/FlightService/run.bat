@echo off
echo Flight Service Application
echo ========================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven and try again.
    exit /b 1
)

echo Building application...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Build failed.
    exit /b 1
)

echo.
echo Running application...
echo.
java -jar target/flightservice-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
pause 