/*
  Authors: Stella Keller, Paul Boos
*/



import ludo.GameLogic._

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
