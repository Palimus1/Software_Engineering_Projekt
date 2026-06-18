package ludo.model

sealed trait LudoException extends Exception
case class NeedSixException() extends LudoException
case class BlockedException() extends LudoException
case class OvershootException() extends LudoException
case class InvalidPieceException() extends LudoException
case class AlreadyRolledException() extends LudoException
case class MustRollFirstException() extends LudoException
case class GameOverException() extends LudoException
case class BaseClearException() extends LudoException
case class BaseLeaveException() extends LudoException

// --- NEU: Unsere typisierten Info-Events! ---
sealed trait GameEvent
case class AllPiecesBlockedEvent(roll: Int) extends GameEvent
case class InvalidRollRetryEvent(roll: Int, attemptsLeft: Int) extends GameEvent
case class ThreeStrikesEvent(roll: Int) extends GameEvent

case class Piece(id: Int, color: PlayerColor, position: Int)

case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

case class BoardConfig(fieldSize: Int, numPlayers: Int, winStrategy: WinStrategy = StandardWinStrategy)

// --- ANGEPASST: message als Option[GameEvent] und winner als Option[Player] ---
case class GameState(players: List[Player], config: BoardConfig, currentPlayerIndex: Int = 0,
                     lastError: Option[Throwable] = None, message: Option[GameEvent] = None, winner: Option[Player] = None,
                     diceRoll: Option[Int] = None, rollAttempt: Int = 0, phase: GamePhase = RollingPhase):
  def currentPlayer: Player = players(currentPlayerIndex)

  def getGlobalPosition(p: Player, piece: Piece): Option[Int] = {
    if (piece.position <= 0 || piece.position > config.fieldSize) {
      None
    } else {
      Some(((piece.position + p.startOffset - 1) % config.fieldSize) + 1)
    }
  }

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

    val players = limitedNames.zip(colors).zipWithIndex.map { case ((name, color), index) =>
      val offset = Math.round(index.toDouble * config.fieldSize.toDouble / config.numPlayers.toDouble).toInt
      val pieces = (1 to 4).map(id => Piece(id, color, 0)).toList
      Player(name, color, pieces, offset)
    }
    apply(players, config)
  }
}