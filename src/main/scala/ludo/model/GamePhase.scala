package ludo.model

import scala.util.{Try, Success, Failure}

trait GamePhase {
  def handleRoll(state: GameState, roll: Int): Try[GameState]
  def handleMove(state: GameState, pieceId: Int): Try[GameState]
  def handleSetup(state: GameState, input: String): Try[GameState] = Failure(NotSetupPhaseException())
  def name: String

  private def maxPosition(state: GameState): Int = state.config.fieldSize + 4

  protected def calculatePos(curr: Int, moved: Int, state: GameState): Int = {
    curr match {
      case 0 if moved == 6 => 1
      case 0 => 0
      case c if c + moved <= maxPosition(state) => c + moved
      case _ => curr
    }
  }

  protected def hasValidMoves(player: Player, roll: Int, state: GameState): Boolean = {
    player.pieces.exists { p =>
      val relPos = calculatePos(p.position, roll, state)
      val isInvalidBaseMove = p.position == 0 && roll != 6
      val isBlocked = player.pieces.exists(other => other.id != p.id && other.position > 0 && other.position == relPos)
      relPos != p.position && !isInvalidBaseMove && !isBlocked
    }
  }
}

object GamePhase {
  def fromName(name: String): GamePhase = name match {
    case "rolling"  => RollingPhase
    case "moving"   => MovingPhase
    case "gameover" => GameOverPhase
    case "setup"    => SetupPhase(SetupStep.NumPlayers)
    case other      => throw new IllegalArgumentException(s"Unknown game phase: $other")
  }
}

enum SetupStep:
  case NumPlayers
  case PlayerNames
  case FieldSize
  case GameMode

case class SetupData(names: List[String] = Nil, numPlayers: Int = 4, fieldSize: Int = 40)

case class SetupPhase(step: SetupStep, data: SetupData = SetupData()) extends GamePhase {
  override def handleRoll(state: GameState, roll: Int): Try[GameState] = Failure(SetupInProgressException())
  override def handleMove(state: GameState, pieceId: Int): Try[GameState] = Failure(SetupInProgressException())
  
  override def handleSetup(state: GameState, input: String): Try[GameState] = {
    step match {
      case SetupStep.NumPlayers =>
        val num = input.toIntOption.getOrElse(4).max(1).min(4)
        Success(state.copy(phase = SetupPhase(SetupStep.PlayerNames, data.copy(numPlayers = num))))
        
      case SetupStep.PlayerNames =>
        val newNames = data.names :+ (if(input.trim.isEmpty) s"Player ${data.names.size + 1}" else input.trim)
        if (newNames.size == data.numPlayers) {
           Success(state.copy(phase = SetupPhase(SetupStep.FieldSize, data.copy(names = newNames))))
        } else {
           Success(state.copy(phase = SetupPhase(SetupStep.PlayerNames, data.copy(names = newNames))))
        }
        
      case SetupStep.FieldSize =>
        val raw = input.toIntOption.getOrElse(40)
        val fs = if (raw < 4) 4 else if (raw % 2 != 0) raw + 1 else raw
        Success(state.copy(phase = SetupPhase(SetupStep.GameMode, data.copy(fieldSize = fs))))
        
      case SetupStep.GameMode =>
        val strategy = input.trim.toLowerCase match {
          case "blitz" => QuickWinStrategy
          case _ => StandardWinStrategy
        }
        val finalConfig = BoardConfig(data.fieldSize, data.numPlayers, strategy)
        val finalState = GameState.create(data.names, finalConfig)
        Success(finalState)
    }
  }
  override def name: String = "setup"
}

object RollingPhase extends GamePhase {
  override def handleMove(state: GameState, pieceId: Int): Try[GameState] = {
    Failure(MustRollFirstException())
  }

