package ludo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.io.AnsiColor

class GameLogicSpec extends AnyWordSpec with Matchers {

  "A GameLogic" when {
    val config = BoardConfig(40, 4)
    val rules = GameLogic(config)
    val renderer = BoardRenderer(config)

    "moving a piece" should {
      "result in a new position" in {
        val playerNames = List("Stella", "Ttella", "Utella", "Vtella")
        val initialState = GameState.create(playerNames, config)

        // Piece 1 von Stella (Blau) startet in Base (0). Mit 6 auf Feld 1, dann +3 auf Feld 4.
        val nextState = rules.movePiece(initialState, "Stella", 1, 6)
        val finalState = rules.movePiece(nextState, "Stella", 1, 3)

        finalState.players.head.pieces.head.position should be(4)
      }

      "not change the other pieces" in {
        val p1 = Piece(1, PlayerColor.Blue, 1)
        val p2 = Piece(2, PlayerColor.Blue, 15)
        val player = Player("Stella", PlayerColor.Blue, List(p1, p2), 0)
        val state = GameState(List(player))

        val newState = rules.movePiece(state, "Stella", 1, 6)
        newState.players.head.pieces(1).position should be(15)
      }

      "not change the other players" in {
        val p1 = Piece(1, PlayerColor.Blue, 1)
        val player1 = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val player2 = Player("Ttella", PlayerColor.Red, List(Piece(1, PlayerColor.Red, 5)), 10)
        val state = GameState(List(player1, player2))

        val newState = rules.movePiece(state, "Stella", 1, 6)
        newState.players(1) should be(player2)
      }
    }

    "handling base and board limits" should {
      "result in movement out of base only with a 6" in {
        val p1 = Piece(1, PlayerColor.Blue, 0)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        // Mit einer 5 passiert nichts
        val stateNoMove = rules.movePiece(state, "Stella", 1, 5)
        stateNoMove.players.head.pieces.head.position should be(0)

        // Mit einer 6 rückt es auf Feld 1
        val stateMove = rules.movePiece(state, "Stella", 1, 6)
        stateMove.players.head.pieces.head.position should be(1)
      }

      "result in no movement if target position is beyond home (max 44)" in {
        val p1 = Piece(1, PlayerColor.Blue, 43)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        // 43 + 2 wäre 45 -> illegal, da maxPosition 44 ist
        val newState = rules.movePiece(state, "Stella", 1, 2)
        newState.players.head.pieces.head.position should be(43)
      }
    }

    "calculating global position" should {
      val player = Player("Stella", PlayerColor.Blue, List(), 0)

      "return None if the piece is in base (0)" in {
        val p = Piece(1, PlayerColor.Blue, 0)
        rules.getGlobalPosition(player, p) should be(None)
      }

      "return None if the piece is in the target house (> 40)" in {
        val p = Piece(1, PlayerColor.Blue, 41)
        rules.getGlobalPosition(player, p) should be(None)
      }

      "return correct global position with offset" in {
        val playerWithOffset = Player("Ttella", PlayerColor.Red, List(), 10)
        val p = Piece(1, PlayerColor.Red, 1) // Erstes Feld für Spieler 2

        // Position 1 + Offset 10 = Globales Feld 11
        rules.getGlobalPosition(playerWithOffset, p) should be(Some(11))
      }
    }

    "rendering the board" should {
      "correctly represent a piece on the field" in {
        val p1 = Piece(1, PlayerColor.Blue, 40)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val rendered = renderer.renderAll(state, rules)
        // Prüfen, ob der String die formatierte Figur an der richtigen Stelle enthält
        rendered should include(s"${PlayerColor.Blue.ansiCode}B1${AnsiColor.RESET}")
      }
    }

    "rendering the board" should {
      // ... dein vorhandener Test für das Feld ...

      "correctly represent a piece in the home base" in {
        // Ein Piece auf Position 0 (Home)
        val p1 = Piece(1, PlayerColor.Blue, 0)
        val player = Player("Stella", PlayerColor.Blue, List(p1), 0)
        val state = GameState(List(player))

        val rendered = renderer.renderAll(state, rules)

        // Prüfen, ob die Figur in eckigen Klammern erscheint [B1]
        rendered should include(s"[${PlayerColor.Blue.ansiCode}B1${AnsiColor.RESET}]")
      }

      "correctly represent a piece in the target house" in {
        // Ein Piece auf Position 41 (erstes Feld im Ziel bei fieldSize 40)
        val targetPos = config.fieldSize + 1
        val p1 = Piece(1, PlayerColor.Red, targetPos)
        val player = Player("Ttella", PlayerColor.Red, List(p1), 10)
        val state = GameState(List(player))

        val rendered = renderer.renderAll(state, rules)

        // Prüfen, ob die Figur in geschweiften Klammern erscheint {R1}
        rendered should include(s"{${PlayerColor.Red.ansiCode}R1${AnsiColor.RESET}}")
      }
    }
  }
}