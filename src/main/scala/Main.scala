import ludo._
import scala.annotation.tailrec
import scala.io.StdIn

@main def main(): Unit = {

  println("Willkommen zu Mensch ärger dich nicht\n")

  println("Bitte die Anzahl Spieler angeben (1-4): ")
  val numPInput = StdIn.readLine()
  val numPlayers = numPInput.toIntOption.getOrElse(0)

  println(s"Bitte gib die Namen für $numPlayers Spieler ein:")
  val playerNames = collectNames(numPlayers, Nil)

  println("Bitte die Feldgröße (min. 1): ")
  val fieldInput = StdIn.readLine()
  val fieldSize = fieldInput.toIntOption.getOrElse(0)

  val config = BoardConfig(fieldSize, numPlayers)
  val renderer = BoardRenderer(config)
  val rules = GameLogic(config)
  val initialState = GameState.create(playerNames, config)

  // Start der rekursiven Schleife
  gameLoop(initialState, 0, rules, renderer)
}

@tailrec
def collectNames(remaining: Int, acc: List[String]): List[String] = {
  if (remaining <= 0) {
    // Abbruchbedingung: Keine Namen mehr übrig Liste umdrehen (da Nil-Anfügen verkehrt herum baut)
    acc.reverse
  } else {
    // Eingabe lesen
    print(s"Name für Spieler ${acc.size + 1}: ")
    val name = StdIn.readLine()

    // Rekursion: Ein Name weniger zu sammeln, Name zur Liste hinzufügen
    collectNames(remaining - 1, name :: acc)
  }
}

@tailrec
def gameLoop(state: GameState, currentPlayerIndex: Int, rules: GameLogic, renderer: BoardRenderer): Unit = {
  // 1. Aktuellen Spieler bestimmen
  val currentPlayer = state.players(currentPlayerIndex)

  // 2. Board anzeigen
  println("\n" * 2) // Etwas Platz schaffen
  println(renderer.renderAll(state, rules))
  println(s"Dran ist: ${currentPlayer.name} (${currentPlayer.color})")

/*
  // 3. Würfeln (Simuliert)
  val roll = scala.util.Random.nextInt(6) + 1
  println(s"Du hast eine $roll gewürfelt!")

  // 4. Eingabe abfragen (Welches Piece 1-4?)
  print("Wähle eine Figur (1-4): ")
  val input = StdIn.readLine()

  // Eingabe sicher in eine Zahl umwandeln
  val pieceId = input.toIntOption.getOrElse(1)
*/
  print("Wähle eine Figur (1-4): ")
  val pieceInput = StdIn.readLine()
  print("Wie weit soll sie sich bewegen: ")
  val moveInput = StdIn.readLine()

  val pieceId = pieceInput.toIntOption.getOrElse(1)
  val moveBy = moveInput.toIntOption.getOrElse(0)
  // 5. Neuen Zustand berechnen
  val nextState = rules.movePiece(state, currentPlayer.name, pieceId, moveBy)

  // 6. Nächsten Spieler bestimmen (Reihum)
  val nextPlayerIndex = (currentPlayerIndex + 1) % state.players.size

  // 7. Rekursiver Aufruf -> Die "Schleife" geht von vorne los mit neuen Werten
  gameLoop(nextState, nextPlayerIndex, rules, renderer)
}