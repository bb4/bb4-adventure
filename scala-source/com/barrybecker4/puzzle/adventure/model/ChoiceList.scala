package com.barrybecker4.puzzle.adventure.model

import scala.collection.mutable


/**
  * A list of choices that you can make in a scene.
  * @author Barry Becker
  */
class ChoiceList(var choices: Seq[Choice] = Seq[Choice]()) {

  /** @param scene use the choices from this scene to initialize from. */
  def this(scene: Scene) = {
    this(scene.getChoices)
  }

  /** @param sceneName sceneName to look for as a destination.
    * @return true if sceneName is one of our choices.
    */
  def isDestination(sceneName: String): Boolean = choices.exists(_.destinationScene == sceneName)

  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit =
    choices.foreach { c =>
      if (c.destinationScene == oldSceneName) c.destinationScene = newSceneName
    }

  /** Update the order and descriptions
    * @param choiceMap new order and descriptions to update with.
    */
  def update(choiceMap: mutable.LinkedHashMap[String, String]): Unit = {
    if (choiceMap.size != choices.size) {
      println("Old choices: " + choices.mkString(", "))
      println("New choices: " + choiceMap.mkString(", "))
    }
    choices = choiceMap.iterator.map { case (dest, desc) => Choice(desc, dest) }.toSeq
  }

  def size: Int = choices.size
  def isEmpty: Boolean = choices.isEmpty
  def contains(o: Any): Boolean = choices.contains(o)
  def add(choice: Choice): Unit = { choices :+= choice }

  /** Remove the choice at the given 0-based index. */
  def remove(index: Int): Unit = {
    require(index >= 0 && index < choices.size,
      s"choice index $index out of range; size is ${choices.size}")
    choices = choices.take(index) ++ choices.drop(index + 1)
  }
}
