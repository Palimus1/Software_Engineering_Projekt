package ludo.aview

class BoardLayoutCalculator(val totalFields: Int) {

  val inward: Boolean = totalFields >= 36
  val offset: Int = if (inward) 0 else 4

  private val w = Math.ceil(totalFields / 4.0).toInt
  private val h = Math.round((totalFields - 2 * w) / 2.0).toInt.max(1)
  private val s0 = w
  private val s1 = h
  private val s2 = w

  private val shiftAmount = (s0 / 2) + 1 + (totalFields / 2)

  private def getRawCoordinates(zeroBasedIndex: Int): (Int, Int) = {
    if (zeroBasedIndex < s0) {
      (zeroBasedIndex, 0)
    } else if (zeroBasedIndex < s0 + s1) {
      (w, zeroBasedIndex - s0)
    } else if (zeroBasedIndex < s0 + s1 + s2) {
      (w - (zeroBasedIndex - s0 - s1), h)
    } else {
      (0, h - (zeroBasedIndex - s0 - s1 - s2))
    }
  }

  def calculateGridCoordinates(fieldIndex: Int): (Int, Int) = {
    val zeroBasedIndex = fieldIndex - 1
    val shiftedIndex = (zeroBasedIndex + shiftAmount) % totalFields
    val (c, r) = getRawCoordinates(shiftedIndex)
    (c + offset, r + offset)
  }

  def calculateAlignedBaseCoordinates(startOffset: Int, slotIndex: Int): (Int, Int) = {
    val entryZeroBased = if (startOffset == 0) totalFields - 1 else startOffset - 1
    val shiftedEntry = (entryZeroBased + shiftAmount) % totalFields
    val (ex, ey) = getRawCoordinates(shiftedEntry)

    val side = if (shiftedEntry < s0) 0
               else if (shiftedEntry < s0 + s1) 1
               else if (shiftedEntry < s0 + s1 + s2) 2
               else 3

    val dx = slotIndex % 2
    val dy = slotIndex / 2

    val (cx, cy) = if (inward) {
      val baseStartX = side match {
        case 0 => if (ex + 2 < w) ex + 1 else ex - 2
        case 1 => ex - 2
        case 2 => if (ex - 2 > 0) ex - 2 else ex + 1
        case 3 => ex + 1
      }
      val baseStartY = side match {
        case 0 => ey + 1
        case 1 => if (ey + 2 < h) ey + 1 else ey - 2
        case 2 => ey - 2
        case 3 => if (ey - 2 > 0) ey - 2 else ey + 1
      }
      
      (baseStartX + dx, baseStartY + dy)
    } else {
      side match {
        case 0 => (ex + 1 + dx, ey - 2 + dy)
        case 1 => (ex + 1 + dx, ey + 1 + dy)
        case 2 => (ex - 2 + dx, ey + 1 + dy)
        case _ => (ex - 2 + dx, ey - 2 + dy)
      }
    }
    (cx + offset, cy + offset)
  }

  def calculateNameCoordinates(startOffset: Int): (Int, Int) = {
    val entryZeroBased = if (startOffset == 0) totalFields - 1 else startOffset - 1
    val shiftedEntry = (entryZeroBased + shiftAmount) % totalFields
    val side = if (shiftedEntry < s0) 0 else if (shiftedEntry < s0 + s1) 1 else if (shiftedEntry < s0 + s1 + s2) 2 else 3
    val (_, eyRaw) = getRawCoordinates(shiftedEntry)
    val ey = eyRaw + (if (inward) 0 else offset)

    val (bx, by) = calculateAlignedBaseCoordinates(startOffset, 0)

    if (inward) {
      side match {
        case 0 => (bx, by + 2)
        case 2 => (bx, by - 1)
        case 1 | 3 =>
          if (by > ey) (bx, by + 2)
          else (bx, by - 1)
      }
    } else {
      side match {
        case 0 | 3 => (bx, by - 1)
        case _     => (bx, by + 2)
      }
    }
  }

  def calculateAlignedTargetCoordinates(startOffset: Int, slotIndex: Int): (Int, Int) = {
    val i = slotIndex + 1

    val entryZeroBased = if (startOffset == 0) totalFields - 1 else startOffset - 1
    val shiftedEntry = (entryZeroBased + shiftAmount) % totalFields

    val (ex, ey) = getRawCoordinates(shiftedEntry)

    val side = if (shiftedEntry < s0) 0
               else if (shiftedEntry < s0 + s1) 1
               else if (shiftedEntry < s0 + s1 + s2) 2
               else 3

    val (cx, cy) = if (inward) {
      side match {
        case 0 => (ex, ey + i)
        case 1 => (ex - i, ey)
        case 2 => (ex, ey - i)
        case _ => (ex + i, ey)
      }
    } else {
      side match {
        case 0 => (ex, ey - i)
        case 1 => (ex + i, ey)
        case 2 => (ex, ey + i)
        case _ => (ex - i, ey)
      }
    }
    (cx + offset, cy + offset)
  }
}