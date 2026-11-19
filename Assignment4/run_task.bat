@echo off
setlocal
cd /d "%~dp0"
echo 1 Fee payment
echo 2 Appointment desk
echo 3 Library search
echo 4 Drawing pad
echo 5 Updated lambda converter
echo 6 Campus events DEMO (no database)
echo 7 Completed converter TODO program
set /p task=Choose 1-7: 
if "%task%"=="1" java -cp Task01 FeeWindow
if "%task%"=="2" java -cp Task02 AppointmentDesk
if "%task%"=="3" java -cp Task03 LibrarySearch
if "%task%"=="4" java -cp Task04 DrawPad
if "%task%"=="5" java -cp Task05\Updated ConverterLambda
if "%task%"=="6" java -cp Task06 CampusEvents --demo
if "%task%"=="7" java -cp Task05\CompleteCode Converter
pause
