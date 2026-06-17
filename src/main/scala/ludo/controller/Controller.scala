package ludo.controller

import ludo.model.*
import ludo.util.{Observable, UndoManager}

class Controller(var gameState: GameState) extends Observable:

  private val undoManager = new UndoManager

  def rollDice(roll: Int = scala.util.Random.between(1, 7)): Unit = {
    undoManager.doStep(new RollCommand(roll, this))
    notifyObservers()
  }

  def doMove(pieceId: Int): Unit = {
    undoManager.doStep(new MoveCommand(pieceId, this))
    notifyObservers()
  }

  def undo(): Unit = {
    undoManager.undoStep()
    notifyObservers()
  }

  def redo(): Unit = {
    undoManager.redoStep()
    notifyObservers()
  }