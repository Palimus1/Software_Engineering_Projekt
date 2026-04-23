/*
  Authors: Stella Keller, Paul Boos
*/



import ludo._


val playerNames = List("Stella", "Ttella", "Utella", "Vtella")
val config = BoardConfig(20, 4)
val state = GameState.create(playerNames, config)
val renderer = BoardRenderer(config)
val rules = GameLogic(config)
val newState = rules.movePiece(state, "Stella", 1, 6)
val newNewState = rules.movePiece(newState, "Utella", 2, 6)
println(renderer.display(newNewState, rules))

