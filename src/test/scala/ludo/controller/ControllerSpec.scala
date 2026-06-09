package ludo.controller

import ludo.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ControllerSpec extends AnyWordSpec with Matchers {
  "A Controller" when {
    val config = BoardConfig(40, 2)
    val initialState = GameState.create(List("Alice", "Bob"), config)

    "performing a move" should {
      "update the game state and grant another turn if a 6 is rolled" in {
        val controller = Controller(initialState, config)

        // Wir schmuggeln die 6 in den State
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)

        // Figur 1 sollte aus der Base auf Feld 1 gezogen sein
        controller.gameState.players.head.pieces.head.position should be(1)
        // Da eine 6 gewürfelt wurde, darf Alice (Index 0) NOCHMAL ziehen!
        controller.gameState.currentPlayerIndex should be(0)
      }

      "update the game state for consecutive moves" in {
        val controller = Controller(initialState, config)

        // Zug 1: Alice würfelt eine 6 und parkt aus (Pos 0 -> 1)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)

        // Zug 2: Alice darf wegen der 6 nochmal, würfelt wieder 6 (Pos 1 -> 7)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)

        // Überprüfen, ob Alice's Figur wirklich auf der 7 gelandet ist
        controller.gameState.players.head.pieces.head.position should be(7)
      }

      "not move a piece out of base if not a 6 is rolled" in {
        val controller = Controller(initialState, config)

        // Wir schmuggeln eine 3 in den State
        controller.gameState = controller.gameState.copy(diceRoll = Some(3))
        controller.doMove(1)

        // Figur bleibt in der Base (0)
        controller.gameState.players(0).pieces(0).position should be(0)
        // Da keine 6 gewürfelt wurde und der Zug verfällt/ungültig war, 
        // wird (je nach deiner Fehler-Logik) oft zum nächsten Spieler gewechselt, 
        // aber die Figur bewegt sich auf keinen Fall.
      }

      "not move the piece if it would move the piece outside the board" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 40))
        val pieces2 = List(Piece(1, PlayerColor.Red, 0))
        val players = List(Player("Stella", PlayerColor.Blue, pieces1, 0), Player("Ttella", PlayerColor.Red, pieces2, 0))
        val state = GameState(players)
        val controller = Controller(state, config)

        // Blaue Figur steht auf 40. Wir würfeln eine 5 (überschießt 44)
        controller.gameState = controller.gameState.copy(diceRoll = Some(5))
        controller.doMove(1)

        // Figur darf sich nicht bewegt haben und muss auf 40 bleiben
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