import ludo.model.*
import ludo.controller.Controller
import ludo.aview.Tui
import scala.annotation.tailrec
import scala.io.StdIn

@main def main(): Unit = {
  println("Willkommen zu Mensch aerger dich nicht\n")

  // 1. Setup / Konfiguration abfragen
  println("Bitte die Anzahl Spieler angeben (1-4): ")
  val numPlayers = StdIn.readLine().toIntOption.getOrElse(4)

  println(s"Bitte gib die Namen fuer $numPlayers Spieler ein:")
  val playerNames = collectNames(numPlayers, Nil)

  println("Bitte die Feldgroeße (Standard 40): ")
  val fieldSize = StdIn.readLine().toIntOption.getOrElse(40)

  println("Waehle einen Spielmodus aus: Standart-Modus(ENTER)  ---  Blitz-Modus(Blitz)")
  val mode = StdIn.readLine().trim.toLowerCase

  val selectedStrategy = mode match {
    case "blitz" =>
      println("Blitz-Modus aktiviert! Wer zuerst eine Figur im Ziel hat, gewinnt.")
      QuickWinStrategy

    case "" | "standard" | "standart" => //
      println("🐢 Standard-Modus aktiviert! Alle 4 Figuren muessen ins Ziel.")
      StandardWinStrategy

    case _ =>
      println("Ungueltige Eingabe. Wir starten zur Sicherheit den Standard-Modus.")
      StandardWinStrategy
  }



  val config = BoardConfig(fieldSize, numPlayers, selectedStrategy)
  val initialState = GameState.create(playerNames, config)


  val controller = Controller(initialState)


  val tui = Tui(controller)


  println("\nSpiel startet!")
  tui.update()


  gameLoop(controller)
}

@tailrec
def collectNames(remaining: Int, acc: List[String]): List[String] = {
  if (remaining <= 0) acc.reverse
  else {
    print(s"Name fuer Spieler ${acc.size + 1}: ")
    collectNames(remaining - 1, StdIn.readLine() :: acc)
  }
}

@tailrec
def gameLoop(controller: Controller): Unit = {

  val state = controller.gameState

  if (state.phase == GameOverPhase) {
    return
  }

  val currPlayer = state.currentPlayer

  println(s"\nAktueller Spieler: ${currPlayer.name} (${currPlayer.color})")

  state.diceRoll.foreach(roll => println(s"🎲 Du hast eine $roll gewuerfelt!"))

  print("Aktion waehlen -> 'w' (Wuerfeln), '1'-'4' (Figur bewegen), 'u' (Undo), 'r' (Redo), 'q' (Beenden): ")
  val input = StdIn.readLine().trim.toLowerCase

  input match {
    case "q" =>
      println("Spiel wird beendet.")
      return
    case "w" =>
      controller.rollDice()
    case "1" | "2" | "3" | "4" =>
      controller.doMove(input.toInt)
    case "u" =>
      controller.undo()
    case "r" =>
      controller.redo()
    case _ =>
      println("Ungueltige Eingabe! Bitte 'w', '1'-'4', 'u', 'r' oder 'q' eingeben.")
  }

  gameLoop(controller)
}