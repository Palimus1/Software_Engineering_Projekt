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

  private var mainStage: Option[Stage] = None

  Platform.startup(() => {
    val stage = new Stage {
      title.value = "Ludo - ScalaFX"
      width = 900
      height = 800
      maximized = true
      scene = new Scene {
        fill = Color.web("#f4f4f4")
        root = GuiLogic.createRootPane(controller.gameState, controller)
      }
    }
    mainStage = Some(stage)
    stage.show()
  })

  override def update(): Unit = {
    Platform.runLater(() => {
      mainStage.foreach { stage =>
        Option(stage.scene.value).foreach { scene =>
          scene.root = GuiLogic.createRootPane(controller.gameState, controller)
        }
      }
    })
  }
}