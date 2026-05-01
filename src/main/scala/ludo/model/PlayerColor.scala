package ludo.model

import scala.io.AnsiColor

enum PlayerColor(val ansiCode: String):
  case Blue extends PlayerColor(AnsiColor.BLUE)
  case Red extends PlayerColor(AnsiColor.RED)
  case Green extends PlayerColor(AnsiColor.GREEN)
  case Yellow extends PlayerColor(AnsiColor.YELLOW)
