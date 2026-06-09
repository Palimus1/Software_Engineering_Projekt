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
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be(1)
        controller.gameState.currentPlayerIndex should be(0)
      }

      "allow a move if the player has an active piece and the move is valid" in {
        // Alice hat eine Figur auf der 1. Wurf ist 3. (1 + 3 = 4 ist gültig)
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.rollDice(3)

        controller.gameState.diceRoll should be(Some(3))
        controller.gameState.errors should be("")
      }

      "allow a move if there are no active pieces, a non-6 is rolled, but a piece in the home area can still move" in {
        // Figur 1 ist auf 41 (Zielbereich), also ist hasActivePiece = false. Wurf = 2.
        // 41 + 2 = 43 (Gültiger Zug innerhalb des Zielbereichs!)
        val pieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.rollDice(2)

        controller.gameState.diceRoll should be(Some(2))
        controller.gameState.errors should be("")
      }

      "evaluate the isBlocked condition inside hasValidMoves" in {
        // Figur 1 auf 39, Wurf 3 -> Ziel 42.
        // Figur 2 auf 42 -> Blockiert Figur 1! Scoverage testet hier das '!isBlocked'.
        // Figur 2 Wurf 3 -> Ziel 45 -> Overshoot (relPos bleibt 42).
        // Figur 3 & 4 in Base (Wurf 3 -> ungültig).
        val pieces = List(Piece(1, PlayerColor.Blue, 39), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.rollDice(3)

        // Da absolut kein Zug gültig ist (Deadlock), ist direkt Bob dran
        controller.gameState.currentPlayerIndex should be(1)
        controller.gameState.errors should include("blockiert")
      }

      "update the game state for consecutive moves" in {
        val controller = Controller(initialState, config)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be(7)
      }

      "return an error if an invalid pieceId is chosen" in {
        val controller = Controller(initialState, config)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6))
        controller.doMove(5) // Figur 5 existiert nicht

        controller.gameState.errors should include("mit 1-4 indiziert")
      }

      "not move a piece out of base if not a 6 is rolled" in {
        val controller = Controller(initialState, config)
        controller.gameState = controller.gameState.copy(diceRoll = Some(3))
        controller.doMove(1)

        controller.gameState.players(0).pieces(0).position should be(0)
        controller.gameState.errors should include("Du brauchst eine 6")
      }

      "force a player to move out of base if a 6 is rolled and the start field is clear" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, diceRoll = Some(6))
        val controller = Controller(state, config)

        controller.doMove(1) // Versucht Figur auf Feld 5 zu bewegen, statt aus der Base

        controller.gameState.errors should include("Du musst eine Figur aus der Base bewegen")
      }

      "force a player to clear the start field if a 6 is rolled and start is blocked by own piece" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 5), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, diceRoll = Some(6))
        val controller = Controller(state, config)

        controller.doMove(3) // Versucht Figur auf Feld 5 zu bewegen, anstatt Startfeld freizuräumen

        controller.gameState.errors should include("Startfeld freiräumen")
      }

      "not move the piece if it would move the piece outside the board (overshoot)" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.gameState = controller.gameState.copy(diceRoll = Some(5))
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be (40)
        controller.gameState.errors should include("überschreitet das Ziel")
      }

      "not allow moving to a field occupied by own piece" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 8), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.gameState = controller.gameState.copy(diceRoll = Some(3))
        controller.doMove(1) // Figur 1 (Pos 5) + 3 = 8. Blockiert durch Figur 2.

        controller.gameState.errors should include("eigenen Figuren nicht schlagen")
        controller.gameState.players.head.pieces.head.position should be(5) // Darf sich nicht bewegt haben
      }

      "capture an enemy piece if landing on the same global position" in {
        // Alice Offset 0, Bob Offset 20
        val alicePieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        // Bob auf relativer Position 21 + Offset 20 = 41 -> Modulo 40 = 1 (Globale Position 1)
        val bobPieces = List(Piece(1, PlayerColor.Red, 21), Piece(2, PlayerColor.Red, 0), Piece(3, PlayerColor.Red, 0), Piece(4, PlayerColor.Red, 0))

        val players = List(Player("Alice", PlayerColor.Blue, alicePieces, 0), Player("Bob", PlayerColor.Red, bobPieces, 20))
        val controller = Controller(GameState(players, diceRoll = Some(4)), config)

        // Alice (auf Feld 1) zieht nicht, wir simulieren Bob, der Alice schlagen soll
        // Bob (Index 1) zieht Figur 1 (rel pos 21) um 20 Felder -> rel pos 41 gibt es nicht, sagen wir er zieht von 17 + 4 = 21.
        // Besser: Alice zieht auf Bobs Feld. Alice auf 1. Würfelt 20? Geht nicht.
        // Einfacher: Alice von 1 + 4 = 5. Bob steht auf global 5. Bob relPos = 25 (25+20 = 45 -> %40 = 5).
        val bobPiecesSetup2 = List(Piece(1, PlayerColor.Red, 25), Piece(2, PlayerColor.Red, 0), Piece(3, PlayerColor.Red, 0), Piece(4, PlayerColor.Red, 0))
        val playersSetup2 = List(Player("Alice", PlayerColor.Blue, alicePieces, 0), Player("Bob", PlayerColor.Red, bobPiecesSetup2, 20))
        val controller2 = Controller(GameState(playersSetup2, diceRoll = Some(4)), config)

        controller2.doMove(1) // Alice zieht von 1 + 4 = 5 (Global 5). Bob steht auf Global 5.

        controller2.gameState.players(0).pieces(0).position should be(5) // Alice gezogen
        controller2.gameState.players(1).pieces(0).position should be(0) // Bob geschlagen (zurück auf 0)
      }

      "declare a winner if all pieces are in the home area" in {
        val winningPieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, winningPieces, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.gameState = controller.gameState.copy(diceRoll = Some(4))
        controller.doMove(4) // Zieht Figur 4 von 40 auf 44

        controller.gameState.winner should include("gewonnen")
      }
    }

    "rolling the dice (rollDiceLogic)" should {

      "use the default random value when rollDice is called without arguments" in {
        val controller = Controller(initialState, config)
        controller.rollDice() // Aufruf komplett ohne Zahl!

        // Da es zufällig ist, prüfen wir nur, ob sich der Zustand geändert hat.
        val stateChanged = controller.gameState.diceRoll.isDefined ||
          controller.gameState.rollAttempt > 0 ||
          controller.gameState.currentPlayerIndex != 0
        stateChanged should be(true)
      }

      "increment roll attempts up to 3 if all pieces are in base and no 6 is rolled" in {
        val controller = Controller(initialState, config) // Alle Figuren in Base

        controller.rollDice(3)
        controller.gameState.rollAttempt should be(1)
        controller.gameState.currentPlayerIndex should be(0)

        controller.rollDice(4)
        controller.gameState.rollAttempt should be(2)
        controller.gameState.currentPlayerIndex should be(0)

        controller.rollDice(2) // 3. Versuch schlägt fehl
        controller.gameState.rollAttempt should be(0) // Reset
        controller.gameState.currentPlayerIndex should be(1) // Nächster Spieler ist dran
        controller.gameState.errors should include("Dreimal keinen Zug gehabt")
      }

      "allow a move and reset attempts if a 6 is rolled while in base" in {
        val controller = Controller(initialState, config)
        controller.rollDice(3) // 1 Fehlversuch
        controller.rollDice(6) // 6 gewürfelt

        controller.gameState.rollAttempt should be(0)
        controller.gameState.diceRoll should be(Some(6))
      }

      "switch to the next player if a deadlock occurs (pieces active but blocked)" in {
        // Alice hat nur eine Figur auf der 43 und würfelt eine 5 (Overshoot = Blockiert).
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players), config)

        controller.rollDice(5)

        controller.gameState.currentPlayerIndex should be(1) // Deadlock -> Bob ist dran
        controller.gameState.errors should include("alle Figuren sind blockiert")
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