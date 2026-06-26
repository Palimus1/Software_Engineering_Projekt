package ludo

import ludo.controller.ControllerInterface
import ludo.controller.impl.Controller
import ludo.fileio.FileIOInterface
import ludo.fileio.impl.JsonFileIO
import ludo.model.GameState
import ludo.fileio.impl.XmlFileIO

class LudoModule(initialState: GameState) {

  given FileIOInterface = new XmlFileIO
  //given FileIOInterface = new JsonFileIO
  given ControllerInterface = new Controller(initialState, summon[FileIOInterface])

}
