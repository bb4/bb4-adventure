package com.barrybecker4.puzzle.adventure

import org.scalatest.FunSuite

class ChoiceSuite extends FunSuite {

  test("ChoiceConstruction") {
    val choice = Choice("room", "parlor")
    assertResult("room") { choice.description }
    assertResult("parlor") { choice.destinationScene }
  }
}