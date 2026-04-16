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
        newState.players.head.pieces.head.position should be(7)
      }
    }
    "moving a piece out of base" should {
      "result in no movement" in {
        val p1 = Piece(1, PlayerColor.Blue, 0)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val newState = movePiece(state, "Stella", 1, 6)
        newState.players.head.pieces.head.position should be(0)
      }
    }
    "moving a piece beyond the board" should {
      "result in no movement" in {
        val p1 = Piece(1, PlayerColor.Blue, 39)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val newState = movePiece(state, "Stella", 1, 6)
        newState.players.head.pieces.head.position should be(39)
      }
    }
    "moving a piece on last field" should {
      "result in no movement" in {
        val p1 = Piece(1, PlayerColor.Blue, 44)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val newState = movePiece(state, "Stella", 1, 6)
        newState.players.head.pieces.head.position should be(44)
      }
    }
    "calculating global position" should {
      "return None if the piece is in base" in {
        val p1 = Piece(1, PlayerColor.Blue, 0)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        getGlobalPosition(player, p1) should be(None)
      }
    }
    "player1 piece" should {
      "be on last field in String when in pos 40" in {
        val p1 = Piece(1, PlayerColor.Blue, 40)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))
        val board = Board(40)
        val result = "|__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||B1|"

        board.display(List(player)) should be(result)
      }
    }
    "player2 piece" should {
      "be on field 10 in String when in pos 40" in {
        val p1 = Piece(1, PlayerColor.Blue, 40)
        val player = Player("Ttella", PlayerColor.Blue, List(p1), 10)
        val state = GameState(List(player))
        val board = Board(40)
        val result = "|__||__||__||__||__||__||__||__||__||B1||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__|"

        board.display(List(player)) should be(result)
      }
    }
    "player3 piece" should {
      "be on field 20 in String when in pos 40" in {
        val p1 = Piece(1, PlayerColor.Blue, 40)
        val player = Player("Ttella", PlayerColor.Blue, List(p1), 20)
        val state = GameState(List(player))
        val board = Board(40)
        val result = "|__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||B1||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__|"

        board.display(List(player)) should be(result)
      }
    }
    "player4 piece" should {
      "be on field 30 in String when in pos 40" in {
        val p1 = Piece(1, PlayerColor.Blue, 40)
        val player = Player("Ttella", PlayerColor.Blue, List(p1), 30)
        val state = GameState(List(player))
        val board = Board(40)
        val result = "|__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||__||B1||__||__||__||__||__||__||__||__||__||__|"

        board.display(List(player)) should be(result)
      }
    }
  }
}