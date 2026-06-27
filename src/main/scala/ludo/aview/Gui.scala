package ludo.aview

import ludo.controller.ControllerInterface
import ludo.util.Observer
import scalafx.application.Platform
import scalafx.Includes.*
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.stage.Stage

class Gui()(using controller: ControllerInterface) extends Observer {

  controller.add(this)

  private var mainStage: Stage = null

  Platform.startup(() => {
    mainStage = new Stage {
      title.value = "Ludo - ScalaFX"
      width = 900
      height = 800
      maximized = true
      scene = new Scene {
        fill = Color.web("#f4f4f4")
        root = GuiLogic.createRootPane(controller.gameState, controller)
      }
    }
    mainStage.show()
  })

  override def update(): Unit = {
    Platform.runLater(() => {
      if (mainStage != null && mainStage.scene.value != null) {
        mainStage.scene.value.root = GuiLogic.createRootPane(controller.gameState, controller)
      }
    })
  }
}