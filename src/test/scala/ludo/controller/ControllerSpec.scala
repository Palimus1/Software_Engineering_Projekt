import _root_.ludo.controller.ControllerInterface
import _root_.ludo.controller.impl.Controller
import _root_.ludo.fileio.FileIOInterface
import _root_.ludo.model.*
import _root_.ludo.model.memento.GameStateMemento
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.util.Success

class ControllerSpec extends AnyWordSpec with Matchers {

  private class MemoryFileIO(var loadedMemento: GameStateMemento) extends FileIOInterface {
    var savedMemento: Option[GameStateMemento] = None

    override def save(memento: GameStateMemento): Unit = {
      savedMemento = Some(memento)
    }

    override def load(): GameStateMemento = loadedMemento
  }

  private val config = BoardConfig(40, 2)
  private val initialState = GameState.create(List("Alice", "Bob"), config)

  "A Controller" when {

    "performing a move" should {
      "update the game state and grant another turn if a 6 is rolled" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position shouldBe 1
        controller.gameState.currentPlayerIndex shouldBe 0
      }

      "allow a move if the player has an active piece and the move is valid" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.diceRoll shouldBe Some(3)
        controller.gameState.lastError shouldBe Success(())
      }

      "allow a move if there are no active pieces but a target-area piece can move" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(2)

