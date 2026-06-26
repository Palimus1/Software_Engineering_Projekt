import _root_.ludo.aview.Tui
import _root_.ludo.controller.ControllerInterface
import _root_.ludo.controller.impl.Controller
import _root_.ludo.model.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.AnsiColor
import scala.util.{Failure, Success}

class TuiSpec extends AnyWordSpec with Matchers {

  private val config = BoardConfig(40, 2)
  private val state = GameState.create(List("Alice", "Bob"), config)

  private def render(testState: GameState): String = {
    val controller: ControllerInterface = new Controller(testState)
    Tui()(using controller).processInput()
  }

  "A TUI" should {

    "render the home base correctly" in {
      val output = render(state)

      output should include(s"[${AnsiColor.BLUE}B1${AnsiColor.RESET}]")
      output should include(s"[${AnsiColor.RED}R1${AnsiColor.RESET}]")
    }

    "render the target area correctly" in {
      val pieces = List(Piece(1, PlayerColor.Blue, 44))
      val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
      val targetState = GameState(players, BoardConfig(40, 1))

      val output = render(targetState)

      output should include(s"{${AnsiColor.BLUE}B1${AnsiColor.RESET}}")
    }

    "render a piece on the field correctly" in {
      val testState = state.copy(
        players = state.players.updated(0,
          state.players(0).copy(
            pieces = state.players(0).pieces.updated(0, Piece(1, PlayerColor.Blue, 5))
          )
        )
      )

      val output = render(testState)

      output should include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|")
    }

    "render the current dice roll" in {
      val output = render(state.copy(diceRoll = Some(6)))

      output should include("Du hast eine 6 gewuerfelt!")
    }

    "call update and print to console when the controller state changes" in {
      val stream = new java.io.ByteArrayOutputStream()
      val testState = state.copy(diceRoll = Some(6), phase = MovingPhase)
      val controller: ControllerInterface = new Controller(testState)
      val tui = Tui()(using controller)

      Console.withOut(stream) {
        controller.doMove(1)
      }

      val consoleOutput = stream.toString
      consoleOutput should include(s"|${AnsiColor.BLUE}B1${AnsiColor.RESET}|")
    }

    "translate all LudoExceptions into user-friendly colored strings" in {
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
        (new RuntimeException("Test Crash"), "Ein Fehler ist aufgetreten: Test Crash")
      )

      for ((exception, expectedText) <- errorCases) {
        val output = render(state.copy(lastError = Failure(exception)))

        output should include(expectedText)
        output should include(AnsiColor.RED)
      }

      val cleanOutput = render(state.copy(lastError = Success(())))
      cleanOutput should not include "Du brauchst eine 6"
      cleanOutput should not include "Ein Fehler ist aufgetreten"
    }

    "translate GameEvents into info messages" in {
      val events = List(
        (AllPiecesBlockedEvent(4), "Eine 4 gewuerfelt, aber alle Figuren sind blockiert! Naechster Spieler."),
        (InvalidRollRetryEvent(2, 1), "Eine 2 gewuerfelt! Kein gueltiger Zug. Du hast noch 1 Versuch(e) uebrig."),
        (ThreeStrikesEvent(5), "Eine 5 gewuerfelt. Dreimal keinen Zug gehabt. Naechster Spieler ist dran.")
      )

      for ((event, expectedText) <- events) {
        val output = render(state.copy(message = Some(event)))

        output should include(expectedText)
      }
    }

    "print a congratulatory message when a player wins" in {
      val winnerPlayer = state.players.head
      val output = render(state.copy(winner = Some(winnerPlayer)))

      output should include(s"Glueckwunsch! ${winnerPlayer.name}")
      output should include("hat das Spiel gewonnen!")
    }

    "print the interactive prompt at the very end of the output" in {
      val output = render(state)

      output should endWith("TUI-Eingabe ('w'=Wuerfeln, '1-4'=Ziehen, 'u'=Undo, 'r'=Redo, 's'=Save, 'l'=Load, 'q'=Quit): \n")
    }
  }
}
