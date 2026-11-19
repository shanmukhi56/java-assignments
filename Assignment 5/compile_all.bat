@echo off
setlocal
cd /d "%~dp0"
for %%D in (Task01 Task02 Task03 Task04) do (
 pushd %%D
 javac *.java
 if errorlevel 1 (
  popd
  echo Compilation failed in %%D. Ensure JDK 21 is installed and in PATH.
  pause
  exit /b 1
 )
 popd
)
echo =======================================================
echo All Assignment 5 tasks compiled successfully on JDK 21.
echo =======================================================
pause
