@echo off
setlocal
cd /d "%~dp0"
for %%D in (Task01 Task02 Task03 Task04 Task05\CompleteCode Task05\Baseline Task05\Updated Task06) do (
 pushd %%D
 javac *.java
 if errorlevel 1 (
  popd
  echo Compilation failed. Ensure JDK 21 is installed.
  pause
  exit /b 1
 )
 popd
)
echo All programs compiled successfully.
pause
