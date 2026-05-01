package ludo.controller

import ludo.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ControllerSpec extends AnyWordSpec with Matchers {
  "A Controller" when {
    val config = BoardConfig(40, 2)
    val initialState = GameState.create(List("Alice", "Bob"), config)

    "performing a move" should {
      "update the game state and switch the player" in {
        val controller = Controller(initialState, config)
        val firstPlayer = controller.gameState.currentPlayerIndex // 0

        // Alice (Blau) würfelt eine 6 und zieht Figur 1 aus der Base
        controller.doMove(1, 6)

        controller.gameState.players(0).pieces(0).position should be(1)
        controller.gameState.currentPlayerIndex should be(1) // Jetzt ist Bob dran
      }

      "not move a piece out of base if not a 6 is rolled" in {
        val controller = Controller(initialState, config)
        controller.doMove(1, 3)

        controller.gameState.players(0).pieces(0).position should be(0)
        controller.gameState.currentPlayerIndex should be(1) // Spieler wechselt trotzdem
      }
    }

    "calculating global positions" should {
      val controller = Controller(initialState, config)

      "correctly calculate global position for player 2 with offset" in {
        val player2 = controller.gameState.players(1) // Bob, Offset 20
        val piece = Piece(1, PlayerColor.Red, 5) // 5 Felder vom Start weg

        // 5 + 20 = 25
        controller.getGlobalPosition(player2, piece) should be(Some(25))
      }

      "return None for pieces in base or home" in {
        val pBase = Piece(1, PlayerColor.Blue, 0)
        val pHome = Piece(1, PlayerColor.Blue, 41)
        val player = controller.gameState.players(0)

        controller.getGlobalPosition(player, pBase) should be(None)
        controller.getGlobalPosition(player, pHome) should be(None)
      }
    }
  }
}