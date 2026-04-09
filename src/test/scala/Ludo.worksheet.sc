/*
  Authors: Stella Keller, Paul Boos
*/


object GameLogic {
  enum PlayerColor:
    case Red, Blue, Green, Yellow

  case class Piece(id: Int, color: PlayerColor, position: Int)

  case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)
  //wenn man case weglässt, lässt sich player nicht mehr printen
  //weil ohne case keine to_String methode existiert

  case class GameState(players: List[Player])

  def movePiece(state: GameState, playerName: String, pieceId: Int, movedBy: Int): GameState =
    val newPlayers = state.players.map { p =>
      if (p.name == playerName) then

        val newPieces = p.pieces.map { piece =>
          if (piece.id == pieceId)
            /*return*/ piece.copy(position = piece.position + movedBy)
          else /*return*/ piece
        }
        /*return*/p.copy(pieces = newPieces)

      else
        /*return*/p
    }
    /*return*/state.copy(players = newPlayers)

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
      val range = 1 until size

      val occupiedFields = for {
        p <- players
        piece <- p.pieces
        globalPos <- GameLogic.getGlobalPosition(p, piece)
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
            // Zeigt anfangsbuchstabe der Farbe und pieceid
            s"|${player.color.toString.head}${piece.id}|"

          case None => "|__|"
        }
      }.mkString("")



}

import GameLogic._

val bluePieces = List(Piece(1, PlayerColor.Blue, 1),
  Piece(2, PlayerColor.Blue, 0),
  Piece(3, PlayerColor.Blue, 0),
  Piece(4, PlayerColor.Blue, 0))
val redPieces = List(Piece(1, PlayerColor.Red, 0),
  Piece(2, PlayerColor.Red, 0),
  Piece(3, PlayerColor.Red, 9),
  Piece(4, PlayerColor.Red, 0))
val yellowPieces = List(Piece(1, PlayerColor.Yellow, 0),
  Piece(2, PlayerColor.Yellow, 0),
  Piece(3, PlayerColor.Yellow, 0),
  Piece(4, PlayerColor.Yellow, 0))
val greenPieces = List(Piece(1, PlayerColor.Green, 0),
  Piece(2, PlayerColor.Green, 0),
  Piece(3, PlayerColor.Green, 30),
  Piece(4, PlayerColor.Green, 0))


val player1 = Player("Stella", PlayerColor.Blue, bluePieces, 0)
val player2 = Player("Ttella", PlayerColor.Red, redPieces, 10)
val player3 = Player("Utella", PlayerColor.Yellow, yellowPieces, 20)
val player4 = Player("Vtella", PlayerColor.Green, greenPieces, 30)




val state = GameState(List(player1, player2, player3, player4))

val fieldSize = 40
val board = Board(fieldSize)


// p1 auf Feld 2, p2 auf Feld 5

println(board.display(state.players))

val newState = movePiece(state,"Stella", 1, 6)

println(board.display(newState.players))
