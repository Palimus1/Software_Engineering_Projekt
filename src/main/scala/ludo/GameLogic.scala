package ludo

object GameLogic {
  enum PlayerColor:
    case Red, Blue, Green, Yellow

  case class Piece(id: Int, color: PlayerColor, position: Int)

  case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

  case class GameState(players: List[Player])

  def movePiece(state: GameState, playerName: String, pieceId: Int, movedBy: Int): GameState =
    val newPlayers = state.players.map { p =>
      if p.name == playerName then
        val newPieces = p.pieces.map { piece =>
          if (piece.id == pieceId)
            val currPos = piece.position
            val newPos = currPos + movedBy
            if (currPos == 0 || currPos == 44 || newPos > 44)
              piece
            else
              piece.copy(position = newPos)
          else piece
        }
        p.copy(pieces = newPieces)
      else
        p
    }
    state.copy(players = newPlayers)

  def getGlobalPosition(p: Player, piece: Piece): Option[Int] = {
    if (piece.position == 0) None // Noch in der Basis
    else if (piece.position > 40) None // Im Zielhaus (separat zu behandeln)
    else {
      // Die Modulo-Logik für den Rundlauf
      val global = ((piece.position + p.startOffset - 1) % 40) + 1
      Some(global)  //letzte zeile -> return Some(global)
      //Some(wert) erzeugt ein Option Objekt mit wert darin geschachtelt
    }
  }

  // Konzept für Brett: Hochzählen
  // 0 = Base, 1-40 = Weg, 41-44 = Ziel
  case class Board(size: Int):
    def display(players: List[Player]): String =
      val range = 1 until (size + 1)

      val occupiedFields = for {
        p <- players
        piece <- p.pieces
        globalPos <- getGlobalPosition(p, piece)
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
            // Zeigt anfangsbuchstabe der Farbe und piece-id
            s"|${player.color.toString.head}${piece.id}|"

          case None => "|__|"
        }
      }.mkString("")
}

