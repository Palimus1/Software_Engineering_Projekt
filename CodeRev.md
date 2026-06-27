run:
sbt -> run
oder sbt run

    WARNUNG: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @49993335'
sbt lädt JavaFX-Bibliotheken über klassischen Classpath statt über neueres Java-Modulsystem
-> Meldung ist fest in JavaFX, kann in Terminal nicht unterdrück werden
-> absolut keine negative Auswirkungen auf die Ausführung oder Funktionalität des Spiels


3 Spieler Calc

1. Absturz bei 3:
Bei 3 Spielern verschieben sich die Startpositionen mathematisch so ungünstig, dass eine Basis über den Rand des Fensters hinaus (in den Minus-Bereich) gezeichnet werden sollte. Das brachte die Grafik zum Absturz. Ich habe den Code so angepasst, dass das Raster nun immer automatisch so weit verschoben wird, dass alles ins Bild passt.

2. Absturz bei Neustart:
Das lag nicht am Spiel, sondern an sbt. Zuerst ist es abgestürzt, weil sbt versucht hat, Grafiktreiber doppelt zu laden (Fehler: UnsatisfiedLinkError). Danach hat sbt blockiert, dass du die Spielerzahl über die Tastatur eintippen konntest (Fehler: NullPointerException). Beides habe ich in der Konfiguration (build.sbt) repariert.