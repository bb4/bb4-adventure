package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class ChoiceListSuite extends AnyFunSuite {

  test("ChoiceList sceneNameChanged updates destinations") {
    val cl = new ChoiceList(Seq(Choice("go", "old")))
    cl.sceneNameChanged("old", "new")
    assert(cl.choices.head.destinationScene == "new")
  }

  test("ChoiceList remove drops 1-based index") {
    val cl = new ChoiceList(Seq(Choice("a", "1"), Choice("b", "2"), Choice("c", "3")))
    cl.remove(2)
    assert(cl.choices.map(_.destinationScene) == Seq("1", "3"))
  }

  test("ChoiceList update rebuilds from LinkedHashMap order") {
    val cl = new ChoiceList(Seq(Choice("d1", "dest1"), Choice("d2", "dest2")))
    val m = mutable.LinkedHashMap("dest2" -> "second", "dest1" -> "first")
    cl.update(m)
    assert(cl.choices.map(c => (c.description, c.destinationScene)) == Seq(
      ("second", "dest2"),
      ("first", "dest1")))
  }

  test("ChoiceList from Scene copies choices") {
    val scene = new Scene(
      "s",
      "d",
      None,
      new ChoiceList(Seq(Choice("x", "y"))),
      None,
      None,
      true)
    val cl2 = new ChoiceList(scene)
    assert(cl2.choices == scene.getChoices)
  }
}
