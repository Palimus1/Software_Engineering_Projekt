import _root_.ludo.model.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class WinStrategySpec extends AnyWordSpec with Matchers {

  "A WinStrategy" when {
    val fieldSize = 40

    "using StandardWinStrategy" should {
      "return false if not all pieces are in the goal" in {
        val player = Player(
          "Alice",
          PlayerColor.Blue,
          List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 10)),
          0
        )

        StandardWinStrategy.isWinner(player, fieldSize) shouldBe false
        StandardWinStrategy.name shouldBe "standard"
      }

      "return true only if all pieces are beyond the field size" in {
        val player = Player(
          "Bob",
          PlayerColor.Blue,
          List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 44)),
          0
        )

        StandardWinStrategy.isWinner(player, fieldSize) shouldBe true
      }
    }

    "using QuickWinStrategy" should {
      "return false if no piece is in the goal" in {
        val player = Player(
          "Alice",
          PlayerColor.Blue,
          List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 2), Piece(3, PlayerColor.Blue, 3), Piece(4, PlayerColor.Blue, 4)),
          0
        )

        QuickWinStrategy.isWinner(player, fieldSize) shouldBe false
        QuickWinStrategy.name shouldBe "quick"
      }

      "return true if at least one piece is beyond the field size" in {
        val player = Player(
          "Bob",
          PlayerColor.Blue,
          List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 2), Piece(3, PlayerColor.Blue, 3), Piece(4, PlayerColor.Blue, 4)),
          0
        )

        QuickWinStrategy.isWinner(player, fieldSize) shouldBe true
      }
    }

    "converted from strings" should {
      "return the matching strategy or fail for unknown values" in {
        WinStrategy.fromString("standard") shouldBe StandardWinStrategy
        WinStrategy.fromString("quick") shouldBe QuickWinStrategy
        an[IllegalArgumentException] should be thrownBy WinStrategy.fromString("unknown")
      }
    }
  }

  "PlayerColor" should {
    "convert strings to enum values or fail for unknown values" in {
      PlayerColor.fromString("Blue") shouldBe PlayerColor.Blue
      PlayerColor.fromString("Red") shouldBe PlayerColor.Red
      an[IllegalArgumentException] should be thrownBy PlayerColor.fromString("Purple")
    }
  }

  "GamePhase" should {
    "convert names to phases or fail for unknown values" in {
      GamePhase.fromName("rolling") shouldBe RollingPhase
      GamePhase.fromName("moving") shouldBe MovingPhase
      GamePhase.fromName("gameover") shouldBe GameOverPhase
      GamePhase.fromName("setup") shouldBe SetupPhase(SetupStep.NumPlayers)
      an[IllegalArgumentException] should be thrownBy GamePhase.fromName("invalid")
    }
  }
}
