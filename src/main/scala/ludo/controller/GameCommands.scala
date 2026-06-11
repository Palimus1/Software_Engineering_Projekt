package ludo.controller

import ludo.model.GameState
import ludo.util.Command

// Kommando für das Würfeln
class RollCommand(roll: Int, controller: Controller) extends Command {
  
  private val oldState: GameState = controller.gameState
  private var newState: GameState = controller.gameState

  override def doStep(): Unit = {
    
    newState = oldState.phase.handleRoll(oldState, roll)
    controller.gameState = newState
  }

  override def undoStep(): Unit = {
    controller.gameState = oldState
  }

  override def redoStep(): Unit = {
    controller.gameState = newState
  }
}

// Kommando für das Bewegen einer Figur
class MoveCommand(pieceId: Int, controller: Controller) extends Command {
  // FEHLER BEHOBEN: 'val' friert den Zustand SOFORT bei Objekterstellung ein
  private val oldState: GameState = controller.gameState
  private var newState: GameState = controller.gameState

  override def doStep(): Unit = {
    // FEHLER BEHOBEN: Wir nutzen den eingefrorenen oldState als Basis
    newState = oldState.phase.handleMove(oldState, pieceId)
    controller.gameState = newState
  }

  override def undoStep(): Unit = {
    controller.gameState = oldState
  }

  override def redoStep(): Unit = {
    controller.gameState = newState
  }
}