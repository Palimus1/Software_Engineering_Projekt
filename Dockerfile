FROM sbtscala/scala-sbt:eclipse-temurin-21_1.x

WORKDIR /app

# Docker soll standardmaessig die TUI starten. Lokal bleibt ohne diese Variable
# weiterhin das bisherige Verhalten erhalten.
ENV LUDO_UI=tui

RUN apt-get update && \
    apt-get install -y \
      libxrender1 \
      libxtst6 \
      libxi6 \
      libgl1 \
      libgtk-3-0 \
      && rm -rf /var/lib/apt/lists/*

COPY . .


# Weniger ANSI-/Supershell-Ausgabe im Container-Log.
ENV SBT_OPTS="-Dsbt.supershell=false -Dsbt.log.noformat=true"

# Erst nur Build-Dateien kopieren, damit Docker Dependency-Layer cachen kann.
COPY build.sbt ./
COPY project ./project

RUN sbt -Dsbt.supershell=false update

# Danach den eigentlichen Quellcode kopieren.
COPY src ./src
COPY README.md ./README.md
COPY stryker4s.conf ./stryker4s.conf

CMD ["sbt", "-Dsbt.supershell=false", "run"]

