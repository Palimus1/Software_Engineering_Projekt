import ludo.LudoModule
import ludo.aview.{Gui, Tui}
import ludo.controller.ControllerInterface
import ludo.controller.impl.Controller
import ludo.model.*

import scala.annotation.tailrec
import scala.io.StdIn

@main def main(): Unit = {
  println("Willkommen zu Mensch aerger dich nicht\n")

  val initialState = GameState.createSetup()
  val module = new LudoModule(initialState)

  import module.given

  val tui = Tui()
  val gui = new Gui()

  tui.update()
  gameLoop()
}

@tailrec
def gameLoop()(using controller: ControllerInterface): Unit = {

  val state = controller.gameState

  if (state.phase == GameOverPhase) {
    return
  }

  val input = StdIn.readLine().trim

  state.phase match {
    case _: SetupPhase =>
      if (input.toLowerCase == "q") {
        println("Spiel wird beendet.")
        sys.exit(0)
      }
      controller.doSetup(input)
      
    case _ =>
      input.toLowerCase match {
        case "q" =>
          println("Spiel wird beendet.")
          sys.exit(0)
        case "w" =>
          controller.rollDice()
        case "1" | "2" | "3" | "4" =>
          controller.doMove(input.toInt)
        case "u" =>
          controller.undo()
        case "r" =>
          controller.redo()
        case "s" =>
          controller.save()
          println("Spiel gespeichert.")
        case "l" =>
          controller.load()
          println("Spiel geladen.")
        case "" =>
        case _ =>
          println("Ungueltige Eingabe! Bitte 'w', '1'-'4', 'u', 'r', 's', 'l' oder 'q' eingeben.")
      }
  }

  gameLoop()
}