package ludo.model

trait WinStrategy {
  def isWinner(player: Player, fieldSize: Int): Boolean
  def name: String
}

object WinStrategy {
  def fromString(name: String): WinStrategy = name match {
    case "standard" => StandardWinStrategy
    case "quick"    => QuickWinStrategy
    case other      => throw new IllegalArgumentException(s"Unknown win strategy: $other")
  }
}

object StandardWinStrategy extends WinStrategy {
  override def isWinner(player: Player, fieldSize: Int): Boolean = {
    player.pieces.forall(p => p.position > fieldSize)
  }
  override def name: String = "standard"
}

object QuickWinStrategy extends WinStrategy {
  override def isWinner(player: Player, fieldSize: Int): Boolean = {
    player.pieces.exists(p => p.position > fieldSize)
  }
  override def name: String = "quick"
}