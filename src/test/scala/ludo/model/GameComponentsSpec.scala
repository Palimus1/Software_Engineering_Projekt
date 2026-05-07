package ludo.model

import ludo.model.*
import ludo.controller.Controller
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.io.AnsiColor

class GameComponentsSpec extends AnyWordSpec with Matchers {
  "A GameState" when {
    "creating a state without declaring currPlayerIndex" should {
      "take the default value" in{
        val pieces = List(Piece(1, PlayerColor.Blue, 0))
        val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
        val state = GameState(players)
        state.currentPlayerIndex should be(0)
      }
    }
  }


}
