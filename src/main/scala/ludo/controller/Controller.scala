package ludo.controller

import ludo.model.*
import ludo.util.Observable

class Controller(var gameState: GameState, val config: BoardConfig) extends Observable:

  private val stepsToHome = config.fieldSize
  private val maxPosition = stepsToHome + 4

  def doMove(pieceId: Int, movedBy: Int): Unit = {
    gameState = movePieceLogic(gameState, pieceId, movedBy)
    // Hier kommt später der Aufruf für notifyObservers() hin!
    notifyObservers()
  }

  // Die reine Logik-Funktion (ehemals in GameLogic)
  private def movePieceLogic(state: GameState, pieceId: Int, movedBy: Int): GameState = {
    val currentplayer = state.currentPlayer

    currentplayer.pieces.find(_.id == pieceId) match { //piece mit der pieceID holen
      case None =>  //falls piece nicht vorhande also nicht 1-4
        state

      case Some(pieceToMove) =>
        val relPos = calculatePos(pieceToMove.position, movedBy)  //neue rel. Position des bewegten Piece berechnen
        val movedPiece = pieceToMove.copy(position = relPos)      //neues Piece mit aktualisierter Position erstellen

        val targetGlobalPosOpt = getGlobalPosition(currentplayer, movedPiece)  // Globale Position des bewegten Piece, kann None sein wenn in Base

        //schauen, ob ein eigenes Piece die bewegung blockiert
        val isBlocked = currentplayer.pieces.exists( p => p.id != pieceId && p.position > 0 && p.position == relPos); //exists prüft bedingung für jedes element und ist wahr falls es für 1 element wahr ist

        if (isBlocked) {  //falls blockiert wird einfach nichts am gamestate geändert und spieler ist nochmal dran
          state
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

          val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
          state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex)
        }
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