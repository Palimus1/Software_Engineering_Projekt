# Mensch ärgere dich nicht

[![Scala CI](https://github.com/Palimus1/Software_Engineering_Projekt/actions/workflows/ci.yml/badge.svg?v=1)](https://github.com/Palimus1/Software_Engineering_Projekt/actions/workflows/ci.yml)
[![Coverage Status](https://coveralls.io/repos/github/Palimus1/Software_Engineering_Projekt/badge.svg?branch=master)](https://coveralls.io/github/Palimus1/Software_Engineering_Projekt?branch=master)


[Präsentationslink (hier klicken)](https://1drv.ms/p/c/061b6bc1767993c1/IQCNyqQxJbpcQZqhbqCkthOIAWQdySRy-KAKBn9j_VnEe1c?e=a9vki5)

Ein Scala 3 Projekt.

## Direkte Ausführung

compile:

    sbt compile

run:

    sbt run

tests:

    sbt clean coverage test coverageReport
  
dann:

    /target/scala-3.8.2/scoverage-report/ludo/index.html öffnen


-------
    WARNUNG: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @49993335'
sbt lädt JavaFX-Bibliotheken über klassischen Classpath statt über neueres Java-Modulsystem
-> Meldung ist fest in JavaFX, kann in Terminal nicht unterdrück werden
-> absolut keine negative Auswirkungen auf die Ausführung oder Funktionalität des Spiels


## Ausführen mit Docker

Standardmäßig startet der Container die TUI

Die GUI kann optional ebenfalls aus Docker gestartet werden. Dafür muss auf dem Host-System ein X-Server laufen, zum Beispiel Xming oder XLaunch unter Windows.

---

## Voraussetzungen

### Für alle Betriebssysteme

- Docker muss installiert und gestartet sein.
- Im Projektordner muss sich das `Dockerfile` befinden.
- Optional kann `docker compose` verwendet werden.

Prüfen, ob Docker läuft:

Unter Windows und macOS muss Docker Desktop geöffnet sein.

```bash
docker version
```



### Windows

- Docker Desktop installieren und starten.
- Für die GUI zusätzlich Xming oder XLaunch installieren.

### macOS

- Docker Desktop installieren und starten.
- Für die GUI zusätzlich XQuartz installieren.

### Linux

- Docker Engine installieren und starten.
- Für die GUI muss ein X11-Display verfügbar sein.

---

## Docker-Image bauen

Im Projektordner ausführen:

```bash
docker build -t ludo:v1 .
```
Das kann ein paar Minuten dauern.
Das Image heißt danach lokal `ludo:v1`.

---

## Spiel ohne GUI starten

Nur TUI starten (Standart)

```bash
docker run --rm -it ludo:v1
```

---

## Spiel ohne GUI mit Docker Compose starten

Alternativ kann das Spiel auch über Docker Compose gestartet werden:

```bash
docker compose run --rm ludo
```

Diese Variante nutzt die Einstellungen aus `docker-compose.yml`.

---

## Spiel mit GUI unter Windows starten

Für die GUI unter Windows wird ein X-Server benötigt. Das wurde mit Xming beziehungsweise XLaunch vorbereitet.

### 1. XLaunch starten

XLaunch öffnen und folgende Optionen auswählen:

```text
Multiple windows
Start no client
Disable access control
```

Damit XLaunch starten. Falls die Windows-Firewall fragt, den Zugriff erlauben.


### 2. Container mit GUI starten

In PowerShell im Projektordner ausführen:

```powershell
docker run --rm -it `
  -e LUDO_UI=both `
  -e DISPLAY=host.docker.internal:0.0 `
  ludo:v1
```

Dabei überschreibt `LUDO_UI=both` die Docker-Standardeinstellung und startet zusätzlich zur TUI auch die GUI.

---

## Spiel mit GUI unter macOS starten

Für die GUI unter macOS wird XQuartz benötigt.

### 1. XQuartz vorbereiten

XQuartz installieren und starten. In den Einstellungen von XQuartz muss erlaubt werden, dass Netzwerk-Clients Verbindungen herstellen dürfen.

Danach im Terminal ausführen:

```bash
xhost +
```

Hinweis: `xhost +` öffnet den lokalen X-Server für Verbindungen. Das ist für lokale Tests einfach, sollte aber nicht dauerhaft aktiv bleiben.

### 2. Container mit GUI starten

```bash
docker run --rm -it \
  -e LUDO_UI=both \
  -e DISPLAY=host.docker.internal:0 \
  ludo:v1
```

Nach dem Testen kann der Zugriff wieder eingeschränkt werden:

```bash
xhost -
```

---

## Spiel mit GUI unter Linux starten

Unter Linux kann meist das vorhandene X11-Display verwendet werden.

Vor dem Start einmal erlauben, dass Docker auf das lokale Display zugreifen darf:

```bash
xhost +local:docker
```

Dann den Container starten:

```bash
docker run --rm -it \
  -e LUDO_UI=both \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  ludo:v1
```

Nach dem Testen kann die Freigabe wieder entfernt werden:

```bash
xhost -local:docker
```

---


### Änderungen am Projekt werden nicht übernommen

Nach Änderungen am Code oder am Dockerfile muss das Image neu gebaut werden:

```bash
docker build -t ludo:v1 .
```
