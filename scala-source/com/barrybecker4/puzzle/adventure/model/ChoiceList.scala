package com.barrybecker4.puzzle.adventure.model

import scala.collection.mutable


/**
  * A list of choices that you can make in a scene.
  * @author Barry Becker
  */
class ChoiceList(var choices: Seq[Choice] = Seq[Choice]()) {

  /** @param scene use the choices from this scene to initialize from.
    *              Choices are deep-copied so later mutations (e.g. rename) do not
    *              alias the source scene's Choice instances.
    */
  def this(scene: Scene) = {
    this(scene.getChoices.map(c => Choice(c.description, c.destinationScene)))
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

  /** Insert at a 0-based index (same indexing as Swing table rows and runtime choice selection). */
  def insert(index: Int, choice: Choice): Unit = {
    require(index >= 0 && index <= choices.size,
      s"choice insert index $index out of range; size is ${choices.size}")
    choices = choices.take(index) ++ Seq(choice) ++ choices.drop(index)
  }

  /** Remove the choice at a 0-based index. */
  def remove(index: Int): Unit = {
    require(index >= 0 && index < choices.size,
      s"choice remove index $index out of range; size is ${choices.size}")
    choices = choices.take(index) ++ choices.drop(index + 1)
  }
}
