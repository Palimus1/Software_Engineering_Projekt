package ludo.aview

import ludo.model.*
import ludo.controller.ControllerInterface
import ludo.util.Observer

import scala.io.AnsiColor
import scala.util.{Success, Failure} // Neuer Import

case class Tui()(using controller: ControllerInterface) extends Observer:

  controller.add(this)

  def update(): Unit =
    print(processInput())

  def processInput(): String =
    renderAll()

  def renderAll(): String = {
    val state = controller.gameState

    state.phase match {
      case SetupPhase(step, data) =>
        val prompt = step match {
          case SetupStep.NumPlayers => "Please specify the number of players (1-4):"
          case SetupStep.PlayerNames => s"Please enter the name for player ${data.names.size + 1}:"
          case SetupStep.FieldSize => "Please specify the board size (Default 40):"
          case SetupStep.GameMode => "Select a game mode: Standard mode(ENTER)  ---  Blitz mode(Blitz):"
        }
        val configStr = step match {
          case SetupStep.NumPlayers => "Current configuration: [Nothing configured yet]"
          case SetupStep.PlayerNames => s"Current configuration: Number of players = ${data.numPlayers}, Names so far = [${data.names.mkString(", ")}]"
          case SetupStep.FieldSize => s"Current configuration: Number of players = ${data.numPlayers}, Names = [${data.names.mkString(", ")}]"
          case SetupStep.GameMode => s"Current configuration: Number of players = ${data.numPlayers}, Names = [${data.names.mkString(", ")}], Board size = ${data.fieldSize}"
        }
        val errorMsg = state.lastError match {
          case Failure(e) => s"${AnsiColor.RED}Error: ${e.getMessage}${AnsiColor.RESET}\n"
          case _ => ""
        }
        s"\n=== LUDO SETUP ===\n$configStr\n$errorMsg$prompt\n"

      case _ =>
        val playerStr = s"\nCurrent player: ${state.currentPlayer.name} (${state.currentPlayer.color})"
        val rollStr = state.diceRoll.map(roll => s"You rolled a $roll!").getOrElse("")

        val board = List(
          playerStr,
          rollStr,
          printHome(state),
          printField(state),
          printTarget(state),
          errorMessage(state),
          infoMessage(state),
          winnerMessage(state)
        )

        val prompt = "TUI-Input ('w'=Roll, '1-4'=Move, 'u'=Undo, 'r'=Redo, 's'=Save, 'l'=Load, 'q'=Quit): "

        board.filter(_.nonEmpty).mkString("\n") + "\n" + prompt + "\n"
    }
  }

  private def winnerMessage(state: GameState): String = {
    state.winner match {
      case Some(player) =>
        s"\nCongratulations! ${player.name} (${player.color.ansiCode}${player.color}${AnsiColor.RESET}) has won the game!"
      case None => ""
    }
  }

  private def infoMessage(state: GameState): String = {
    state.message match {
      case Some(AllPiecesBlockedEvent(roll)) =>
        s"Rolled a $roll, but all pieces are blocked! Next player."
      case Some(InvalidRollRetryEvent(roll, attemptsLeft)) =>
        s"Rolled a $roll! Not a valid move. You have $attemptsLeft attempt(s) left."
      case Some(ThreeStrikesEvent(roll)) =>
        s"Rolled a $roll. No moves possible three times. Next player's turn."
      case None => ""
    }
  }


  private def errorMessage(state: GameState): String = {
    state.lastError match {
      case Failure(_: NeedSixException) => s"${AnsiColor.RED}You need a 6 to leave the base!${AnsiColor.RESET}"
      case Failure(_: BlockedException) => s"${AnsiColor.RED}You cannot capture your own pieces!${AnsiColor.RESET}"
      case Failure(_: OvershootException) => s"${AnsiColor.RED}The move overshoots the target!${AnsiColor.RESET}"
      case Failure(_: InvalidPieceException) => s"${AnsiColor.RED}Pieces are indexed 1-4! Please choose again.${AnsiColor.RESET}"
      case Failure(_: AlreadyRolledException) => s"${AnsiColor.RED}You have already rolled! Please move a piece.${AnsiColor.RESET}"
      case Failure(_: MustRollFirstException) => s"${AnsiColor.RED}You must roll first!${AnsiColor.RESET}"
      case Failure(_: GameOverException) => s"${AnsiColor.RED}The game is already over!${AnsiColor.RESET}"
      case Failure(_: BaseClearException) => s"${AnsiColor.RED}You must clear the start field!${AnsiColor.RESET}"
      case Failure(_: BaseLeaveException) => s"${AnsiColor.RED}You must move a piece out of the base!${AnsiColor.RESET}"
      case Failure(e: Throwable) => s"${AnsiColor.RED}An error occurred: ${e.getMessage}${AnsiColor.RESET}"
      case Success(_) => "" // Der leere Success-Fall
    }
  }

  private def printHome(state: GameState): String = {
    val playerBases = state.players.map { player =>
      val slots = (1 to 4).map { slotId =>
        val maybePiece = player.pieces.find(p => p.id == slotId)
        maybePiece match {
          case Some(p) if p.position == 0 =>
            s"[${player.color.ansiCode}${player.color.toString.head}${p.id}${AnsiColor.RESET}]"
          case _ => "[__]"
        }
      }
      slots.mkString("")
    }
    playerBases.mkString("   ")
  }

  private def printTarget(state: GameState): String = {
    val config = controller.gameState.config
    val playerTargets = state.players.map { player =>
      val targetSlots = (1 to 4).map { slotId =>
        val targetPos = config.fieldSize + slotId
        val maybePiece = player.pieces.find(p => p.position == targetPos)
        maybePiece match {
          case Some(p) => s"{${player.color.ansiCode}${player.color.toString.head}${p.id}${AnsiColor.RESET}}"
          case None => "{  }"
        }
      }
      targetSlots.mkString("")
    }
    playerTargets.mkString("   ")
  }

  private def printField(state: GameState): String = {
    val config = controller.gameState.config
    val range = 1 until (config.fieldSize + 1)

    val occupiedFields = for {
      p <- state.players
      piece <- p.pieces
      globalPos <- state.getGlobalPosition(p, piece)
    } yield globalPos -> (p, piece)

    val posMap = occupiedFields.toMap

    range.map { pos =>
      posMap.get(pos) match {
        case Some((player, piece)) =>
          s"|${player.color.ansiCode}${player.color.toString.head}${piece.id}${AnsiColor.RESET}|"
        case None => "|__|"
      }
    }.mkString("")
  }