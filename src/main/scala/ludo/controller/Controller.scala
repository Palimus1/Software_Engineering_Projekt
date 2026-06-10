package ludo.controller

import ludo.model.*
import ludo.util.Observable

import scala.io.AnsiColor

class Controller(var gameState: GameState) extends Observable:

  def rollDice(roll: Int = scala.util.Random.between(1, 7)): Unit = {
    // Der Controller delegiert blind an die aktuelle Phase!
    gameState = gameState.phase.handleRoll(gameState, roll)
    notifyObservers()
  }

  def doMove(pieceId: Int): Unit = {
    // Der Controller delegiert blind an die aktuelle Phase!
    gameState = gameState.phase.handleMove(gameState, pieceId)
    notifyObservers()
  }