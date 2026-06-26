package ludo.controller.impl

import ludo.controller.ControllerInterface
import ludo.fileio.FileIOInterface
import ludo.fileio.impl.JsonFileIO
import ludo.model.*
import ludo.util.{Observable, UndoManager}

class Controller(var gameState: GameState, fileIO: FileIOInterface) extends ControllerInterface:

  def this(gameState: GameState) = this(gameState, new JsonFileIO)

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

  override def save(): Unit = {
    fileIO.save(gameState.createMemento())
  }

  override def load(): Unit = {
    gameState = GameState.fromMemento(fileIO.load())
    undoManager.clear()
    notifyObservers()
  }
