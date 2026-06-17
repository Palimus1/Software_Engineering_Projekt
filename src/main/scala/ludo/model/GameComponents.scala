package ludo.model


case class Piece(id: Int, color: PlayerColor, position: Int)

case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

case class BoardConfig(fieldSize: Int, numPlayers: Int, winStrategy: WinStrategy = StandardWinStrategy)

case class GameState(players: List[Player], config: BoardConfig,currentPlayerIndex: Int = 0,
                     errors: String = "", winner: String = "", diceRoll: Option[Int] = None,
                     rollAttempt: Int = 0, phase: GamePhase = RollingPhase):
  def currentPlayer: Player = players(currentPlayerIndex)

  def getGlobalPosition(p: Player, piece: Piece): Option[Int] = {
    if (piece.position <= 0 || piece.position > config.fieldSize) {
      None
    } else {
      Some(((piece.position + p.startOffset - 1) % config.fieldSize) + 1)
    }
  }

//Companion Object mit statischer methode um GameState richtig zu initialisieren
object GameState {
  
  def apply(players: List[Player], config: BoardConfig): GameState = {
    new GameState(players, config)
  }

  def create(playerNames: List[String], config: BoardConfig): GameState = {


    val defaults = List("PC 1", "PC 2", "PC 3", "PC 4")

    val limitedNames = playerNames
      .padTo(config.numPlayers, "")
      .zip(defaults)
      .map { case (name, defaultName) =>
        if (name.trim.isEmpty) defaultName else name.trim
      }
      .take(config.numPlayers)

    val colors = List(PlayerColor.Blue, PlayerColor.Red, PlayerColor.Green, PlayerColor.Yellow)

    // zip kombiniert nur so viele Elemente, wie in der kürzeren Liste sind
    val players = limitedNames.zip(colors).zipWithIndex.map { case ((name, color), index) =>
      // Offset-Berechnung bleibt dynamisch
      val offset = Math.round(index.toDouble * config.fieldSize.toDouble / config.numPlayers.toDouble).toInt
      val pieces = (1 to 4).map(id => Piece(id, color, 0)).toList
      Player(name, color, pieces, offset)
    }
    
    apply(players, config)
  }
}