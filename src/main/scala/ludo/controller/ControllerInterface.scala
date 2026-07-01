package ludo.controller

import ludo.model.GameState
import ludo.util.Observable


trait ControllerInterface extends Observable {
  def gameState: GameState
  def doSetup(input: String): Unit
  def rollDice(roll: Int = scala.util.Random.between(1, 7)): Unit
  def doMove(pieceId: Int): Unit
  def undo(): Unit
  def redo(): Unit
  def save(): Unit
  def load(): Unit
}
