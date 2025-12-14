@echo off
REM Tworzenie folderu na skompilowane pliki
if not exist "bin" mkdir bin

REM Kompilacja projektu
echo Kompilowanie projektu...
javac -d bin -sourcepath src src\Proxy.java

if %ERRORLEVEL% EQU 0 (
    echo Kompilacja zakończona pomyślnie!
    echo Pliki .class znajdują się w folderze bin\
) else (
    echo Błąd podczas kompilacji!
    exit /b 1
)