        controller.gameState.diceRoll shouldBe Some(2)
        controller.gameState.lastError shouldBe Success(())
      }

      "switch players if all active pieces are blocked" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 39), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.currentPlayerIndex shouldBe 1
        controller.gameState.message.get shouldBe a[AllPiecesBlockedEvent]
      }

      "update the game state for consecutive moves" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice(6)
        controller.doMove(1)
        controller.rollDice(6)
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position shouldBe 7
      }

      "return an error if an invalid piece id is chosen" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(5)

        controller.gameState.lastError.failed.get shouldBe a[InvalidPieceException]
      }

      "not move a piece out of base if no 6 is rolled" in {
        val state = initialState.copy(diceRoll = Some(3), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players(0).pieces(0).position shouldBe 0
        controller.gameState.lastError.failed.get shouldBe a[NeedSixException]
      }

      "force a player to move out of base if a 6 is rolled and the start field is clear" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.lastError.failed.get shouldBe a[BaseLeaveException]
      }

      "force a player to clear the start field if a 6 is rolled and start is blocked by own piece" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 5), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(3)

        controller.gameState.lastError.failed.get shouldBe a[BaseClearException]
      }

      "not move the piece if it would overshoot the target area" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(5), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position shouldBe 40
        controller.gameState.lastError.failed.get shouldBe a[OvershootException]
      }

      "not allow moving to a field occupied by an own piece" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 8), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(3), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.lastError.failed.get shouldBe a[BlockedException]
        controller.gameState.players.head.pieces.head.position shouldBe 5
      }

      "capture an enemy piece if landing on the same global position" in {
        val alicePieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val bobPieces = List(Piece(1, PlayerColor.Red, 25), Piece(2, PlayerColor.Red, 0), Piece(3, PlayerColor.Red, 0), Piece(4, PlayerColor.Red, 0))
        val players = List(Player("Alice", PlayerColor.Blue, alicePieces, 0), Player("Bob", PlayerColor.Red, bobPieces, 20))
        val state = GameState(players, config, diceRoll = Some(4), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players(0).pieces(0).position shouldBe 5
        controller.gameState.players(1).pieces(0).position shouldBe 0
      }

      "declare a winner if all pieces are in the home area" in {
        val winningPieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, winningPieces, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(4), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(4)

        controller.gameState.winner.map(_.name) shouldBe Some("Alice")
        controller.gameState.phase shouldBe GameOverPhase
      }

      "declare a winner instantly in Blitz mode when only one piece reaches the target" in {
        val configBlitz = BoardConfig(40, 2, QuickWinStrategy)
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, configBlitz, diceRoll = Some(4), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.winner.map(_.name) shouldBe Some("Alice")
        controller.gameState.phase shouldBe GameOverPhase
      }
    }

    "rolling the dice" should {
      "use a default random value when rollDice is called without arguments" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice()

        val stateChanged = controller.gameState.diceRoll.isDefined ||
          controller.gameState.rollAttempt > 0 ||
          controller.gameState.currentPlayerIndex != 0
        stateChanged shouldBe true
      }

      "increment roll attempts up to three if all pieces are in base and no 6 is rolled" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice(3)
        controller.gameState.rollAttempt shouldBe 1
        controller.gameState.currentPlayerIndex shouldBe 0

        controller.rollDice(4)
        controller.gameState.rollAttempt shouldBe 2
        controller.gameState.currentPlayerIndex shouldBe 0

        controller.rollDice(2)
        controller.gameState.rollAttempt shouldBe 0
        controller.gameState.currentPlayerIndex shouldBe 1
        controller.gameState.message.get shouldBe a[ThreeStrikesEvent]
      }

      "allow a move and reset attempts if a 6 is rolled while in base" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice(3)
        controller.rollDice(6)

        controller.gameState.rollAttempt shouldBe 0
        controller.gameState.diceRoll shouldBe Some(6)
        controller.gameState.phase shouldBe MovingPhase
      }
    }

    "handling invalid phase actions" should {
      "reject a move if the player has not rolled yet" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.doMove(1)

        controller.gameState.lastError.failed.get shouldBe a[MustRollFirstException]
      }

      "reject a dice roll if the player already rolled" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.rollDice(3)

        controller.gameState.lastError.failed.get shouldBe a[AlreadyRolledException]
      }

      "reject rolls and moves if the game is already over" in {
        val state = initialState.copy(phase = GameOverPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.rollDice(5)
        controller.gameState.lastError.failed.get shouldBe a[GameOverException]

        controller.doMove(1)
        controller.gameState.lastError.failed.get shouldBe a[GameOverException]
      }
    }

    "using undo and redo" should {
      "safely handle undo and redo when the history is empty" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.undo()
        controller.redo()

        controller.gameState shouldBe initialState
      }

      "undo and redo a dice roll" in {
        val controller: ControllerInterface = new Controller(initialState)
        val stateBefore = controller.gameState

        controller.rollDice(6)
        controller.gameState.diceRoll shouldBe Some(6)
        controller.gameState.phase shouldBe MovingPhase

        controller.undo()
        controller.gameState shouldBe stateBefore

        controller.redo()
        controller.gameState.diceRoll shouldBe Some(6)
        controller.gameState.phase shouldBe MovingPhase
      }

      "undo and redo a piece move" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)
        val stateBeforeMove = controller.gameState

        controller.doMove(1)
        controller.gameState.players(0).pieces(0).position shouldBe 1

        controller.undo()
        controller.gameState shouldBe stateBeforeMove
        controller.gameState.players(0).pieces(0).position shouldBe 0

        controller.redo()
        controller.gameState.players(0).pieces(0).position shouldBe 1
      }
    }

    "using FileIO" should {
      "save the current state as a memento" in {
        val fileIO = new MemoryFileIO(initialState.createMemento())
        val controller = new Controller(initialState, fileIO)

        controller.save()

        fileIO.savedMemento shouldBe Some(initialState.createMemento())
      }

      "load a state from a memento and clear the undo history" in {
        val loadedState = initialState.copy(currentPlayerIndex = 1, rollAttempt = 2)
        val fileIO = new MemoryFileIO(loadedState.createMemento())
        val controller = new Controller(initialState, fileIO)

        controller.rollDice(6)
        controller.load()
        val stateAfterLoad = controller.gameState
        controller.undo()

        stateAfterLoad shouldBe loadedState.copy(message = Some(InvalidRollRetryEvent(0, 1)))
        controller.gameState shouldBe stateAfterLoad
      }
    }
  }
}
