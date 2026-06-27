import _root_.ludo.model.*
import _root_.ludo.model.memento.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.util.{Failure, Success}

class GameComponentsSpec extends AnyWordSpec with Matchers {

  "A GameState" when {

    "created with default values" should {
      "use the expected defaults" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 0))
        val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
        val config = BoardConfig(40, 1)
        val state = GameState(players, config)

        state.currentPlayerIndex shouldBe 0
        state.lastError shouldBe Success(())
        state.message shouldBe None
        state.winner shouldBe None
        state.diceRoll shouldBe None
        state.rollAttempt shouldBe 0
        state.phase shouldBe RollingPhase
        state.currentPlayer shouldBe players.head
      }
    }

    "created via createSetup()" should {
      "start in SetupPhase(NumPlayers) with a dummy player" in {
        val state = GameState.createSetup()
        state.phase shouldBe a[SetupPhase]
        state.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.NumPlayers
        state.players.head.name shouldBe "Setup"
      }
    }

    "created via the factory method create()" should {
      "use default names for empty or whitespace strings" in {
        val config = BoardConfig(40, 3)
        val state = GameState.create(List("Alice", "", "   "), config)

        state.players.map(_.name) shouldBe List("Alice", "PC 2", "PC 3")
      }

      "pad the list with default names if too few names are provided" in {
        val config = BoardConfig(40, 4)
        val state = GameState.create(List("Alice", "Bob"), config)

        state.players.map(_.name) shouldBe List("Alice", "Bob", "PC 3", "PC 4")
      }

      "limit the list to the configured number of players and assign colors and offsets" in {
        val config = BoardConfig(40, 2)
        val state = GameState.create(List("A", "B", "C"), config)

        state.players.size shouldBe 2
        state.players.map(_.color) shouldBe List(PlayerColor.Blue, PlayerColor.Red)
        state.players.map(_.startOffset) shouldBe List(0, 20)
        state.players.foreach(_.pieces.map(_.position) shouldBe List(0, 0, 0, 0))
      }
    }

    "calculating global positions" should {
      "correctly calculate global positions with player offsets" in {
        val state = GameState.create(List("Alice", "Bob"), BoardConfig(40, 2))
        val player2 = state.players(1)

        state.getGlobalPosition(player2, Piece(1, PlayerColor.Red, 5)) shouldBe Some(25)
      }

      "return None for pieces in base or target area" in {
        val state = GameState.create(List("Alice"), BoardConfig(40, 1))
        val player = state.players.head

        state.getGlobalPosition(player, Piece(1, PlayerColor.Blue, 0)) shouldBe None
        state.getGlobalPosition(player, Piece(1, PlayerColor.Blue, 41)) shouldBe None
      }
    }

    "using the Memento Pattern" should {
      "create a memento from the current state" in {
        val config = BoardConfig(40, 2, QuickWinStrategy)
        val state = GameState.create(List("Alice", "Bob"), config).copy(
          currentPlayerIndex = 1,
          winner = Some(GameState.create(List("Alice", "Bob"), config).players.head),
          diceRoll = Some(6),
          rollAttempt = 2,
          phase = MovingPhase,
          lastError = Failure(NeedSixException()),
          message = Some(InvalidRollRetryEvent(2, 1))
        )

        val memento = state.createMemento()

        memento.players.map(_.name) shouldBe List("Alice", "Bob")
        memento.fieldSize shouldBe 40
        memento.numPlayers shouldBe 2
        memento.winStrategy shouldBe "quick"
        memento.currentPlayerIndex shouldBe 1
        memento.winnerColor shouldBe Some("Blue")
        memento.diceRoll shouldBe Some(6)
        memento.rollAttempt shouldBe 2
        memento.phase shouldBe "moving"
      }

      "restore a complete game state from a memento" in {
        val memento = GameStateMemento(
          players = List(
            PlayerMemento("Alice", "Blue", List(PieceMemento(1, "Blue", 1), PieceMemento(2, "Blue", 0)), 0),
            PlayerMemento("Bob", "Red", List(PieceMemento(1, "Red", 10)), 20)
          ),
          fieldSize = 40,
          numPlayers = 2,
          winStrategy = "standard",
          currentPlayerIndex = 1,
          winnerColor = Some("Red"),
          diceRoll = Some(5),
          rollAttempt = 1,
          phase = "rolling"
        )

        val state = GameState.fromMemento(memento)

        state.players(0).name shouldBe "Alice"
        state.players(0).color shouldBe PlayerColor.Blue
        state.players(0).pieces.head shouldBe Piece(1, PlayerColor.Blue, 1)
        state.config shouldBe BoardConfig(40, 2, StandardWinStrategy)
        state.currentPlayerIndex shouldBe 1
        state.winner.map(_.name) shouldBe Some("Bob")
        state.diceRoll shouldBe Some(5)
        state.rollAttempt shouldBe 1
        state.phase shouldBe RollingPhase
        state.message shouldBe Some(InvalidRollRetryEvent(0, 2))
        state.lastError shouldBe Success(())
      }

      "restore a state without winner, dice roll or generated message" in {
        val memento = GameStateMemento(
          players = List(PlayerMemento("Alice", "Blue", List(PieceMemento(1, "Blue", 0)), 0)),
          fieldSize = 40,
          numPlayers = 1,
          winStrategy = "quick",
          currentPlayerIndex = 0,
          winnerColor = None,
          diceRoll = None,
          rollAttempt = 0,
          phase = "moving"
        )

        val state = GameState.fromMemento(memento)

        state.winner shouldBe None
        state.diceRoll shouldBe None
        state.message shouldBe None
        state.config.winStrategy shouldBe QuickWinStrategy
        state.phase shouldBe MovingPhase
      }
    }
  }
}
