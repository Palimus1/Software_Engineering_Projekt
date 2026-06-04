package ludo.model


case class Piece(id: Int, color: PlayerColor, position: Int)

case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

case class BoardConfig(fieldSize: Int, numPlayers: Int)

case class GameState(players: List[Player], currentPlayerIndex: Int = 0, errors: String = ""):
  def currentPlayer: Player = players(currentPlayerIndex)
//Companion Object mit statischer methode um GameState richtig zu initialisieren
object GameState {
  def create(playerNames: List[String], config: BoardConfig): GameState = {
    
    //mit namen auffüllen, falls zu wenig angegeben
    val defaults = List("PC 1", "PC 2", "PC 3", "PC 4")
    // Nur so viele Namen nehmen, wie in config erlaubt
    val limitedNames = (playerNames ++ defaults).take(config.numPlayers)

    val colors = List(PlayerColor.Blue, PlayerColor.Red, PlayerColor.Green, PlayerColor.Yellow)

    // zip kombiniert nur so viele Elemente, wie in der kürzeren Liste sind
    val players = limitedNames.zip(colors).zipWithIndex.map { case ((name, color), index) =>
      // Offset-Berechnung bleibt dynamisch
      val offset = index * (config.fieldSize / config.numPlayers)
      val pieces = (1 to 4).map(id => Piece(id, color, 0)).toList
      Player(name, color, pieces, offset)
    }
    GameState(players, 0)
  }
}