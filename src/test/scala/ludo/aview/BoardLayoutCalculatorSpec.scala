import _root_.ludo.aview.BoardLayoutCalculator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BoardLayoutCalculatorSpec extends AnyWordSpec with Matchers {

  "The BoardLayoutCalculator" should {

    "determine if inner layout is possible based on total fields" in {
      new BoardLayoutCalculator(16).inward shouldBe false
      new BoardLayoutCalculator(24).inward shouldBe false
      new BoardLayoutCalculator(36).inward shouldBe true
      new BoardLayoutCalculator(40).inward shouldBe true
    }

    "calculate ring path correctly for 4 fields" in {
      val calc = new BoardLayoutCalculator(4)

      calc.calculateGridCoordinates(1) shouldBe (4, 5)
      calc.calculateGridCoordinates(2) shouldBe (4, 4)
      calc.calculateGridCoordinates(3) shouldBe (5, 4)
      calc.calculateGridCoordinates(4) shouldBe (5, 5)
    }

    "calculate ring path correctly for 16 fields" in {
      val calc = new BoardLayoutCalculator(16)

      calc.calculateGridCoordinates(4) shouldBe (4, 6)
      calc.calculateGridCoordinates(8) shouldBe (6, 4)
      calc.calculateGridCoordinates(12) shouldBe (8, 6)
      calc.calculateGridCoordinates(16) shouldBe (6, 8)
    }

    "calculate ring path correctly for 40 fields" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateGridCoordinates(10) shouldBe (0, 5)
      calc.calculateGridCoordinates(20) shouldBe (5, 0)
      calc.calculateGridCoordinates(30) shouldBe (10, 5)
      calc.calculateGridCoordinates(40) shouldBe (5, 10)
      calc.calculateGridCoordinates(11) shouldBe (0, 4)
    }

    "calculate aligned target coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateAlignedTargetCoordinates(20, 0) shouldBe (5, 1)
      calc.calculateAlignedTargetCoordinates(30, 1) shouldBe (8, 5)
      calc.calculateAlignedTargetCoordinates(0, 2) shouldBe (5, 7)
      calc.calculateAlignedTargetCoordinates(10, 3) shouldBe (4, 5)
    }

    "calculate aligned base coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateAlignedBaseCoordinates(20, 0) shouldBe (6, 1)
      calc.calculateAlignedBaseCoordinates(30, 1) shouldBe (9, 6)
      calc.calculateAlignedBaseCoordinates(0, 2) shouldBe (3, 9)
      calc.calculateAlignedBaseCoordinates(10, 3) shouldBe (2, 4)
    }

    "calculate aligned base coordinates for inward edge cases" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateAlignedBaseCoordinates(23, 0) shouldBe (6, 1)
      calc.calculateAlignedBaseCoordinates(33, 0) shouldBe (8, 6)
      calc.calculateAlignedBaseCoordinates(3, 0) shouldBe (3, 8)
      calc.calculateAlignedBaseCoordinates(13, 0) shouldBe (1, 3)
    }

    "calculate name coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateNameCoordinates(20) shouldBe (6, 3)
      calc.calculateNameCoordinates(30) shouldBe (8, 8)
      calc.calculateNameCoordinates(0) shouldBe (3, 7)
      calc.calculateNameCoordinates(10) shouldBe (1, 2)
    }

    "calculate name coordinates for inward vertical edge cases" in {
      val calc = new BoardLayoutCalculator(40)

      calc.calculateNameCoordinates(25) shouldBe (8, 3)
      calc.calculateNameCoordinates(13) shouldBe (1, 5)
    }

    "calculate aligned target coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)

      calc.calculateAlignedTargetCoordinates(8, 0) shouldBe (6, 3)
      calc.calculateAlignedTargetCoordinates(12, 1) shouldBe (10, 6)
      calc.calculateAlignedTargetCoordinates(0, 2) shouldBe (6, 11)
      calc.calculateAlignedTargetCoordinates(4, 3) shouldBe (0, 6)
    }

    "calculate aligned base coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)

      calc.calculateAlignedBaseCoordinates(8, 0) shouldBe (7, 2)
      calc.calculateAlignedBaseCoordinates(12, 1) shouldBe (10, 7)
      calc.calculateAlignedBaseCoordinates(0, 2) shouldBe (4, 10)
      calc.calculateAlignedBaseCoordinates(4, 3) shouldBe (3, 5)
    }

    "calculate name coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)

      calc.calculateNameCoordinates(8) shouldBe (7, 1)
      calc.calculateNameCoordinates(12) shouldBe (9, 9)
      calc.calculateNameCoordinates(0) shouldBe (4, 11)
      calc.calculateNameCoordinates(4) shouldBe (2, 3)
    }
  }
}
