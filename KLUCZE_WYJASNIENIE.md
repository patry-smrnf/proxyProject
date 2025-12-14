# Wyjaśnienie mechanizmu kluczy serwerów

## 🎯 Co to są klucze?

**Klucze** to identyfikatory danych przechowywanych na różnych serwerach. Każdy serwer może przechowywać dane pod określonymi kluczami (np. "temperature", "humidity", "counter").

## 📋 Przykład scenariusza

Wyobraź sobie, że masz:
- **Serwer 1** (TCP na porcie 1234) - przechowuje klucze: "temperature", "pressure"
- **Serwer 2** (UDP na porcie 5678) - przechowuje klucze: "humidity", "wind"

Proxy musi wiedzieć, który serwer odpowiada za który klucz, żeby przekierować zapytania klienta do właściwego serwera.

---

## 🔄 Jak to działa - krok po kroku:

### 1️⃣ **START PROXY - Odkrywanie kluczy** (ServerDiscovery)

Gdy proxy się uruchamia, wykonuje się `discoverKeys()`:

```java
// Proxy wysyła do każdego serwera komendę: "GET NAMES"
// Każdy serwer odpowiada np.: "OK 2 temperature pressure"
// Proxy zapisuje pierwszy klucz w server.keyName
```

**Kod:**
```java
String response = communicator.sendCommand(server, "GET NAMES");
// Odpowiedź: "OK 2 temperature pressure"
String[] parts = response.split("\\s+");
server.keyName = parts[2]; // Zapisuje "temperature" dla tego serwera
```

**Problem w obecnej implementacji:** 
- Zapisuje się tylko **pierwszy klucz** z odpowiedzi (parts[2])
- Jeśli serwer ma wiele kluczy, pozostałe nie są zapisywane

---

### 2️⃣ **KLIENT WYSYŁA: "GET NAMES"**

Klient pyta proxy: "Jakie klucze są dostępne?"

```java
handleGetNames() {
    // Proxy wysyła "GET NAMES" do WSZYSTKICH serwerów
    // Zbiera wszystkie klucze i zwraca je klientowi
    // Odpowiedź: "OK 4 temperature pressure humidity wind"
}
```

**Różnica:** 
- Tu pobierane są **wszystkie** klucze ze wszystkich serwerów
- To jest bardziej kompletne niż `discoverKeys()`

---

### 3️⃣ **KLIENT WYSYŁA: "GET VALUE temperature"**

Klient chce dostać wartość klucza "temperature".

```java
handleGetValue("temperature") {
    // ServerFinder szuka serwera, który ma keyName == "temperature"
    Server server = serverFinder.findServerForKey(servers, "temperature");
    
    // Jeśli znalazł serwer, przekazuje zapytanie do niego
    communicator.sendCommand(server, "GET VALUE temperature");
}
```

**Jak działa wyszukiwanie:**
```java
for (Server server : servers) {
    if (keyName.equals(server.keyName)) {  // Porównuje "temperature" z server.keyName
        return server;  // Zwraca odpowiedni serwer
    }
}
```

---

### 4️⃣ **KLIENT WYSYŁA: "SET temperature 25"**

Podobnie - proxy znajduje odpowiedni serwer i przekazuje komendę SET.

---

## ⚠️ **OBECNY PROBLEM W KODZIE:**

### Problem z `ServerDiscovery.discoverKeys()`:

**Linia 29 w ServerDiscovery.java:**
```java
server.keyName = parts[2]; // Zapisuje tylko pierwszy klucz!
```

**Przykład:**
- Serwer zwraca: `"OK 3 temperature pressure humidity"`
- Kod zapisuje tylko: `"temperature"`
- Klucze `"pressure"` i `"humidity"` nie są zapisane w `server.keyName`

**Konsekwencja:**
- Jeśli klient zapyta o `"GET VALUE pressure"`, proxy nie znajdzie serwera (bo szuka serwera z `keyName == "pressure"`, ale zapisane jest tylko `"temperature"`)

---

## ✅ **ROZWIĄZANIE:**

Trzeba by było zmienić logikę, żeby:
1. Albo zapisywać **wszystkie klucze** w każdym serwerze (lista kluczy zamiast jednego keyName)
2. Albo używać `handleGetNames()` także do zapisu (bo ono pobiera wszystkie klucze)
3. Albo tworzyć mapę: `Map<String, Server>` gdzie kluczem mapy jest nazwa klucza, a wartością serwer

---

## 📊 **PRZEPŁYW DANYCH:**

```
KLIENT                    PROXY                      SERWERY
  |                         |                           |
  |-- "GET NAMES" --------->|                           |
  |                         |-- "GET NAMES" ----------->| Serwer 1
  |                         |<-- "OK 2 temp press" -----|
  |                         |-- "GET NAMES" ----------->| Serwer 2
  |                         |<-- "OK 2 humid wind" -----|
  |<-- "OK 4 temp press humid wind"                     |
  |                         |                           |
  |-- "GET VALUE temp" ---->|                           |
  |                         |-- Znajduje serwer z temp--|
  |                         |-- "GET VALUE temp" ------>| Serwer 1
  |                         |<-- "OK temp 23" ----------|
  |<-- "OK temp 23" --------|                           |
```

---

## 🎓 **PODSUMOWANIE:**

1. **Klucze** = identyfikatory danych na serwerach
2. **Odkrywanie** = proxy pyta serwery o dostępne klucze przy starcie
3. **Routowanie** = gdy klient pyta o konkretny klucz, proxy znajduje odpowiedni serwer
4. **Problem** = obecna implementacja zapisuje tylko pierwszy klucz, co może powodować błędy routingu

Chcesz, żebym naprawił ten problem w kodzie?
