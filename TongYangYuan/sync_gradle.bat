@echo off
cd /d "%~dp0"
gradlew.bat --stop
gradlew.bat build --refresh-dependencies
