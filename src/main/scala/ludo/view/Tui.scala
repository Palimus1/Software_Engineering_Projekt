package ludo.view

import ludo.model.*
import ludo.controller.Controller
import ludo.observer.Observer

import scala.io.AnsiColor

case class Tui(controller: Controller) extends Observer:

  controller.add(this) //Die Tui zum Observer des controller machen

  // Konzept für Brett: Hochzählen
  // 0 = Base, 1-40 = Weg, 41-44 = Ziel

  // 1. Der Seiteneffekt: Diese Methode wird vom Observer aufgerufen.
  // Sie ist schwer zu testen, enthält aber auch keine Logik mehr.
  def update(): Unit =
    print(processInput())

  // 2. Die Logik: Diese Methode gibt nur einen String zurück.
  // Du kannst sie in deinen Tests einfach aufrufen und prüfen:
  // tui.processInput() should include ("|B1|")
  def processInput(): String =
    renderAll()

  def renderAll(): String = {
    val state = controller.gameState
    val board = List(printHome(state), printField(state), printTarget(state))
    board.mkString("\n")
  }

  private def printHome(state: GameState): String = {
    // [_][_][_][_]

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
    //{ }{ }{ }{ }

    val config = controller.config
    val playerTargets = state.players.map { player =>


      val targetSlots = (1 to 4).map { slotId =>

        val targetPos = config.fieldSize + slotId
        val maybePiece = player.pieces.find(p => p.position == targetPos)

        maybePiece match {
          case Some(p) =>

            s"{${player.color.ansiCode}${player.color.toString.head}${p.id}${AnsiColor.RESET}}"
          case None =>

            "{  }"
        }
      }

      targetSlots.mkString("")
    }

    // Die Zielbereiche aller Spieler nebeneinander mit Abstand ausgeben
    playerTargets.mkString("   ")
  }

  private def printField(state: GameState): String =
    val config = controller.config
    val range = 1 until (config.fieldSize + 1)

    val occupiedFields = for {
      p <- state.players
      piece <- p.pieces
      globalPos <- controller.getGlobalPosition(p, piece)
    } yield globalPos -> (p, piece)
    /*
    yield gibt hier eine Liste mit geschachtelten Tupeln: (pos -> (player, piece))
                                              '->' ist schönere Schreibweise für Tupel*/
    val posMap = occupiedFields.toMap
    //erzeugt aus liste eine Map wo globalpos der key ist

    range.map { pos =>
      posMap.get(pos) match {
        case Some((player, piece)) =>
          // Zeigt anfangsbuchstabe der Farbe und piece-id (plus hat die Farbe)
          s"|${player.color.ansiCode}${player.color.toString.head}${piece.id}${AnsiColor.RESET}|"
        case None => "|__|"
      }
    }.mkString("")


