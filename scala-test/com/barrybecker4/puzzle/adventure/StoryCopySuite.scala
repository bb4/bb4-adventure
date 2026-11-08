package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.*
import org.scalatest.funsuite.AnyFunSuite

class StoryCopySuite extends AnyFunSuite {

  private def twoSceneStory(): Story = {
    val end = new Scene("end", "end desc", None, new ChoiceList(), None, None)
    val start = new Scene(
      "start",
      "start desc",
      None,
      new ChoiceList(Seq(Choice("finish", "end"))),
      None,
      None)
    new Story("t", "n", "", "", "", "script", Array(start, end))
  }

  test("Story copy isolates choice destinations after sceneNameChanged on the copy") {
    val original = twoSceneStory()
    val copy = new Story(original)

    copy.sceneNameChanged("end", "finale")

    assert(copy.getSceneMap.contains("finale"))
    assert(!copy.getSceneMap.contains("end"))
    assert(copy.getSceneMap.get("start").getChoices.map(_.destinationScene) == Seq("finale"))

    assert(original.getSceneMap.contains("end"))
    assert(!original.getSceneMap.contains("finale"))
    assert(original.getSceneMap.get("start").getChoices.map(_.destinationScene) == Seq("end"))
    assert(original.getSceneMap.contains(
      original.getSceneMap.get("start").getChoices.head.destinationScene))
  }
}
