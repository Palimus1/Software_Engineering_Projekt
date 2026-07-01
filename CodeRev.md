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

### ludo.model.GamePhase,  ludo.model.Setup

+ ludo.model.SetupStep: Ein Enum, das die aktuellen Setup-Schritte definiert (NumPlayers, PlayerNames, FieldSize, GameMode).

+ ludo.model.SetupPhase: Eine neue GamePhase, die den Zustand des Setups speichert.

GamePhase: Wir fügen eine Methode handleSetup(state: GameState, input: String): Try[GameState] hinzu, die nur in der SetupPhase Eingaben verarbeitet.
### ludo.model.GameState
[MODIFY] GameState: Wird angepasst, sodass man ein "leeres" Spiel erstellen kann, das in der SetupPhase startet (z.B. mit Dummy-Spielern und einem Dummy-Board), bis das Setup abgeschlossen ist. Sobald das Setup fertig ist, generiert die SetupPhase den finalen GameState und wechselt in die RollingPhase.
ludo.controller.ControllerInterface & Controller
[MODIFY] ControllerInterface / Controller: Bekommt eine neue Methode def doSetup(input: String): Unit. Diese reicht den Input an die aktuelle Phase weiter und aktualisiert den State.
### Main.scala
[MODIFY] Main.scala: Die blockierenden StdIn.readLine() Aufrufe zur Konfiguration werden komplett entfernt.
gameLoop() wird so angepasst, dass es alle Tastatureingaben entweder an doSetup() (wenn das Spiel noch im Setup ist) oder an die regulären Spiel-Funktionen (doMove, rollDice) sendet.
### ludo.aview.Tui
[MODIFY] Tui.scala: Die update()-Methode prüft nun, ob sich das Spiel in der SetupPhase befindet. Falls ja, druckt sie den entsprechenden Text in die Konsole (z.B. "Bitte Anzahl Spieler eingeben").
### ludo.aview.GuiLogic
[MODIFY] GuiLogic.scala: Wir erweitern createRootPane.
Wenn state.phase eine SetupPhase ist, wird nicht das Spielfeld (boardGrid) gezeichnet, sondern ein schönes, visuell ansprechendes Setup-Menü (z.B. Buttons für Spieleranzahl, Textfelder für Namen).

Eingaben in dieses Setup-Menü rufen controller.doSetup(wert) auf.