package ludo.aview

import ludo.model.*
import ludo.controller.Controller
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.io.AnsiColor

class TuiSpec extends AnyWordSpec with Matchers {
  "A TUI" should {
    val config = BoardConfig(40, 2)
    val state = GameState.create(List("Alice", "Bob"), config)
    val controller = Controller(state)
    val tui = Tui(controller)

    "render the home base correctly" in {
      val output = tui.processInput()
      output should include(s"[${AnsiColor.BLUE}B1${AnsiColor.RESET}]")
    }

    "render the target area correctly" in {
      val config = BoardConfig(40, 1)
      val pieces = List(Piece(1, PlayerColor.Blue, 44))
      val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
      val thisState = GameState(players, config)
      val thisController = Controller(thisState)
      val thisTui = Tui(thisController)
      val output = thisTui.processInput()
      output should include(s"{${AnsiColor.BLUE}B1${AnsiColor.RESET}}")
    }

    "render a piece on the field correctly" in {
      controller.gameState = controller.gameState.copy(
        players = controller.gameState.players.updated(0,
          controller.gameState.players(0).copy(
            pieces = controller.gameState.players(0).pieces.updated(0, Piece(1, PlayerColor.Blue, 5))
          )
        )
      )

      val output = tui.processInput()
      output should include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|")
    }

    "call update() and print to console when the controller state changes" in {
      val stream = new java.io.ByteArrayOutputStream()

      Console.withOut(stream) {
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(1)
      }

      val consoleOutput = stream.toString
      consoleOutput should include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|")
    }
  }
}