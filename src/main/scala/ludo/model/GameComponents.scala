package ludo.model

import ludo.model.memento.*

import scala.util.{Failure, Success, Try} // <--- WICHTIG: Neuer Import!

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
case class SetupInProgressException() extends LudoException
case class NotSetupPhaseException() extends LudoException

sealed trait GameEvent
case class AllPiecesBlockedEvent(roll: Int) extends GameEvent
case class InvalidRollRetryEvent(roll: Int, attemptsLeft: Int) extends GameEvent
case class ThreeStrikesEvent(roll: Int) extends GameEvent

case class Piece(id: Int, color: PlayerColor, position: Int)

case class Player(name: String, color: PlayerColor, pieces: List[Piece], startOffset: Int)

case class BoardConfig(fieldSize: Int, numPlayers: Int, winStrategy: WinStrategy = StandardWinStrategy)


case class GameState(players: List[Player], config: BoardConfig, currentPlayerIndex: Int = 0,
                     lastError: Try[Unit] = Success(()), message: Option[GameEvent] = None, winner: Option[Player] = None,
                     diceRoll: Option[Int] = None, rollAttempt: Int = 0, phase: GamePhase = RollingPhase):

  def currentPlayer: Player = players(currentPlayerIndex)

  def getGlobalPosition(p: Player, piece: Piece): Option[Int] = {
    if (piece.position <= 0 || piece.position > config.fieldSize) {
      None
    } else {
      Some(((piece.position + p.startOffset - 1) % config.fieldSize) + 1)
    }
  }

  def createMemento(): GameStateMemento = {
    GameStateMemento(
      players = this.players.map(player =>
        PlayerMemento(
          name = player.name,
          color = player.color.toString,
          startOffset = player.startOffset,
          pieces = player.pieces.map(piece =>
            PieceMemento(
              id = piece.id,
              color = piece.color.toString,
              position = piece.position
            )
          )
        )
      ),
      fieldSize = this.config.fieldSize,
      numPlayers = this.config.numPlayers,
      winStrategy = this.config.winStrategy.name,
      currentPlayerIndex = this.currentPlayerIndex,
      winnerColor = this.winner.map(_.color.toString), //gibt bei None wieder None?
      diceRoll = this.diceRoll,
      rollAttempt = this.rollAttempt,
      phase = this.phase.name
    )
  }

object GameState {

  def apply(players: List[Player], config: BoardConfig): GameState = {
    new GameState(players, config)
  }

  def createSetup(): GameState = {
    val dummyPlayer = Player("Setup", PlayerColor.Blue, Nil, 0)
    apply(List(dummyPlayer), BoardConfig(40, 4)).copy(phase = SetupPhase(SetupStep.NumPlayers))
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

  def fromMemento(memento: GameStateMemento): GameState = {
    val players = memento.players.map(memPlayer =>

      val color = PlayerColor.fromString(memPlayer.color)
      val pieces = memPlayer.pieces.map(memPiece =>
        Piece(
          id = memPiece.id,
          color = PlayerColor.fromString(memPiece.color),
          position = memPiece.position
        )
      )

      Player(name = memPlayer.name, color = color, pieces = pieces, startOffset = memPlayer.startOffset)
    )

    val config = BoardConfig(
      fieldSize = memento.fieldSize,
      winStrategy = WinStrategy.fromString(memento.winStrategy),
      numPlayers = memento.numPlayers)

    val phase = GamePhase.fromName(memento.phase)

    val message =
      if (phase == RollingPhase && memento.rollAttempt > 0) {
        Some(InvalidRollRetryEvent(0, 3 - memento.rollAttempt))
      } else{
        None
      }
    val winner = memento.winnerColor.flatMap( memColor =>
      val color = PlayerColor.fromString(memColor)
      players.find(_.color == color)
    )
    GameState(
      players = players,
      config = config,
      currentPlayerIndex = memento.currentPlayerIndex,
      lastError = Success(()),
      message = message,
      winner = winner,
      diceRoll = memento.diceRoll,
      rollAttempt = memento.rollAttempt,
      phase = GamePhase.fromName(memento.phase)
    )
  }

}