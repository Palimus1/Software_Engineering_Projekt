package ludo.model

trait WinStrategy {
  def isWinner(player: Player, fieldSize: Int): Boolean
}

object StandardWinStrategy extends WinStrategy {
  override def isWinner(player: Player, fieldSize: Int): Boolean = {
    player.pieces.forall(p => p.position > fieldSize)
  }
}

object QuickWinStrategy extends WinStrategy {
  override def isWinner(player: Player, fieldSize: Int): Boolean = {
    player.pieces.exists(p => p.position > fieldSize)
  }
}