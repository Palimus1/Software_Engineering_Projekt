package ludo.aview

import ludo.model.*
import ludo.controller.ControllerInterface
import ludo.util.Observer

import scala.io.AnsiColor


case class Tui()(using controller: ControllerInterface) extends Observer:

  controller.add(this)

  def update(): Unit =
    print(processInput())

  def processInput(): String =
    renderAll()

  def renderAll(): String = {
    val state = controller.gameState

    val playerStr = s"\nAktueller Spieler: ${state.currentPlayer.name} (${state.currentPlayer.color})"
    val rollStr = state.diceRoll.map(roll => s"Du hast eine $roll gewuerfelt!").getOrElse("")

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

    val prompt = "TUI-Eingabe ('w'=Wuerfeln, '1-4'=Ziehen, 'u'=Undo, 'r'=Redo, 'q'=Quit): "

    board.filter(_.nonEmpty).mkString("\n") + "\n" + prompt + "\n"
  }

  private def winnerMessage(state: GameState): String = {
    state.winner match {
      case Some(player) =>
        s"\nGlueckwunsch! ${player.name} (${player.color.ansiCode}${player.color}${AnsiColor.RESET}) hat das Spiel gewonnen!"
      case None => ""
    }
  }

  private def infoMessage(state: GameState): String = {
    state.message match {
      case Some(AllPiecesBlockedEvent(roll)) =>
        s"Eine $roll gewuerfelt, aber alle Figuren sind blockiert! Naechster Spieler."
      case Some(InvalidRollRetryEvent(roll, attemptsLeft)) =>
        s"Eine $roll gewuerfelt! Kein gueltiger Zug. Du hast noch $attemptsLeft Versuch(e) uebrig."
      case Some(ThreeStrikesEvent(roll)) =>
        s"Eine $roll gewuerfelt. Dreimal keinen Zug gehabt. Naechster Spieler ist dran."
      case None => ""
    }
  }

  private def errorMessage(state: GameState): String = {
    state.lastError match {
      case Some(_: NeedSixException) => s"${AnsiColor.RED}Du brauchst eine 6, um die Base zu verlassen!${AnsiColor.RESET}"
      case Some(_: BlockedException) => s"${AnsiColor.RED}Du kannst deine eigenen Figuren nicht schlagen!${AnsiColor.RESET}"
      case Some(_: OvershootException) => s"${AnsiColor.RED}Der Zug ueberschreitet das Ziel!${AnsiColor.RESET}"
      case Some(_: InvalidPieceException) => s"${AnsiColor.RED}Die Figuren sind mit 1-4 indiziert! Bitte erneut waehlen.${AnsiColor.RESET}"
      case Some(_: AlreadyRolledException) => s"${AnsiColor.RED}Du hast schon gewuerfelt! Bitte bewege eine Figur.${AnsiColor.RESET}"
      case Some(_: MustRollFirstException) => s"${AnsiColor.RED}Du musst erst wuerfeln!${AnsiColor.RESET}"
      case Some(_: GameOverException) => s"${AnsiColor.RED}Das Spiel ist bereits vorbei!${AnsiColor.RESET}"
      case Some(_: BaseClearException) => s"${AnsiColor.RED}Du musst das Startfeld freiraeumen!${AnsiColor.RESET}"
      case Some(_: BaseLeaveException) => s"${AnsiColor.RED}Du musst eine Figur aus der Base bewegen!${AnsiColor.RESET}"
      case Some(e: Throwable) => s"${AnsiColor.RED}Ein Fehler ist aufgetreten: ${e.getMessage}${AnsiColor.RESET}"
      case None => ""
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