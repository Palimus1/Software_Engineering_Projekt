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
    val player = state.currentPlayer

    val updatedPieces = player.pieces.map { p =>
      if (p.id == pieceId) {
        p.copy(position = calculatePos(p.position, movedBy))
      }
      else p
    }

    val updatedPlayer = player.copy(pieces = updatedPieces)
    //.updated ist ein .copy um nur ein element einer liste zu ändern
    val updatedPlayers = state.players.updated(state.currentPlayerIndex, updatedPlayer)

    val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size


    state.copy(players = updatedPlayers, currentPlayerIndex = nextPlayerIndex)
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