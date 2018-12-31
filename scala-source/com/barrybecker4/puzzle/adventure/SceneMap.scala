// Copyright by Barry G. Becker, 2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import scala.collection.{Set, mutable}


/**
  * @author Barry Becker
  */
class SceneMap(map: mutable.LinkedHashMap[String, Scene] = new mutable.LinkedHashMap[String, Scene]()) {

  def copy(): SceneMap = {
    val newMap = new mutable.LinkedHashMap[String, Scene]()
    for (sceneName <- map.keySet) {
      val scene = map(sceneName)
      // add deep copies of the scene.
      newMap.put(sceneName, new Scene(scene))
    }
    new SceneMap(newMap)
  }

  def put(name: String, scene: Scene): Unit =
    map += name -> scene

  def get(name: String): Scene = map(name)
  def getFirst: Scene = map.values.iterator.next
  def contains(name: String): Boolean = map.contains(name)
  def sceneNames: Set[String] = map.keySet
  override def toString: String = map.keySet.mkString(", ")

  def initFromScenes(scenes: Array[Scene]): Unit = {
    map.clear()
    for (scene <- scenes) {
      assert(scene.choices.isDefined)
      map.put(scene.name, scene)
    }
    verifyScenes()
  }

  /** @return a list of all the scenes that led to the specified scene. */
  def getParentScenes(scene: Scene): Seq[Scene] = {
    var parentScenes: Seq[Scene] = Seq()
    // loop through all the scenes, and if any of them have us as a child, add to the list
    for (sceneName <- map.keySet) {
      val s = map(sceneName)
      if (s.isParentOf(scene)) parentScenes :+= s
    }
    parentScenes
  }

  /** make sure the set of scenes in internally consistent. */
  private def verifyScenes(): Unit = {
    for (scene <- map.values) {
      scene.verifyMedia
      for (choice <- scene.getChoices) {
        val dest = choice.destinationScene
        if (dest != null && map.get(choice.destinationScene) == null)
          throw new IllegalStateException(
            "No scene named " + choice.destinationScene + " desc=" + choice.description)
      }
    }
  }

  /** Since the name of one of the scenes has changed we need to update the sceneMap. */
  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit = {
    val changedScene = map.remove(oldSceneName)
    //println("oldScene name=" + oldSceneName +
    // "  newSceneName="+ newSceneName+"  changedScene=" + changedScene.getName())
    map.put(newSceneName, changedScene.get)
    // also need to update the references to named scenes in the choices.
    for (sceneName <- map.keySet) {
      map(sceneName).choices.get.sceneNameChanged(oldSceneName, newSceneName)
    }
  }
}
