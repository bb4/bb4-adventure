package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.*
import org.scalatest.funsuite.AnyFunSuite

class UniquePathsFinderSuite extends AnyFunSuite {

  test("findUniquePaths counts distinct acyclic routes in a diamond") {
    val d = new Scene("d", "d", None, new ChoiceList(), None, None, false)
    val b = new Scene(
      "b",
      "b",
      None,
      new ChoiceList(Seq(Choice("to d", "d"))),
      None,
      None,
      false)
    val c = new Scene(
      "c",
      "c",
      None,
      new ChoiceList(Seq(Choice("to d", "d"))),
      None,
      None,
      false)
    val a = new Scene(
      "a",
      "a",
      None,
      new ChoiceList(Seq(Choice("to b", "b"), Choice("to c", "c"))),
      None,
      None,
      true)
    val story = new Story("t", "n", "", "", "", "script", Array(a, b, c, d))
    val paths = UniquePathsFinder(story).findUniquePaths(d)
    assert(paths.size == 2)
    assert(paths.forall(_.last == d))
    assert(paths.forall(_.head == a))
  }

  test("findUniquePaths is one for a simple chain") {
    val b = new Scene("b", "b", None, new ChoiceList(), None, None, false)
    val a = new Scene(
      "a",
      "a",
      None,
      new ChoiceList(Seq(Choice("go", "b"))),
      None,
      None,
      true)
    val story = new Story("t", "n", "", "", "", "script", Array(a, b))
    val paths = UniquePathsFinder(story).findUniquePaths(b)
    assert(paths == List(Seq(a, b)))
  }
}
