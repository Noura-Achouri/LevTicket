@echo off
REM Start the LevTicket Spring Boot backend so the website's API works.
REM Set JAVA_HOME to your JDK, then run maven spring-boot:run.
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
call "%~dp0mvnw.cmd" spring-boot:run
