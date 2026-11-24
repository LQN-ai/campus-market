@echo off
cd /d "%~dp0backend"
if not exist target\market-1.0.0.jar (
    D:\Software\apache-maven-3.9.9\bin\mvn package -DskipTests
)
java -jar target\market-1.0.0.jar
pause
