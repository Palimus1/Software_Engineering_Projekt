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
    val controller = Controller(state, config)
    val tui = Tui(controller)

    "render the home base correctly" in {
      val output = tui.processInput()
      // Alice ist Blau (B), Figur 1 sollte in Base sein: [B1]
      output should include(s"[${AnsiColor.BLUE}B1${AnsiColor.RESET}]")
    }

    "render the target area correctly" in {
      val config = BoardConfig(40, 1)
      val pieces = List(Piece(1, PlayerColor.Blue, 44))
      val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
      val thisState = GameState(players)
      val thisController = Controller(thisState, config)
      val thisTui = Tui(thisController)
      val output = thisTui.processInput()
      //thisTui.update()
      // Alice ist Blau (B), Figur 1 sollte in Base sein: [B1]
      output should include(s"{${AnsiColor.BLUE}B1${AnsiColor.RESET}}")
    }


    "render a piece on the field correctly" in {
      // Manuelle Manipulation für den Test: Alice Figur 1 auf Feld 5
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
  }
}