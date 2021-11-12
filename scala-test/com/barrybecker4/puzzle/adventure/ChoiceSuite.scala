package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.Choice
import org.scalatest.funsuite.AnyFunSuite

class ChoiceSuite extends AnyFunSuite {

  test("ChoiceConstruction") {
    val choice = Choice("room", "parlor")
    assertResult("room") { choice.description }
    assertResult("parlor") { choice.destinationScene }
  }
}
