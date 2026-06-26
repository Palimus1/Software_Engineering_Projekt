package ludo.controller.impl

import ludo.model.GameState
import ludo.util.Command

import scala.util.{Failure, Success, Try}


class RollCommand(roll: Int, controller: Controller) extends Command {

  private val oldState: GameState = controller.gameState
  private var newState: GameState = controller.gameState

  override def doStep(): Unit = {
    oldState.phase.handleRoll(oldState, roll) match {
      case Success(state) =>
        newState = state.copy(lastError = Success(())) 
      case Failure(exception) =>
        newState = oldState.copy(lastError = Failure(exception), message = None)
    }
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
  private val oldState: GameState = controller.gameState
  private var newState: GameState = controller.gameState

  override def doStep(): Unit = {
    oldState.phase.handleMove(oldState, pieceId) match {
      case Success(state) =>
        newState = state.copy(lastError = Success(()))
      case Failure(exception) =>
        newState = oldState.copy(lastError = Failure(exception), message = None)
    }
    controller.gameState = newState
  }

  override def undoStep(): Unit = {
    controller.gameState = oldState
  }

  override def redoStep(): Unit = {
    controller.gameState = newState
  }
}