package ludo.controller

import ludo.model.*
import ludo.util.Observable

import scala.io.AnsiColor

class Controller(var gameState: GameState, val config: BoardConfig) extends Observable:

  private val stepsToHome = config.fieldSize
  private val maxPosition = stepsToHome + 4

  def doMove(pieceId: Int): Unit = {
    gameState = movePieceLogic(gameState, pieceId)
    // Hier kommt später der Aufruf für notifyObservers() hin!
    notifyObservers()
  }

  // Die reine Logik-Funktion (ehemals in GameLogic)
  private def movePieceLogic(state: GameState, pieceId: Int): GameState = {
    val currentplayer = state.currentPlayer
    val movedBy = state.diceRoll.get

    currentplayer.pieces.find(_.id == pieceId) match { //piece mit der pieceID holen
      case None =>  //falls piece nicht vorhanden also nicht 1-4
        state.copy(errors = "Die Figuren sind mit 1-4 indiziert! Bitte erneut wählen.")

      case Some(pieceToMove) =>
        val relPos = calculatePos(pieceToMove.position, movedBy)  //neue rel. Position des bewegten Piece berechnen
        val movedPiece = pieceToMove.copy(position = relPos)      //neues Piece mit aktualisierter Position erstellen

        val targetGlobalPosOpt = getGlobalPosition(currentplayer, movedPiece)  // Globale Position des bewegten Piece, kann None sein wenn in Base

        //schauen, ob ein eigenes Piece die bewegung blockiert
        val isOvershooting = pieceToMove.position > 0 && relPos == pieceToMove.position
        val isBlocked = currentplayer.pieces.exists( p => p.id != pieceId && p.position > 0 && p.position == relPos); //exists prüft bedingung für jedes element und ist wahr falls es für 1 element wahr ist
        val hasPieceInBase = currentplayer.pieces.exists( p => p.position == 0)
        val isStartBlocked = currentplayer.pieces.exists( p => p.position == 1)
        val isInvalidBaseMove = pieceToMove.position == 0 && movedBy != 6
        if (isInvalidBaseMove) {
          state.copy(errors = "Du brauchst eine 6 um die Base zu verlassen! Bitte erneut wählen.")
        } else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 0 && !isStartBlocked) {
          state.copy(errors = "Du musst eine Figur aus der Base bewegen! Bitte erneut wählen.")
        } else if (movedBy == 6 && hasPieceInBase && pieceToMove.position != 1 && isStartBlocked) {
          state.copy(errors = "Du musst das Startfeld freiräumen! Bitte erneut wählen.")
        } else if (isOvershooting) {
          state.copy(errors = "Der Zug überschreitet das Ziel! Bitte erneut wählen.")
        } else if (isBlocked) {  //falls blockiert wird einfach nichts am gamestate geändert und spieler ist nochmal dran
          state.copy(errors = "Du kannst deine eigenen Figuren nicht schlagen! Bitte erneut wählen.")
        } else {
          val updatedPlayers = state.players.map { player =>
            //bewegtes pieces wird jetzt im aktuellen spieler aktualisiert
            if (player == currentplayer) {
              val updatedPieces = player.pieces.map { piece =>
                if (piece.id == pieceId) movedPiece else piece
              }
              player.copy(pieces = updatedPieces)

            } else {//andere spieler
              val updatedPieces = player.pieces.map { enemyPiece =>
                val enemyGlobalPosOpt = getGlobalPosition(player, enemyPiece)

                if (targetGlobalPosOpt.isDefined && targetGlobalPosOpt == enemyGlobalPosOpt) {
                  enemyPiece.copy(position = 0) //Schlagen
                } else {
                  enemyPiece
                }
              }

              player.copy(pieces = updatedPieces)
            }
          }

          val nextPlayerIndex = if (movedBy == 6) {
            state.currentPlayerIndex
          } else {
            (state.currentPlayerIndex + 1) % state.players.size
          }

          val updatedCurrentPlayer = updatedPlayers(state.currentPlayerIndex)
          val isWinner = updatedCurrentPlayer.pieces.forall( p => p.position > stepsToHome)
          if (isWinner) {
            val winnerText = s"Glückwunsch! ${currentplayer.name}(${currentplayer.color.ansiCode}${currentplayer.color}${AnsiColor.RESET}) hat das Spiel gewonnen!"
            state.copy(
              players = updatedPlayers,
              currentPlayerIndex = nextPlayerIndex,
              errors = "",
              winner = winnerText,
              diceRoll = None,
              rollAttempt = 0)
          } else {
            state.copy(players = updatedPlayers,
              currentPlayerIndex = nextPlayerIndex,
              errors = "",
              diceRoll = None,
              rollAttempt = 0)
          }

        }
    }
  }

  def rollDice(roll: Int = scala.util.Random.between(1, 7)): Unit = {
    gameState = rollDiceLogic(gameState, roll)
    notifyObservers()
  }

  private def rollDiceLogic(state: GameState, roll: Int): GameState = {
    // val roll = scala.util.Random.between(1, 7)  <--- DIESE ZEILE WIRD GELÖSCHT
    val currentplayer = state.currentPlayer

    val hasActivePiece = currentplayer.pieces.exists(p => p.position > 0 && p.position <= config.fieldSize)

    if (hasActivePiece) {
      if (hasValidMoves(currentplayer, roll)) {
        // Alles super, er kann ziehen
        state.copy(diceRoll = Some(roll), rollAttempt = 0, errors = "")
      } else {
        // DEADLOCK! Er hat zwar Figuren draußen, aber keine kann sich mit dieser Zahl bewegen.
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        state.copy(
          currentPlayerIndex = nextPlayerIndex,
          diceRoll = None,
          rollAttempt = 0,
          errors = s"Eine $roll gewürfelt, aber alle Figuren sind blockiert! Nächster Spieler."
        )
      }
    } else {
      if (roll == 6) {
        state.copy(diceRoll = Some(6), rollAttempt = 0, errors = "")
      } else {
        if (hasValidMoves(currentplayer, roll)) {
          state.copy(diceRoll = Some(roll), rollAttempt = 0, errors = "")
        } else {
          // Kein gültiger Zug möglich, Versuche hochzählen
          if (state.rollAttempt < 2) {
            val newRollAttempt = state.rollAttempt + 1
            state.copy(
              rollAttempt = newRollAttempt,
              errors = s"Eine $roll gewürfelt! Kein gültiger Zug. Du hast noch ${3 - newRollAttempt} Versuch(e) übrig."
            )
          } else { // Alle 3 Versuche verbraucht
            val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
            state.copy(
              currentPlayerIndex = nextPlayerIndex,
              rollAttempt = 0,
              diceRoll = None,
              errors = s"Eine $roll gewürfelt. Dreimal keinen Zug gehabt. Nächster Spieler ist dran."
            )
          }
        }
      }
    }
  }

  private def hasValidMoves(player: Player, roll: Int): Boolean = {
    player.pieces.exists { p =>
      val relPos = calculatePos(p.position, roll)

      val isInvalidBaseMove = p.position == 0 && roll != 6
      val isBlocked = player.pieces.exists(other => other.id != p.id && other.position > 0 && other.position == relPos)
      // relPos != p.position ist wenn man übers ziel hinausschießt
      relPos != p.position && !isInvalidBaseMove && !isBlocked
    }
  }

  private def calculatePos(curr: Int, moved: Int): Int = {
    curr match {
      case 0 if moved == 6 => 1
      case 0 => 0
      case c if c + moved <= maxPosition => c + moved
      case _ => curr
    }
  }

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