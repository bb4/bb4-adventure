package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene}
import org.scalatest.funsuite.AnyFunSuite

class TextAdventureSuite extends AnyFunSuite {

  test("parseChoiceLine accepts integers and rejects garbage") {
    assert(TextAdventure.parseChoiceLine("  2 ").contains(2))
    assert(TextAdventure.parseChoiceLine("3").contains(3))
    assert(TextAdventure.parseChoiceLine("x").isEmpty)
    assert(TextAdventure.parseChoiceLine("").isEmpty)
  }

  test("getNextSceneIndex skips bad lines then accepts valid 1-based choice") {
    val scene = new Scene(
      "s",
      "d",
      None,
      new ChoiceList(Seq(Choice("a", "x"), Choice("b", "y"))),
      None,
      None,
      true)
    val lines = Iterator("not-a-number", "9", "2")
    val idx = TextAdventure.getNextSceneIndex(scene, () => lines.next())
    assert(idx == 1)
  }

  test("getNextSceneIndex returns -1 when scene has no choices") {
    val scene =
      new Scene("s", "d", None, new ChoiceList(), None, None, true)
    assert(TextAdventure.getNextSceneIndex(scene, () => "") == -1)
  }
}
