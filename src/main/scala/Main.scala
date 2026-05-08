import ludo.model.*
import ludo.controller.Controller
import ludo.aview.Tui
import scala.annotation.tailrec
import scala.io.StdIn

@main def main(): Unit = {
  println("Willkommen zu Mensch ärger dich nicht\n")

  // 1. Setup / Konfiguration abfragen
  println("Bitte die Anzahl Spieler angeben (1-4): ")
  val numPlayers = StdIn.readLine().toIntOption.getOrElse(4)

  println(s"Bitte gib die Namen für $numPlayers Spieler ein:")
  val playerNames = collectNames(numPlayers, Nil)

  println("Bitte die Feldgröße (Standard 40): ")
  val fieldSize = StdIn.readLine().toIntOption.getOrElse(40)

  // 2. MVC Komponenten initialisieren
  val config = BoardConfig(fieldSize, numPlayers)
  val initialState = GameState.create(playerNames, config)

  // Der Controller verwaltet den State
  val controller = Controller(initialState, config)

  // Die TUI meldet sich beim Erstellen automatisch als Observer an
  val tui = Tui(controller)

  // Einmaliges manuelles Anzeigen zum Start
  println("\nSpiel startet!")
  tui.update()

  // 3. Start der Spielschleife
  gameLoop(controller)
}

@tailrec
def collectNames(remaining: Int, acc: List[String]): List[String] = {
  if (remaining <= 0) acc.reverse
  else {
    print(s"Name für Spieler ${acc.size + 1}: ")
    collectNames(remaining - 1, StdIn.readLine() :: acc)
  }
}

@tailrec
def gameLoop(controller: Controller): Unit = {
  // Aktuellen Spieler aus dem Controller-State lesen
  val currPlayer = controller.gameState.currentPlayer

  println(s"\nAktueller Spieler: ${currPlayer.name} (${currPlayer.color})")

  print("Wähle eine Figur (1-4): ")
  val pieceId = StdIn.readLine().toIntOption.getOrElse(1)

  print("Wie weit soll sie sich bewegen: ")
  val moveBy = StdIn.readLine().toIntOption.getOrElse(0)

  // Der Controller führt den Zug aus und BENACHRICHTIGT die TUI automatisch[cite: 7, 10]
  controller.doMove(pieceId, moveBy)

  // Rekursion für die nächste Runde
  gameLoop(controller)
}