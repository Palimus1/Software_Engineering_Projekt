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
  val controller = Controller(initialState)

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
  //checken ob jemand gewonnen hat
  if (controller.gameState.winner != "") {
    return
  }

  val state = controller.gameState
  val currPlayer = state.currentPlayer

  println(s"\nAktueller Spieler: ${currPlayer.name} (${currPlayer.color})")

  state.diceRoll match {
    case None =>
      // PHASE 1: Es wurde noch nicht gewürfelt
      println("Drücke ENTER zum Würfeln...")
      StdIn.readLine() // Wartet einfach auf die Enter-Taste
      controller.rollDice()
      gameLoop(controller) // Wieder von vorne starten, um das Ergebnis zu sehen

    case Some(roll) =>
      // PHASE 2: Es wurde gewürfelt, jetzt Figur auswählen
      println(s"🎲 Du hast eine $roll gewürfelt!")
      print("Wähle eine Figur (1-4): ")
      val pieceId = StdIn.readLine().toIntOption.getOrElse(0)

      controller.doMove(pieceId) // Controller macht den Zug und löscht danach den diceRoll
      gameLoop(controller)
  }
}