@echo off
echo ==========================================
echo   College Event Management System
echo ==========================================
echo.
if not exist out mkdir out

echo [1/2] Compiling...
javac -d out -sourcepath src src\Main.java src\gui\AppTheme.java src\gui\SplashScreen.java src\gui\LoginFrame.java src\gui\AdminDashboard.java src\gui\StudentDashboard.java src\data\DataStore.java src\models\Admin.java src\models\Student.java src\models\Club.java src\models\Event.java src\models\Feedback.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed. Make sure Java JDK is installed.
    pause
    exit /b 1
)
echo [2/2] Launching...
java -cp out Main
pause
