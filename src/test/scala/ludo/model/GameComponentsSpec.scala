package ludo.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameComponentsSpec extends AnyWordSpec with Matchers {
  "A GameState" when {

    "creating a state without declaring currPlayerIndex" should {
      "take the default value" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 0))
        val players = List(Player("Stella", PlayerColor.Blue, pieces, 0))
        val state = GameState(players)
        state.currentPlayerIndex should be(0)
      }
    }

    "created via the factory method create()" should {
      "use default names for empty or whitespace strings" in {
        val config = BoardConfig(40, 3)
        // Spieler 1 hat einen Namen, Spieler 2 ist leer, Spieler 3 hat nur Leerzeichen
        val names = List("Alice", "", "   ")
        val state = GameState.create(names, config)

        state.players(0).name should be("Alice")
        state.players(1).name should be("PC 2")
        state.players(2).name should be("PC 3")
      }

      "pad the list with default names if too few names are provided" in {
        val config = BoardConfig(40, 4)
        // Wir übergeben nur 2 Namen, wollen aber 4 Spieler
        val names = List("Alice", "Bob")
        val state = GameState.create(names, config)

        state.players.size should be(4)
        state.players(0).name should be("Alice")
        state.players(1).name should be("Bob")
        // Die fehlenden Spieler müssen die Defaults bekommen
        state.players(2).name should be("PC 3")
        state.players(3).name should be("PC 4")
      }
    }

  }
}