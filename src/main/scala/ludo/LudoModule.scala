package ludo

import ludo.model.GameState
import ludo.controller.impl.Controller
import ludo.controller.ControllerInterface

class LudoModule(initialState: GameState) {
  
  given ControllerInterface = new Controller(initialState)

}