package ludo.aview

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class BoardLayoutCalculatorSpec extends AnyWordSpec with Matchers {

  "The BoardLayoutCalculator" should {

    "determine if inner layout is possible based on total fields" in {
      new BoardLayoutCalculator(16).inward should be (false)
      new BoardLayoutCalculator(24).inward should be (false)
      new BoardLayoutCalculator(36).inward should be (true)
      new BoardLayoutCalculator(40).inward should be (true)
    }

    "calculate ring path correctly for 4 fields" in {
      val calc = new BoardLayoutCalculator(4)
      calc.calculateGridCoordinates(1) should be ((4, 5))
      calc.calculateGridCoordinates(2) should be ((4, 4))
      calc.calculateGridCoordinates(3) should be ((5, 4))
      calc.calculateGridCoordinates(4) should be ((5, 5))
    }

    "calculate ring path correctly for 16 fields" in {
      val calc = new BoardLayoutCalculator(16)
      calc.calculateGridCoordinates(4) should be ((4, 6))
      calc.calculateGridCoordinates(8) should be ((6, 4))
      calc.calculateGridCoordinates(12) should be ((8, 6))
      calc.calculateGridCoordinates(16) should be ((6, 8))
    }

    "calculate ring path correctly for standard 40 fields" in {
      val calc = new BoardLayoutCalculator(40)
      calc.calculateGridCoordinates(10) should be ((0, 5))
      calc.calculateGridCoordinates(20) should be ((5, 0))
      calc.calculateGridCoordinates(30) should be ((10, 5))
      calc.calculateGridCoordinates(40) should be ((5, 10))
      calc.calculateGridCoordinates(11) should be ((0, 4))
    }

    "calculate aligned target coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)
      calc.calculateAlignedTargetCoordinates(20, 0) should be ((5, 1))
      calc.calculateAlignedTargetCoordinates(30, 1) should be ((8, 5))
      calc.calculateAlignedTargetCoordinates(0, 2) should be ((5, 7))
      calc.calculateAlignedTargetCoordinates(10, 3) should be ((4, 5))
    }

    "calculate aligned base coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)
      calc.calculateAlignedBaseCoordinates(20, 0) should be ((6, 1))
      calc.calculateAlignedBaseCoordinates(30, 1) should be ((9, 6))
      calc.calculateAlignedBaseCoordinates(0, 2) should be ((3, 9))
      calc.calculateAlignedBaseCoordinates(10, 3) should be ((2, 4))
    }

    "calculate name coordinates inwards for all sides" in {
      val calc = new BoardLayoutCalculator(40)
      calc.calculateNameCoordinates(20) should be ((6, 3))
      calc.calculateNameCoordinates(30) should be ((8, 8))
      calc.calculateNameCoordinates(0) should be ((3, 7))
      calc.calculateNameCoordinates(10) should be ((1, 2))
    }

    "calculate aligned target coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)
      calc.calculateAlignedTargetCoordinates(8, 0) should be ((6, 3))
      calc.calculateAlignedTargetCoordinates(12, 1) should be ((10, 6))
      calc.calculateAlignedTargetCoordinates(0, 2) should be ((6, 11))
      calc.calculateAlignedTargetCoordinates(4, 3) should be ((0, 6))
    }

    "calculate aligned base coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)
      calc.calculateAlignedBaseCoordinates(8, 0) should be ((7, 2))
      calc.calculateAlignedBaseCoordinates(12, 1) should be ((10, 7))
      calc.calculateAlignedBaseCoordinates(0, 2) should be ((4, 10))
      calc.calculateAlignedBaseCoordinates(4, 3) should be ((3, 5))
    }

    "calculate name coordinates outwards for all sides" in {
      val calc = new BoardLayoutCalculator(16)
      calc.calculateNameCoordinates(8) should be ((7, 1))
      calc.calculateNameCoordinates(12) should be ((9, 9))
      calc.calculateNameCoordinates(0) should be ((4, 11))
      calc.calculateNameCoordinates(4) should be ((2, 3))
    }
  }
}
