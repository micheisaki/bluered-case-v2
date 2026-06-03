@echo off
setlocal
REM theBlueRed SUT denetleyici - Maven'siz derle ve calistir (Windows)
cd /d "%~dp0"

REM --- JDK'yi (javac) bul ---
set "JC="
set "JV="
where javac >nul 2>nul && ( set "JC=javac" & set "JV=java" )
if not defined JC if exist "%JAVA_HOME%\bin\javac.exe" ( set "JC=%JAVA_HOME%\bin\javac" & set "JV=%JAVA_HOME%\bin\java" )
if not defined JC for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do if exist "%%D\bin\javac.exe" ( set "JC=%%D\bin\javac" & set "JV=%%D\bin\java" )
if not defined JC for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\javac.exe" ( set "JC=%%D\bin\javac" & set "JV=%%D\bin\java" )
if not defined JC (
  echo javac bulunamadi. JDK kurulu mu kontrol edin.
  pause & exit /b 1
)
echo JDK: %JC%

if not exist lib\gson-2.10.1.jar (
  echo Gson indiriliyor...
  if not exist lib mkdir lib
  curl -L -o lib\gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
)

echo Derleniyor...
dir /s /b src\main\java\*.java > sources.txt
"%JC%" -encoding UTF-8 -cp lib\gson-2.10.1.jar -d out @sources.txt
if errorlevel 1 ( del sources.txt & echo DERLEME HATASI & pause & exit /b 1 )
del sources.txt

echo Calistiriliyor...
echo.
"%JV%" -cp "out;lib\gson-2.10.1.jar;src\main\resources" red.theblue.sutcheck.Main %*
echo.
pause
