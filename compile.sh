#!/bin/bash

# Tworzenie folderu na skompilowane pliki
mkdir -p bin

# Kompilacja projektu
echo "Kompilowanie projektu..."
javac -d bin -sourcepath src src/Proxy.java

if [ $? -eq 0 ]; then
    echo "Kompilacja zakończona pomyślnie!"
    echo "Pliki .class znajdują się w folderze bin/"
else
    echo "Błąd podczas kompilacji!"
    exit 1
fi
