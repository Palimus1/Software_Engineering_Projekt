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
      output.should(include(s"[${AnsiColor.BLUE}B1${AnsiColor.RESET}]"))
    }

    "render the target area correctly" in {
      val config = BoardConfig(40, 1)
      val pieces = List(Piece(1, PlayerColor.Blue, 44))
      val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
      val thisState = GameState(players, config)
      val thisController = Controller(thisState)
      val thisTui = Tui(thisController)
      val output = thisTui.processInput()
      output.should(include(s"{${AnsiColor.BLUE}B1${AnsiColor.RESET}}"))
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
      output.should(include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|"))
    }

    "call update() and print to console when the controller state changes" in {
      val stream = new java.io.ByteArrayOutputStream()

      Console.withOut(stream) {
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(1)
      }

      val consoleOutput = stream.toString
      consoleOutput.should(include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|"))
    }

    // --- NEUE TESTS FÜR TASK 8 ---

    "translate ALL LudoExceptions from the model into user-friendly colored strings" in {

      // Eine Liste aller möglichen Fehler und deren exakt erwarteter deutscher Text
      val errorCases = List(
        (NeedSixException(), "Du brauchst eine 6, um die Base zu verlassen!"),
        (BlockedException(), "Du kannst deine eigenen Figuren nicht schlagen!"),
        (OvershootException(), "Der Zug ueberschreitet das Ziel!"),
        (InvalidPieceException(), "Die Figuren sind mit 1-4 indiziert! Bitte erneut waehlen."),
        (AlreadyRolledException(), "Du hast schon gewuerfelt! Bitte bewege eine Figur."),
        (MustRollFirstException(), "Du musst erst wuerfeln!"),
        (GameOverException(), "Das Spiel ist bereits vorbei!"),
        (BaseClearException(), "Du musst das Startfeld freiraeumen!"),
        (BaseLeaveException(), "Du musst eine Figur aus der Base bewegen!"),
        (new RuntimeException("Test Crash"), "Ein Fehler ist aufgetreten: Test Crash") // Testet den Fallback (Throwable)
      )

      // Wir jagen jede Exception durch die selbe Test-Logik!
      for ((exception, expectedText) <- errorCases) {
        controller.gameState = controller.gameState.copy(lastError = Some(exception))
        val output = tui.processInput()

        output.should(include(expectedText))
        output.should(include(AnsiColor.RED)) // Alle Fehler müssen rot sein
      }

      // Testet den allerletzten Fall im Pattern Match: case None => ""
      controller.gameState = controller.gameState.copy(lastError = None)
      val cleanOutput = tui.processInput()
      cleanOutput.shouldNot(include(s"${AnsiColor.RED}❌")) // Ohne Fehler darf kein rotes X auftauchen
    }

    "print the interactive prompt at the very end of the output" in {
      val output = tui.processInput()
      // Prüft, ob der String genau mit dem Prompt aufhört,
      // damit der Cursor in der echten Konsole direkt dahinter wartet.
      output should endWith ("TUI-Eingabe ('w'=Wuerfeln, '1-4'=Ziehen, 'u'=Undo, 'r'=Redo, 'q'=Quit): ")
    }
  }
}