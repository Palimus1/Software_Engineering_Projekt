import ludo.aview.BoardLayoutCalculator

object DebugLayout extends App {
  val layout = new BoardLayoutCalculator(40)
  for (i <- 1 to 40) {
    val (c, r) = layout.calculateGridCoordinates(i)
    println(s"Field $i: ($c, $r)")
  }
  for (p <- Seq(0, 13, 26)) {
    val (bc, br) = layout.calculateAlignedBaseCoordinates(p, 0)
    val (nc, nr) = layout.calculateNameCoordinates(p)
    println(s"Player starting at $p: base(0)=($bc, $br), name=($nc, $nr)")
  }
}
