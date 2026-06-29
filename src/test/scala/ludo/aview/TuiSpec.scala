import _root_.ludo.aview.Tui
import _root_.ludo.controller.ControllerInterface
import _root_.ludo.controller.impl.Controller
import _root_.ludo.fileio.FileIOInterface
import _root_.ludo.model.*
import _root_.ludo.model.memento.GameStateMemento
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.AnsiColor
import scala.util.{Failure, Success}

class TuiSpec extends AnyWordSpec with Matchers {

  private class MemoryFileIO(var loadedMemento: GameStateMemento) extends FileIOInterface {
    override def save(memento: GameStateMemento): Unit = loadedMemento = memento
    override def load(): GameStateMemento = loadedMemento
  }

  private val config = BoardConfig(40, 2)
  private val state = GameState.create(List("Alice", "Bob"), config)

  private def render(testState: GameState): String = {
    given FileIOInterface = new MemoryFileIO(testState.createMemento())
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

      output should include("You rolled a 6!")
    }

    "call update and print to console when the controller state changes" in {
      val stream = new java.io.ByteArrayOutputStream()
      val testState = state.copy(diceRoll = Some(6), phase = MovingPhase)
      given FileIOInterface = new MemoryFileIO(testState.createMemento())
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
        (NeedSixException(), "You need a 6 to leave the base!"),
        (BlockedException(), "You cannot capture your own pieces!"),
        (OvershootException(), "The move overshoots the target!"),
        (InvalidPieceException(), "Pieces are indexed 1-4! Please choose again."),
        (AlreadyRolledException(), "You have already rolled! Please move a piece."),
        (MustRollFirstException(), "You must roll first!"),
        (GameOverException(), "The game is already over!"),
        (BaseClearException(), "You must clear the start field!"),
        (BaseLeaveException(), "You must move a piece out of the base!"),
        (new RuntimeException("Test Crash"), "An error occurred: Test Crash")
      )

      for ((exception, expectedText) <- errorCases) {
        val output = render(state.copy(lastError = Failure(exception)))

        output should include(expectedText)
        output should include(AnsiColor.RED)
      }

      val cleanOutput = render(state.copy(lastError = Success(())))
      cleanOutput should not include "You need a 6"
      cleanOutput should not include "An error occurred"
    }

    "translate GameEvents into info messages" in {
      val events = List(
        (AllPiecesBlockedEvent(4), "Rolled a 4, but all pieces are blocked! Next player."),
        (InvalidRollRetryEvent(2, 1), "Rolled a 2! Not a valid move. You have 1 attempt(s) left."),
        (ThreeStrikesEvent(5), "Rolled a 5. No moves possible three times. Next player's turn.")
      )

      for ((event, expectedText) <- events) {
        val output = render(state.copy(message = Some(event)))

        output should include(expectedText)
      }
    }

    "print a congratulatory message when a player wins" in {
      val winnerPlayer = state.players.head
      val output = render(state.copy(winner = Some(winnerPlayer)))

      output should include(s"Congratulations! ${winnerPlayer.name}")
      output should include("has won the game!")
    }


    "render every setup step with configuration, prompts and setup errors" in {
      val setupStart = GameState.createSetup()
      val startOutput = render(setupStart)
      startOutput should include("=== LUDO SETUP ===")
      startOutput should include("Current configuration: [Nothing configured yet]")
      startOutput should include("Please specify the number of players (1-4):")

      val playerNamesState = setupStart.phase.handleSetup(setupStart, "2").get
      val playerNamesOutput = render(playerNamesState)
      playerNamesOutput should include("Current configuration: Number of players = 2, Names so far = []")
      playerNamesOutput should include("Please enter the name for player 1:")

      val secondPlayerNameState = playerNamesState.phase.handleSetup(playerNamesState, "Alice").get
      val secondPlayerNameOutput = render(secondPlayerNameState)
      secondPlayerNameOutput should include("Current configuration: Number of players = 2, Names so far = [Alice]")
      secondPlayerNameOutput should include("Please enter the name for player 2:")

      val fieldSizeState = secondPlayerNameState.phase.handleSetup(secondPlayerNameState, "Bob").get
      val fieldSizeOutput = render(fieldSizeState)
      fieldSizeOutput should include("Current configuration: Number of players = 2, Names = [Alice, Bob]")
      fieldSizeOutput should include("Please specify the board size (Default 40):")

      val gameModeState = fieldSizeState.phase.handleSetup(fieldSizeState, "40").get
      val gameModeOutput = render(gameModeState)
      gameModeOutput should include("Current configuration: Number of players = 2, Names = [Alice, Bob], Board size = 40")
      gameModeOutput should include("Select a game mode: Standard mode(ENTER)  ---  Blitz mode(Blitz):")

      val errorOutput = render(setupStart.copy(lastError = Failure(new RuntimeException("Invalid setup input"))))
      errorOutput should include("Error: Invalid setup input")
      errorOutput should include(AnsiColor.RED)
    }

    "print the interactive prompt at the very end of the output" in {
      val output = render(state)

      output should endWith("TUI-Input ('w'=Roll, '1-4'=Move, 'u'=Undo, 'r'=Redo, 's'=Save, 'l'=Load, 'q'=Quit): \n")
    }
  }
}
