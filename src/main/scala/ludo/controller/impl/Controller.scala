package ludo.controller.impl

import ludo.controller.ControllerInterface
import ludo.model.*
import ludo.util.{Observable, UndoManager}

class Controller(var gameState: GameState) extends ControllerInterface:

  private val undoManager = new UndoManager

  override def rollDice(roll: Int = scala.util.Random.between(1, 7)): Unit = {
    undoManager.doStep(new RollCommand(roll, this))
    notifyObservers()
  }

  override def doMove(pieceId: Int): Unit = {
    undoManager.doStep(new MoveCommand(pieceId, this))
    notifyObservers()
  }

  override def undo(): Unit = {
    undoManager.undoStep()
    notifyObservers()
  }

  override def redo(): Unit = {
    undoManager.redoStep()
    notifyObservers()
  }