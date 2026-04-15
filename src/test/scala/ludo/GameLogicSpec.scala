package ludo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import ludo.GameLogic._

class GameLogicSpec extends AnyWordSpec with Matchers {

  "A GameLogic" when {
    "moving a piece" should {
      "result in a new position" in {
        val p1 = Piece(1, PlayerColor.Blue, 1)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val newState = movePiece(state, "Stella", 1, 6)
        // Hier darf KEIN [cite...] stehen!
        newState.players.head.pieces.head.position should be(7)
      }
    }

    "calculating global position" should {
      "return None if the piece is in base" in {
        val p1 = Piece(1, PlayerColor.Blue, 0)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        getGlobalPosition(player, p1) should be(None)
      }
    }
  }
}