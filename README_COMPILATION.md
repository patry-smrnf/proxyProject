# Kompilacja projektu Proxy

## Szybka kompilacja (Windows)
```bash
compile.bat
```

## Szybka kompilacja (Linux/Mac)
```bash
chmod +x compile.sh
./compile.sh
```

## Ręczna kompilacja

### Opcja 1: Użycie sourcepath (zalecane)
```bash
# Utworzenie folderu bin
mkdir bin

# Kompilacja (javac automatycznie znajdzie wszystkie zależności)
javac -d bin -sourcepath src src/Proxy.java
```

### Opcja 2: Kompilacja wszystkich plików
```bash
# Windows
javac -d bin -sourcepath src src\Proxy.java src\proxy\**\*.java

# Linux/Mac
javac -d bin -sourcepath src src/Proxy.java src/proxy/**/*.java
```

### Opcja 3: Kompilacja każdego pliku osobno (jeśli powyższe nie działają)
```bash
# Windows PowerShell
javac -d bin -sourcepath src src/Proxy.java `
    src/proxy/model/Server.java `
    src/proxy/network/ServerCommunicator.java `
    src/proxy/discovery/ServerDiscovery.java `
    src/proxy/discovery/ServerFinder.java `
    src/proxy/commands/CommandProcessor.java `
    src/proxy/handlers/ClientHandler.java `
    src/proxy/core/ProxyObject.java `
    src/proxy/utils/ArgumentParser.java
```

## Uruchomienie
Po kompilacji, uruchom projekt:
```bash
java -cp bin Proxy -port 8080 -servers tcp://localhost:1234 udp://localhost:5678
```

## Struktura po kompilacji
```
proxyProject/
├── src/              (pliki źródłowe)
├── bin/              (skompilowane pliki .class)
├── compile.bat       (skrypt kompilacji dla Windows)
└── compile.sh        (skrypt kompilacji dla Linux/Mac)
```

## Uwagi
- Upewnij się, że masz zainstalowane JDK (Java Development Kit)
- Sprawdź wersję: `javac -version`
- Jeśli potrzebujesz konkretnej wersji Java, użyj: `javac -source 11 -target 11 ...`
