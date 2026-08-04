package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene}
import org.scalatest.funsuite.AnyFunSuite

class SceneSuite extends AnyFunSuite {

  private def sceneWithChoices(choices: Choice*): Scene =
    new Scene(
      "start",
      "You stand at a fork.",
      None,
      new ChoiceList(choices),
      None,
      None,
      true)

  test("print includes description and 1-based choice lines") {
    val scene = sceneWithChoices(Choice("go left", "left"), Choice("go right", "right"))
    val text = scene.print
    assert(text.contains("You stand at a fork."))
    assert(text.contains("1) go left"))
    assert(text.contains("2) go right"))
  }

  test("print is description only when there are no choices") {
    val scene = sceneWithChoices()
    assert(scene.print == "\n You stand at a fork.\n")
  }

  test("isParentOf is true when choice destinations match child name") {
    val child = new Scene("left", "left room", None, new ChoiceList(), None, None, false)
    val parent = sceneWithChoices(Choice("go left", "left"))
    assert(parent.isParentOf(child))
    assert(!child.isParentOf(parent))
  }

  test("isValidChoice accepts 1-based indices within range") {
    val scene = sceneWithChoices(Choice("a", "x"), Choice("b", "y"))
    assert(!scene.isValidChoice(0))
    assert(scene.isValidChoice(1))
    assert(scene.isValidChoice(2))
    assert(!scene.isValidChoice(3))
  }

  test("verifyMedia is true for text-only scenes with neither image nor sound") {
    val scene = sceneWithChoices()
    assert(scene.verifyMedia)
  }
}
