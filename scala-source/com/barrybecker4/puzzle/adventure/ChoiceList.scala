package com.barrybecker4.puzzle.adventure

import org.w3c.dom.Node
import scala.collection.mutable
import ChoiceList._


object ChoiceList {

  /** @return extracted choices from a sceneNode */
  private def getChoices(sceneNode: Node): Seq[Choice] = {
    // if there are choices they will be the second element (right after description).
    val children = sceneNode.getChildNodes
    var choices: Seq[Choice] = Seq()
    if (children.getLength > 1) {
      val choicesNode = children.item(1)
      val choiceList = choicesNode.getChildNodes
      val numChoices = choiceList.getLength
      choices = Seq()
      var i = 0
      while (i < numChoices) {
        assert(choiceList.item(i) != null)
        choices :+= new Choice(choiceList.item(i))
        i += 1
      }
    }
    choices
  }
}

/**
  * A list of choices that you can make in a scene.
  * @author Barry Becker
  */
class ChoiceList(var choices: Seq[Choice]) {

  /** @param scene use the choices from this scene to initialize from. */
  def this(scene: Scene) {
    this(scene.getChoices)
  }

  /** @param sceneNode to initialize from. */
  def this(sceneNode: Node) {
    this(getChoices(sceneNode))
  }

  /** @param sceneName sceneName to look for as a destination.
    * @return true if sceneName is one of our choices.
    */
  def isDestination(sceneName: String): Boolean = choices.exists(_.destinationScene == sceneName)

  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit = {
    for (c <- choices) {
      if (c.destinationScene == oldSceneName) c.destinationScene = newSceneName
    }
  }

  /** Update the order and descriptions
    * @param choiceMap new order and descriptions to update with.
    */
  def update(choiceMap: mutable.LinkedHashMap[String, String]): Unit = {
    assert(choiceMap.size == choices.size,
      "choiceMap.size()=" + choiceMap.size + " not equal choices.size()=" + choices.size)
    var newChoices = Seq[Choice]()
    for (dest <- choiceMap.keySet) {
      newChoices :+= Choice(choiceMap(dest), dest)
    }
    choices = newChoices
  }

  def size: Int = choices.size
  def isEmpty: Boolean = choices.isEmpty
  def contains(o: Any): Boolean = choices.contains(o)
  def add(choice: Choice): Unit = { choices :+= choice }
  def remove(index: Int): Unit = choices = choices.take(index - 1) ++ choices.drop(index)
}