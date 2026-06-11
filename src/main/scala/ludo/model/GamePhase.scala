package ludo.model

import scala.io.AnsiColor

// Das ist dein State-Interface
trait GamePhase {
  def handleRoll(state: GameState, roll: Int): GameState
  def handleMove(state: GameState, pieceId: Int): GameState

  protected def maxPosition(state: GameState): Int = state.config.fieldSize + 4

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

// Zustand 1: Man muss wuerfeln
object RollingPhase extends GamePhase {
  override def handleMove(state: GameState, pieceId: Int): GameState = {
    state.copy(errors = "Du musst erst wuerfeln!")
  }

  override def handleRoll(state: GameState, roll: Int): GameState = {
    val currentplayer = state.currentPlayer
    val hasActivePiece = currentplayer.pieces.exists(p => p.position > 0 && p.position <= state.config.fieldSize)

    if (hasActivePiece) {
      if (hasValidMoves(currentplayer, roll, state)) {
        // Erfolgreich gewürfelt -> Wechsel in die MovingPhase
        state.copy(diceRoll = Some(roll), rollAttempt = 0, errors = "", phase = MovingPhase)
      } else {
        // Deadlock
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        state.copy(
          currentPlayerIndex = nextPlayerIndex, diceRoll = None, rollAttempt = 0,
          errors = s"Eine $roll gewuerfelt, aber alle Figuren sind blockiert! Naechster Spieler.",
          phase = RollingPhase // Bleibt in der RollingPhase für den nächsten Spieler
        )
      }
    } else {
      if (roll == 6 || hasValidMoves(currentplayer, roll, state)) {
        // Erfolgreich gewürfelt -> Wechsel in die MovingPhase
        state.copy(diceRoll = Some(roll), rollAttempt = 0, errors = "", phase = MovingPhase)
      } else {
        if (state.rollAttempt < 2) {
          val newRollAttempt = state.rollAttempt + 1
          state.copy(
            rollAttempt = newRollAttempt,
            errors = s"Eine $roll gewuerfelt! Kein gueltiger Zug. Du hast noch ${3 - newRollAttempt} Versuch(e) uebrig.",
            phase = RollingPhase
          )
        } else {
          val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
          state.copy(
            currentPlayerIndex = nextPlayerIndex, rollAttempt = 0, diceRoll = None,
            errors = s"Eine $roll gewuerfelt. Dreimal keinen Zug gehabt. Naechster Spieler ist dran.",
            phase = RollingPhase
          )
        }
      }
    }
  }
}

// Zustand 2: Man muss eine Figur bewegen
object MovingPhase extends GamePhase {
  override def handleRoll(state: GameState, roll: Int): GameState = {
    state.copy(errors = "Du hast schon gewuerfelt! Bitte bewege eine Figur (1-4).")
  }

  override def handleMove(state: GameState, pieceId: Int): GameState = {
    val currentplayer = state.currentPlayer
    val movedBy = state.diceRoll.get

    currentplayer.pieces.find(_.id == pieceId) match {
      case None =>
        state.copy(errors = "Die Figuren sind mit 1-4 indiziert! Bitte erneut waehlen.")

      case Some(pieceToMove) =>
        val relPos = calculatePos(pieceToMove.position, movedBy, state)
        val movedPiece = pieceToMove.copy(position = relPos)
        val targetGlobalPosOpt = state.getGlobalPosition(currentplayer, movedPiece)

        val isOvershooting = pieceToMove.position > 0 && relPos == pieceToMove.position
        val isBlocked = currentplayer.pieces.exists(p => p.id != pieceId && p.position > 0 && p.position == relPos)
        val hasPieceInBase = currentplayer.pieces.exists(p => p.position == 0)
        val isStartBlocked = currentplayer.pieces.exists(p => p.position == 1)
        val isInvalidBaseMove = pieceToMove.position == 0 && movedBy != 6

        if (isInvalidBaseMove) state.copy(errors = "Du brauchst eine 6 um die Base zu verlassen! Bitte erneut waehlen.")
        else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 0 && !isStartBlocked) state.copy(errors = "Du musst eine Figur aus der Base bewegen! Bitte erneut waehlen.")
        else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 1 && isStartBlocked) state.copy(errors = "Du musst das Startfeld freiraeumen! Bitte erneut waehlen.")
        else if (isOvershooting) state.copy(errors = "Der Zug ueberschreitet das Ziel! Bitte erneut waehlen.")
        else if (isBlocked) state.copy(errors = "Du kannst deine eigenen Figuren nicht schlagen! Bitte erneut waehlen.")
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
            val winnerText = s"Glueckwunsch! ${currentplayer.name}(${currentplayer.color.ansiCode}${currentplayer.color}${AnsiColor.RESET}) hat das Spiel gewonnen!"
            state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex, errors = "", winner = winnerText, diceRoll = None, rollAttempt = 0, phase = GameOverPhase) // <--- Spielende!
          } else {
            state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex, errors = "", diceRoll = None, rollAttempt = 0, phase = RollingPhase) // <--- Zurück zum Würfeln!
          }
        }
    }
  }
}

// Zustand 3: Das Spiel ist vorbei
object GameOverPhase extends GamePhase {
  override def handleRoll(state: GameState, roll: Int): GameState = state.copy(errors = "Das Spiel ist bereits vorbei!")
  override def handleMove(state: GameState, pieceId: Int): GameState = state.copy(errors = "Das Spiel ist bereits vorbei!")
}