  override def handleRoll(state: GameState, roll: Int): Try[GameState] = {
    val currentplayer = state.currentPlayer
    val hasActivePiece = currentplayer.pieces.exists(p => p.position > 0 && p.position <= state.config.fieldSize)

    if (hasActivePiece) {
      if (hasValidMoves(currentplayer, roll, state)) {
        Success(state.copy(diceRoll = Some(roll), rollAttempt = 0, message = None, phase = MovingPhase))
      } else {
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        Success(state.copy(
          currentPlayerIndex = nextPlayerIndex, diceRoll = None, rollAttempt = 0,
          message = Some(AllPiecesBlockedEvent(roll)),
          phase = RollingPhase
        ))
      }
    } else {
      if (roll == 6 || hasValidMoves(currentplayer, roll, state)) {
        Success(state.copy(diceRoll = Some(roll), rollAttempt = 0, message = None, phase = MovingPhase))
      } else {
        if (state.rollAttempt < 2) {
          val newRollAttempt = state.rollAttempt + 1
          Success(state.copy(
            rollAttempt = newRollAttempt,
            message = Some(InvalidRollRetryEvent(roll, 3 - newRollAttempt)),
            phase = RollingPhase
          ))
        } else {
          val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
          Success(state.copy(
            currentPlayerIndex = nextPlayerIndex, rollAttempt = 0, diceRoll = None,
            message = Some(ThreeStrikesEvent(roll)),
            phase = RollingPhase
          ))
        }
      }
    }
  }

  override def name: String = "rolling"
}

object MovingPhase extends GamePhase {
  override def handleRoll(state: GameState, roll: Int): Try[GameState] = {
    Failure(AlreadyRolledException())
  }

  override def handleMove(state: GameState, pieceId: Int): Try[GameState] = {
    val currentplayer = state.currentPlayer
    val movedBy = state.diceRoll.get

    currentplayer.pieces.find(_.id == pieceId) match {
      case None =>
        Failure(InvalidPieceException())

      case Some(pieceToMove) =>
        val relPos = calculatePos(pieceToMove.position, movedBy, state)
        val movedPiece = pieceToMove.copy(position = relPos)
        val targetGlobalPosOpt = state.getGlobalPosition(currentplayer, movedPiece)

        val isOvershooting = pieceToMove.position > 0 && relPos == pieceToMove.position
        val isBlocked = currentplayer.pieces.exists(p => p.id != pieceId && p.position > 0 && p.position == relPos)
        val hasPieceInBase = currentplayer.pieces.exists(p => p.position == 0)
        val isStartBlocked = currentplayer.pieces.exists(p => p.position == 1)
        val isInvalidBaseMove = pieceToMove.position == 0 && movedBy != 6

        if (isInvalidBaseMove) Failure(NeedSixException())
        else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 0 && !isStartBlocked) Failure(BaseLeaveException())
        else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 1 && isStartBlocked) Failure(BaseClearException())
        else if (isOvershooting) Failure(OvershootException())
        else if (isBlocked) Failure(BlockedException())
        else {
          val updatedPlayers = state.players.map { player =>
            if (player == currentplayer) {
              player.copy(pieces = player.pieces.map(piece => if (piece.id == pieceId) movedPiece else piece))
            } else {
              player.copy(pieces = player.pieces.map { enemyPiece =>
                val enemyGlobalPosOpt = state.getGlobalPosition(player, enemyPiece)
                if (targetGlobalPosOpt.isDefined && targetGlobalPosOpt == enemyGlobalPosOpt) enemyPiece.copy(position = 0) else enemyPiece
              })
            }
          }

          val nextPlayerIndex = if (movedBy == 6) state.currentPlayerIndex else (state.currentPlayerIndex + 1) % state.players.size
          val updatedCurrentPlayer = updatedPlayers(state.currentPlayerIndex)
          val isWinner = state.config.winStrategy.isWinner(updatedCurrentPlayer, state.config.fieldSize)

          if (isWinner) {
            Success(state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex, message = None, lastError = Success(()), winner = Some(updatedCurrentPlayer), diceRoll = None, rollAttempt = 0, phase = GameOverPhase))
          } else {
            Success(state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex, message = None, lastError = Success(()), diceRoll = None, rollAttempt = 0, phase = RollingPhase))
          }
        }
    }
  }

  override def name: String = "moving"
}

object GameOverPhase extends GamePhase {
  override def handleRoll(state: GameState, roll: Int): Try[GameState] = Failure(GameOverException())
  override def handleMove(state: GameState, pieceId: Int): Try[GameState] = Failure(GameOverException())
  override def name = "gameover"
}