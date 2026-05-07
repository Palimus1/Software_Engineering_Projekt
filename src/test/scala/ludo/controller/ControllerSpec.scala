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

        controller.doMove(1, 6)

        controller.gameState.players.head.pieces.head.position should be(1)
        controller.gameState.currentPlayerIndex should be(1) // Jetzt ist Bob dran
      }

      "update the game state" in {
        val controller = Controller(initialState, config)

        controller.doMove(1, 6) //player 1
        controller.doMove(1, 6) //player 2
        controller.doMove(1, 6) //player 3
        controller.gameState.players.head.pieces.head.position should be(7)

      }

      "not move a piece out of base if not a 6 is rolled" in {
        val controller = Controller(initialState, config)
        controller.doMove(1, 3)

        controller.gameState.players(0).pieces(0).position should be(0)
        controller.gameState.currentPlayerIndex should be(1) // Spieler wechselt trotzdem
      }




      "not move the piece if it would move the piece outside the board" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 40))
        val pieces2 = List(Piece(1, PlayerColor.Red, 0))
        val players = List(Player("Stella", PlayerColor.Blue, pieces1, 0), Player("Ttella", PlayerColor.Red, pieces2, 0))
        val state = GameState(players)
        val controller = Controller(state, config)

        controller.doMove(1, 5)
        controller.gameState.players.head.pieces.head.position should be (40)

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