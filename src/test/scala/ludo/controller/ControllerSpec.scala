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
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position.shouldBe(1)
        controller.gameState.currentPlayerIndex.shouldBe(0)
      }

      "allow a move if the player has an active piece and the move is valid" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.diceRoll.shouldBe(Some(3))
        controller.gameState.lastError.shouldBe(None)
      }

      "allow a move if there are no active pieces, a non-6 is rolled, but a piece in the home area can still move" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(2)

        controller.gameState.diceRoll.shouldBe(Some(2))
        controller.gameState.lastError.shouldBe(None)
      }

      "evaluate the isBlocked condition inside hasValidMoves" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 39), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(3)

        controller.gameState.currentPlayerIndex.shouldBe(1)
        controller.gameState.message.get.shouldBe(a[AllPiecesBlockedEvent])
      }

      "update the game state for consecutive moves" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice(6)
        controller.doMove(1)
        controller.rollDice(6)
        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position.shouldBe(7)
      }

      "return an error if an invalid pieceId is chosen" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(5)

        controller.gameState.lastError.get.shouldBe(a[InvalidPieceException])
      }

      "not move a piece out of base if not a 6 is rolled" in {
        val state = initialState.copy(diceRoll = Some(3), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players(0).pieces(0).position.shouldBe(0)
        controller.gameState.lastError.get.shouldBe(a[NeedSixException])
      }

      "force a player to move out of base if a 6 is rolled and the start field is clear" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.lastError.get.shouldBe(a[BaseLeaveException])
      }

      "force a player to clear the start field if a 6 is rolled and start is blocked by own piece" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 5), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, config, diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(3)

        controller.gameState.lastError.get.shouldBe(a[BaseClearException])
      }

      "not move the piece if it would move the piece outside the board (overshoot)" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(5), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.players.head.pieces.head.position.shouldBe(40)
        controller.gameState.lastError.get.shouldBe(a[OvershootException])
      }

      "not allow moving to a field occupied by own piece" in {
        val pieces1 = List(Piece(1, PlayerColor.Blue, 5), Piece(2, PlayerColor.Blue, 8), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces1, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(3), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        controller.gameState.lastError.get.shouldBe(a[BlockedException])
        controller.gameState.players.head.pieces.head.position.shouldBe(5)
      }

      "capture an enemy piece if landing on the same global position" in {
        val alicePieces = List(Piece(1, PlayerColor.Blue, 1), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val bobPiecesSetup2 = List(Piece(1, PlayerColor.Red, 25), Piece(2, PlayerColor.Red, 0), Piece(3, PlayerColor.Red, 0), Piece(4, PlayerColor.Red, 0))
        val playersSetup2 = List(Player("Alice", PlayerColor.Blue, alicePieces, 0), Player("Bob", PlayerColor.Red, bobPiecesSetup2, 20))
        val state = GameState(playersSetup2, config, diceRoll = Some(4), phase = MovingPhase)
        val controller2: ControllerInterface = new Controller(state)

        controller2.doMove(1)

        controller2.gameState.players(0).pieces(0).position.shouldBe(5)
        controller2.gameState.players(1).pieces(0).position.shouldBe(0)
      }

      "declare a winner if all pieces are in the home area" in {
        val winningPieces = List(Piece(1, PlayerColor.Blue, 41), Piece(2, PlayerColor.Blue, 42), Piece(3, PlayerColor.Blue, 43), Piece(4, PlayerColor.Blue, 40))
        val players = List(Player("Alice", PlayerColor.Blue, winningPieces, 0), initialState.players(1))
        val state = GameState(players, config).copy(diceRoll = Some(4), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(4)

        // HIER GEÄNDERT: Prüfen auf Option[Player] anstatt String
        controller.gameState.winner.isDefined.shouldBe(true)
        controller.gameState.winner.get.name.shouldBe("Alice")
      }

      "declare a winner instantly in Blitz mode (QuickWinStrategy) when only one piece reaches the target" in {
        val configBlitz = BoardConfig(40, 2, QuickWinStrategy)
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val state = GameState(players, configBlitz, diceRoll = Some(4), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.doMove(1)

        // HIER GEÄNDERT: Prüfen auf Option[Player] anstatt String
        controller.gameState.winner.isDefined.shouldBe(true)
        controller.gameState.winner.get.name.shouldBe("Alice")
        controller.gameState.phase.shouldBe(GameOverPhase)
      }

    }

    "rolling the dice (rollDiceLogic)" should {

      "use the default random value when rollDice is called without arguments" in {
        val controller: ControllerInterface = new Controller(initialState)
        controller.rollDice()

        val stateChanged = controller.gameState.diceRoll.isDefined ||
          controller.gameState.rollAttempt > 0 ||
          controller.gameState.currentPlayerIndex != 0
        stateChanged.shouldBe(true)
      }

      "increment roll attempts up to 3 if all pieces are in base and no 6 is rolled" in {
        val controller: ControllerInterface = new Controller(initialState)

        controller.rollDice(3)
        controller.gameState.rollAttempt.shouldBe(1)
        controller.gameState.currentPlayerIndex.shouldBe(0)

        controller.rollDice(4)
        controller.gameState.rollAttempt.shouldBe(2)
        controller.gameState.currentPlayerIndex.shouldBe(0)

        controller.rollDice(2)
        controller.gameState.rollAttempt.shouldBe(0)
        controller.gameState.currentPlayerIndex.shouldBe(1)
        controller.gameState.message.get.shouldBe(a[ThreeStrikesEvent])
      }

      "allow a move and reset attempts if a 6 is rolled while in base" in {
        val controller: ControllerInterface = new Controller(initialState)
        controller.rollDice(3)
        controller.rollDice(6)

        controller.gameState.rollAttempt.shouldBe(0)
        controller.gameState.diceRoll.shouldBe(Some(6))
      }

      "switch to the next player if a deadlock occurs (pieces active but blocked)" in {
        val pieces = List(Piece(1, PlayerColor.Blue, 40), Piece(2, PlayerColor.Blue, 0), Piece(3, PlayerColor.Blue, 0), Piece(4, PlayerColor.Blue, 0))
        val players = List(Player("Alice", PlayerColor.Blue, pieces, 0), initialState.players(1))
        val controller: ControllerInterface = new Controller(GameState(players, config))

        controller.rollDice(5)

        controller.gameState.currentPlayerIndex.shouldBe(1)
        controller.gameState.message.get.shouldBe(a[AllPiecesBlockedEvent])
      }
    }

    "calculating global positions" should {
      val controller: ControllerInterface = new Controller(initialState)

      "correctly calculate global position for player 2 with offset" in {
        val player2 = controller.gameState.players(1)
        val piece = Piece(1, PlayerColor.Red, 5)

        controller.gameState.getGlobalPosition(player2, piece).shouldBe(Some(25))
      }

      "return None for pieces in base or home" in {
        val pBase = Piece(1, PlayerColor.Blue, 0)
        val pHome = Piece(1, PlayerColor.Blue, 41)
        val player = controller.gameState.players(0)

        controller.gameState.getGlobalPosition(player, pBase).shouldBe(None)
        controller.gameState.getGlobalPosition(player, pHome).shouldBe(None)
      }
    }

    "handling invalid phase actions (State Pattern limits)" should {

      "reject a move if the player hasn't rolled the dice yet (RollingPhase)" in {
        val controller: ControllerInterface = new Controller(initialState)
        controller.doMove(1)
        controller.gameState.lastError.get.shouldBe(a[MustRollFirstException])
      }

      "reject a dice roll if the player already rolled (MovingPhase)" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.rollDice(3)
        controller.gameState.lastError.get.shouldBe(a[AlreadyRolledException])
      }

      "reject any rolls or moves if the game is already over (GameOverPhase)" in {
        val state = initialState.copy(phase = GameOverPhase)
        val controller: ControllerInterface = new Controller(state)

        controller.rollDice(5)
        controller.gameState.lastError.get.shouldBe(a[GameOverException])

        controller.doMove(1)
        controller.gameState.lastError.get.shouldBe(a[GameOverException])
      }
    }

    "using the Undo/Redo Mechanism (Command Pattern)" should {

      "safely handle undo and redo when the history is empty" in {
        val controller: ControllerInterface = new Controller(initialState)
        controller.undo()
        controller.redo()

        controller.gameState.shouldBe(initialState)
      }

      "undo and redo a dice roll (RollCommand)" in {
        val controller: ControllerInterface = new Controller(initialState)
        val stateBefore = controller.gameState

        controller.rollDice(6)
        controller.gameState.diceRoll.shouldBe(Some(6))
        controller.gameState.phase.shouldBe(MovingPhase)

        controller.undo()
        controller.gameState.shouldBe(stateBefore)

        controller.redo()
        controller.gameState.diceRoll.shouldBe(Some(6))
        controller.gameState.phase.shouldBe(MovingPhase)
      }

      "undo and redo a piece move (MoveCommand)" in {
        val state = initialState.copy(diceRoll = Some(6), phase = MovingPhase)
        val controller: ControllerInterface = new Controller(state)
        val stateBeforeMove = controller.gameState

        controller.doMove(1)
        controller.gameState.players(0).pieces(0).position.shouldBe(1)

        controller.undo()
        controller.gameState.shouldBe(stateBeforeMove)
        controller.gameState.players(0).pieces(0).position.shouldBe(0)

        controller.redo()
        controller.gameState.players(0).pieces(0).position.shouldBe(1)
      }
    }
  }
}