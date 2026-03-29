package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.*
import org.scalatest.funsuite.AnyFunSuite

class StoryAndSceneMapSuite extends AnyFunSuite {

  private def twoSceneStory(): Story = {
    val end = new Scene("end", "end desc", None, new ChoiceList(), None, None, false)
    val start = new Scene(
      "start",
      "start desc",
      None,
      new ChoiceList(Seq(Choice("finish", "end"))),
      None,
      None,
      true)
    new Story("t", "n", "", "", "", "script", Array(start, end))
  }

  test("Story advanceScene and isOver after terminal choice") {
    val story = twoSceneStory()
    assert(story.getCurrentScene.name == "start")
    story.advanceScene(0)
    assert(story.getCurrentScene.name == "end")
    story.advanceScene(-1)
    assert(story.isOver)
  }

  test("Story resetToFirstScene returns to root") {
    val story = twoSceneStory()
    story.advanceScene(0)
    assert(story.getCurrentScene.name == "end")
    story.resetToFirstScene()
    assert(story.getCurrentScene.name == "start")
  }

  test("Story advanceToScene updates current scene") {
    val story = twoSceneStory()
    story.advanceToScene("end")
    assert(story.getCurrentScene.name == "end")
  }

  test("SceneMap initFromScenes throws when choice references missing scene") {
    val orphan = new Scene(
      "start",
      "s",
      None,
      new ChoiceList(Seq(Choice("nowhere", "missing"))),
      None,
      None,
      true)
    intercept[IllegalStateException] {
      new Story("t", "n", "", "", "", "script", Array(orphan))
    }
  }

  test("SceneMap sceneNameChanged updates keys and choice destinations") {
    val story = twoSceneStory()
    story.sceneNameChanged("end", "finale")
    assert(story.getSceneMap.contains("finale"))
    assert(!story.getSceneMap.contains("end"))
    story.advanceToScene("start")
    assert(story.getCurrentScene.getChoices.map(_.destinationScene) == Seq("finale"))
  }

  test("Scene getNextSceneName requires in-range index") {
    val scene = new Scene(
      "s",
      "d",
      None,
      new ChoiceList(Seq(Choice("a", "x"), Choice("b", "y"))),
      None,
      None,
      true)
    assert(scene.getNextSceneName(0) == "x")
    intercept[IllegalArgumentException] {
      scene.getNextSceneName(2)
    }
    intercept[IllegalArgumentException] {
      scene.getNextSceneName(-1)
    }
  }
}
