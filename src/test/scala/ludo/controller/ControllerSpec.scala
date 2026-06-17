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
        val controller = Controller(initialState)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be(1)
        controller.gameState.currentPlayerIndex should be(0)
      }

      "allow a move if the player has an active piece and the move is valid" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.diceRoll should be(Some(3))
        controller.gameState.errors should be("")
      }

      "allow a move if there are no active pieces, a non-6 is rolled, but a piece in the home area can still move" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.rollDice(2)

        controller.gameState.diceRoll should be(Some(2))
        controller.gameState.errors should be("")
      }

      "evaluate the isBlocked condition inside hasValidMoves" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 39), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.currentPlayerIndex should be(1)
        controller.gameState.errors should include("blockiert")
      }

      "update the game state for consecutive moves" in {
        val controller = Controller(initialState)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(1)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be(7)
      }

      "return an error if an invalid pieceId is chosen" in {
        val controller = Controller(initialState)
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        controller.doMove(5)

        controller.gameState.errors should include("mit 1-4 indiziert")
      }

      "not move a piece out of base if not a 6 is rolled" in {
        val controller = Controller(initialState)
        controller.gameState = controller.gameState.copy(diceRoll = Some(3), phase = MovingPhase)
        controller.doMove(1)

        controller.gameState.players(0).pieces(0).position should be(0)
        controller.gameState.errors should include("Du brauchst eine 6")
      }

      "force a player to move out of base if a 6 is rolled and the start field is clear" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller = Controller(state)

        controller.doMove(1)

        controller.gameState.errors should include("Du musst eine Figur aus der Base bewegen")
      }

      "force a player to clear the start field if a 6 is rolled and start is blocked by own piece" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 5), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller = Controller(state)

        controller.doMove(3)

        controller.gameState.errors should include("Startfeld freiraeumen")
      }

      "not move the piece if it would move the piece outside the board (overshoot)" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.gameState = controller.gameState.copy(diceRoll = Some(5), phase = MovingPhase)
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position should be (40)
        controller.gameState.errors should include("ueberschreitet das Ziel")
      }

      "not allow moving to a field occupied by own piece" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 8), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.gameState = controller.gameState.copy(diceRoll = Some(3), phase = MovingPhase)
        controller.doMove(1)

        controller.gameState.errors should include("eigenen Figuren nicht schlagen")
        controller.gameState.players.head.pieces.head.position should be(5)
      }

      "capture an enemy piece if landing on the same global position" in {
        val alicePieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val bobPiecesSetup2 = List(Piece(1, PlayerColor.Red, 25), Piece(2, PlayerColor.Red, 0), Piece(3, PlayerColor.Red, 0), Piece(4, PlayerColor.Red, 0))
        val playersSetup2 = List(Player("Alice", PlayerColor.Blue, alicePieces, 0), Player("Bob", PlayerColor.Red, bobPiecesSetup2, 20))
        val controller2 = Controller(GameState(playersSetup2, config, diceRoll = Some(4), phase = MovingPhase))

        controller2.doMove(1)

        controller2.gameState.players(0).pieces(0).position should be(5)
        controller2.gameState.players(1).pieces(0).position should be(0)
      }

      "declare a winner if all pieces are in the home area" in {
        val winningPieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, winningPieces, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.gameState = controller.gameState.copy(diceRoll = Some(4), phase = MovingPhase)
        controller.doMove(4)

        controller.gameState.winner should include("gewonnen")
      }
      "declare a winner instantly in Blitz mode (QuickWinStrategy) when only one piece reaches the target" in {
        // Wir erstellen eine Config explizit für den Blitz-Modus!
        val configBlitz = BoardConfig(40, 2, QuickWinStrategy)

        // Alice hat nur EINE Figur kurz vorm Ziel (Feld 40), der Rest ist noch in der Base
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))

        // Spielstatus: Wir haben eine 4 gewürfelt und sind in der MovingPhase
        val state = GameState(players, configBlitz, diceRoll = Some(4), phase = MovingPhase)
        val controller = Controller(state)

        // Wir ziehen Figur 1 von Feld 40 auf Feld 44 (ins Ziel)
        controller.doMove(1)

        // Der Controller muss sofort den Gewinn auslösen, obwohl Figur 2, 3 und 4 noch in der Base sind!
        controller.gameState.winner should include("gewonnen")
        controller.gameState.phase should be(GameOverPhase)
      }

    }

    "rolling the dice (rollDiceLogic)" should {

      "use the default random value when rollDice is called without arguments" in {
        val controller = Controller(initialState)
        controller.rollDice()

        val stateChanged = controller.gameState.diceRoll.isDefined ||
          controller.gameState.rollAttempt > 0 ||
          controller.gameState.currentPlayerIndex != 0
        stateChanged should be(true)
      }

      "increment roll attempts up to 3 if all pieces are in base and no 6 is rolled" in {
        val controller = Controller(initialState)

        controller.rollDice(3)
        controller.gameState.rollAttempt should be(1)
        controller.gameState.currentPlayerIndex should be(0)

        controller.rollDice(4)
        controller.gameState.rollAttempt should be(2)
        controller.gameState.currentPlayerIndex should be(0)

        controller.rollDice(2)
        controller.gameState.rollAttempt should be(0)
        controller.gameState.currentPlayerIndex should be(1)
        controller.gameState.errors should include("Dreimal keinen Zug gehabt")
      }

      "allow a move and reset attempts if a 6 is rolled while in base" in {
        val controller = Controller(initialState)
        controller.rollDice(3)
        controller.rollDice(6)

        controller.gameState.rollAttempt should be(0)
        controller.gameState.diceRoll should be(Some(6))
      }

      "switch to the next player if a deadlock occurs (pieces active but blocked)" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller = Controller(GameState(players, config))

        controller.rollDice(5)

        controller.gameState.currentPlayerIndex should be(1)
        controller.gameState.errors should include("alle Figuren sind blockiert")
      }
    }

    "calculating global positions" should {
      val controller = Controller(initialState)

      "correctly calculate global position for player 2 with offset" in {
        val player2 = controller.gameState.players(1)
        val piece = Piece(1, PlayerColor.Red, 5)

        controller.gameState.getGlobalPosition(player2, piece) should be(Some(25))
      }

      "return None for pieces in base or home" in {
        val pBase = Piece(1, PlayerColor.Blue, 0)
        val pHome = Piece(1, PlayerColor.Blue, 41)
        val player = controller.gameState.players(0)

        controller.gameState.getGlobalPosition(player, pBase) should be(None)
        controller.gameState.getGlobalPosition(player, pHome) should be(None)
      }
    }
    "handling invalid phase actions (State Pattern limits)" should {
      val controller = Controller(initialState)

      "reject a move if the player hasn't rolled the dice yet (RollingPhase)" in {
        // Spiel startet automatisch in der RollingPhase.
        // Wir versuchen direkt zu ziehen, ohne vorher zu würfeln:
        controller.doMove(1)

        controller.gameState.errors should include("Du musst erst wuerfeln!")
      }

      "reject a dice roll if the player already rolled (MovingPhase)" in {
        // Wir schieben das Spiel künstlich in die MovingPhase
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)

        // Jetzt versuchen wir, NOCHMAL zu würfeln:
        controller.rollDice(3)

        controller.gameState.errors should include("Du hast schon gewuerfelt")
      }

      "reject any rolls or moves if the game is already over (GameOverPhase)" in {
        // Wir versetzen das Spiel direkt in den GameOver-Zustand!
        // So testen wir das Spielende, ohne spielen zu müssen.
        controller.gameState = controller.gameState.copy(phase = GameOverPhase)

        // 1. Versuch: Würfeln im beendeten Spiel
        controller.rollDice(5)
        controller.gameState.errors should include("bereits vorbei")

        // 2. Versuch: Ziehen im beendeten Spiel
        controller.doMove(1)
        controller.gameState.errors should include("bereits vorbei")
      }
    }
    "using the Undo/Redo Mechanism (Command Pattern)" should {

      "safely handle undo and redo when the history is empty" in {
        val controller = Controller(initialState)
        // Sollte einfach nichts tun und nicht abstürzen (Testet 'case Nil' im UndoManager)
        controller.undo()
        controller.redo()

        controller.gameState should be(initialState)
      }

      "undo and redo a dice roll (RollCommand)" in {
        val controller = Controller(initialState)
        val stateBefore = controller.gameState

        // 1. Wir würfeln (Speichert RollCommand im UndoManager)
        controller.rollDice(6)
        controller.gameState.diceRoll should be(Some(6))
        controller.gameState.phase should be(MovingPhase)

        // 2. Wir machen den Wurf rückgängig (Testet RollCommand.undoStep)
        controller.undo()
        controller.gameState should be(stateBefore) // Wieder in der RollingPhase, kein Würfel-Wert

        // 3. Wir stellen den Wurf wieder her (Testet RollCommand.redoStep)
        controller.redo()
        controller.gameState.diceRoll should be(Some(6))
        controller.gameState.phase should be(MovingPhase)
      }

      "undo and redo a piece move (MoveCommand)" in {
        val controller = Controller(initialState)
        // Wir bereiten das Spiel künstlich vor, damit wir sofort ziehen können
        controller.gameState = controller.gameState.copy(diceRoll = Some(6), phase = MovingPhase)
        val stateBeforeMove = controller.gameState

        // 1. Wir ziehen die Figur (Speichert MoveCommand im UndoManager)
        controller.doMove(1)
        controller.gameState.players(0).pieces(0).position should be(1) // Figur ist auf Feld 1

        // 2. Wir machen den Zug rückgängig (Testet MoveCommand.undoStep)
        controller.undo()
        controller.gameState should be(stateBeforeMove)
        controller.gameState.players(0).pieces(0).position should be(0) // Figur ist wieder in der Base

        // 3. Wir stellen den Zug wieder her (Testet MoveCommand.redoStep)
        controller.redo()
        controller.gameState.players(0).pieces(0).position should be(1) // Figur ist wieder auf Feld 1
      }
    }
  }

}