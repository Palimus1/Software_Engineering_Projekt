package ludo

import scala.io.AnsiColor

enum PlayerColor(val ansiCode: String):
  case Blue extends PlayerColor(AnsiColor.BLUE)
  case Red extends PlayerColor(AnsiColor.RED)
  case Green extends PlayerColor(AnsiColor.GREEN)
  case Yellow extends PlayerColor(AnsiColor.YELLOW)

case class Piece(id: Int, color: PlayerColor, position: Int)

case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

case class GameState(players: List[Player])
//Companion Object mit statischer methode um GameState richtig zu initialisieren
object GameState {
  def create(playerNames: List[String], config: BoardConfig): GameState = {

    val color = List(PlayerColor.Blue,PlayerColor.Red,PlayerColor.Green,PlayerColor.Yellow)
    //  zip erstellt aus zwei listen eine tupel liste
    //  zipWithIndex fügt ein index hinzu, wieder ein tupel.      hier: tupel in tupel
    val players = playerNames.zip(color).zipWithIndex.map { case ((name, color), index) =>
      //  offset berechnen
      val offset = index * (config.fieldSize / playerNames.size)
      //4 pieces erstellen
      val pieces = (1 to 4).map(id => Piece(id, color, 0)).toList

      Player(name, color, pieces, offset)
    }
    GameState(players)
  }
}

case class BoardConfig(fieldSize: Int, numPlayers: Int)

case class BoardRenderer(config: BoardConfig):
  // Konzept für Brett: Hochzählen
  // 0 = Base, 1-40 = Weg, 41-44 = Ziel
  def display(state: GameState, rules: GameLogic): String =
    val range = 1 until (rules.config.fieldSize + 1)

    val occupiedFields = for {
      p <- state.players
      piece <- p.pieces
      globalPos <- rules.getGlobalPosition(p, piece)
    } yield globalPos -> (p, piece)
    /*
    yield gibt hier eine Liste mit geschachtelten Tupeln: (pos -> (player, piece))
                                              '->' ist schönere Schreibweise für Tupel
    */
    val posMap = occupiedFields.toMap
    //erzeugt aus liste eine Map wo globalpos der key ist

    range.map { pos =>
      posMap.get(pos) match {
        case Some((player, piece)) =>
          // Zeigt anfangsbuchstabe der Farbe und piece-id (plus hat die Farbe)
          s"|${player.color.ansiCode}${player.color.toString.head}${piece.id}${AnsiColor.RESET}|"

        case None => "|__|"
      }
    }.mkString("")

case class GameLogic(config: BoardConfig) {

  private val stepsToHome = config.fieldSize // z.B. 40
  private val homeSize = 4 // Plätze im Zielhaus
  private val maxPosition: Int = stepsToHome + homeSize // 44

  def movePiece(state: GameState, playerName: String, pieceId: Int, movedBy: Int): GameState =

    def calculateNewPosition(piece: Piece): Int =
      piece.position match
        case 0 if movedBy == 6 => 1 // aus Base raus
        case 0 => 0 // wenn in base ohne 6
        case curr if (curr + movedBy <= maxPosition) => curr + movedBy
        case _ => piece.position

    val newPlayers = state.players.map { p =>
      if p.name == playerName then
        val newPieces = p.pieces.map { piece =>
          if piece.id == pieceId then
            piece.copy(position = calculateNewPosition(piece))
          else
            piece
        }
        p.copy(pieces = newPieces)
      else p
    }
    state.copy(players = newPlayers)

  def getGlobalPosition(p: Player, piece: Piece): Option[Int] = {
    if (piece.position <= 0 || piece.position > stepsToHome) {
      // Basis (0) oder Zielhaus (>40) haben keine globale Feldnummer
      None
    } else {
      // Korrekte Modulo-Berechnung für 1-basierte Felder
      val global = ((piece.position + p.startOffset - 1) % config.fieldSize) + 1
      Some(global)
    }
  }
}

