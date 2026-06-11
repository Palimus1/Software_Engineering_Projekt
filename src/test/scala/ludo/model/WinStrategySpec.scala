package ludo.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class WinStrategySpec extends AnyWordSpec with Matchers {
  "A WinStrategy" when {
    val fieldSize = 40

    "using StandardWinStrategy" should {
      "return false if not all pieces are in the goal" in {
        // 3 Figuren im Ziel, 1 Figur noch draußen
        val piecesNotWon = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 10))
        val player = Player("Alice", PlayerColor.Blue, piecesNotWon, 0)

        StandardWinStrategy.isWinner(player, fieldSize) should be(false)
      }

      "return true only if ALL pieces are beyond the field size" in {
        // Alle 4 Figuren im Ziel
        val piecesWon = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 44))
        val player = Player("Bob", PlayerColor.Blue, piecesWon, 0)

        StandardWinStrategy.isWinner(player, fieldSize) should be(true)
      }
    }

    "using QuickWinStrategy" should {
      "return false if no piece is in the goal" in {
        // Keine Figur im Ziel
        val piecesNotWon = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 2), Piece(3, PlayerColor.Blue, 3), Piece(4, PlayerColor.Blue, 4))
        val player = Player("Alice", PlayerColor.Blue, piecesNotWon, 0)

        QuickWinStrategy.isWinner(player, fieldSize) should be(false)
      }

      "return true if AT LEAST ONE piece is beyond the field size" in {
        // Genau 1 Figur im Ziel, der Rest noch auf dem Feld
        val piecesWon = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 2), Piece(3, PlayerColor.Blue, 3), Piece(4, PlayerColor.Blue, 4))
        val player = Player("Bob", PlayerColor.Blue, piecesWon, 0)

        QuickWinStrategy.isWinner(player, fieldSize) should be(true)
      }
    }
  }
}