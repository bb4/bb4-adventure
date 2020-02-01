// Copyright by Barry G. Becker, 2000-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model

import scala.collection.Set

/**
  * Contains the story script. It can actually represent anything that is in a graph or tree.
  * Just modify the script on disk and run. You can also edit with the StoryEditor UI.
  * This program was originally meant as a very simple example of how you can
  * approach creating a simple adventure game on a computer, but its uses are much more general.
  *
  * @param title title of the story
  * @param name name of the story used as an identifier for by convention
  *             resolution of locations and things like that. e.g. "ludlow".
  * @param author person who created the story document.
  * @param date date story was created.
  * @param resourcePath where the images and sounds will come from
  * @param rootTag the root tag name. Either script or hierarchy depending on which sort of XML dtd we have.
  *                    This is used for exporting, and determining of cycles are allowed.
  * @param scenes the array of scenes that can be used to construct the scene map
  * @author Barry Becker
  */
class Story(val title: String = "", val name: String = "",
            val author: String = "", var date: String = "",
            var resourcePath: String = "", val rootTag: String,
            val scenes: Array[Scene]) {

  assert(scenes.nonEmpty)

  /** The scene where the user is now. */
  private var currentScene: Scene = scenes(0)

  /** A stack of currently visited scenes. There may be duplicates if you visit the same scene twice. */
  private var visitedScenes: Seq[Scene] = Seq()

  /** Maps scene name to the scene. Preserves order of scenes. */
  private var sceneMap: SceneMap = new SceneMap()

  sceneMap.initFromScenes(scenes)

  def this(story: Story) {
    this(story.title, story.name, story.author, story.date, story.resourcePath, story.rootTag, story.scenes)
    currentScene = story.currentScene
    initializeFrom(story)
  }

  private[adventure] def initializeFrom(story: Story): Unit = {
    this.resourcePath = story.resourcePath
    sceneMap = story.sceneMap.copy()
    advanceToScene(story.getCurrentScene.name)
    visitedScenes = story.visitedScenes
  }

  /** @return the title of the story */
  def getTitle: String = title

  /** Return to the initial scene from wherever they be now. */
  def resetToFirstScene(): Unit = {
    currentScene = sceneMap.getFirst
  }

  def getSceneMap: SceneMap = sceneMap

  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit =
    sceneMap.sceneNameChanged(oldSceneName, newSceneName)

  def getCurrentScene: Scene = currentScene
  def getFirstScene: Scene = sceneMap.getFirst
  def isOver: Boolean = getCurrentScene == null

  /** Advance the story to the next scene based on the specified choice
    * @param choice index of the selected choice.
    */
  def advanceScene(choice: Int): Unit = {
    if (choice < 0) {
      currentScene = null // game over
      return
    }
    val nextSceneName = currentScene.getNextSceneName(choice)
    advanceToScene(nextSceneName)
  }

  /** Jump to some arbitrary scene.
    * Not typically used. Should use advanceScene for normal navigation.
    * @param nextSceneName name of the scene to navigate to.
    */
  def advanceToScene(nextSceneName: String): Unit = {
    if (nextSceneName != null) {
      if (currentScene != null) visitedScenes :+= currentScene
      assert(sceneMap.contains(nextSceneName), nextSceneName +
        " not found among map keys: " + sceneMap)
      currentScene = sceneMap.get(nextSceneName)
      assert(currentScene != null, "Could not find a scene named '" + nextSceneName + "'.")
    }
  }

  /** @return a list of all the scenes that led to the current scene. */
  def getParentScenes: Seq[Scene] = sceneMap.getParentScenes(currentScene)

  /** @param newSceneName      name of the new scene. It may or may not exist already.
    * @param choiceDescription text describing what you will do to go to the destination.
    */
  def addChoiceToCurrentScene(newSceneName: String, choiceDescription: String): Unit = {
    // if we do not already have this scene, we need to create it.
    if (!sceneMap.contains(newSceneName)) {
      val newScene = new Scene(newSceneName, " --- describe the scene here ---", resourcePath)
      sceneMap.put(newSceneName, newScene)
    }
    this.getCurrentScene.choices.add(Choice(choiceDescription, newSceneName))
  }

  /** @return a list of all the existing scenes that we could navigate to
    *         that are not already included in the current scene's list of choices.
    */
  def getCandidateDestinationSceneNames: Seq[String] = {
    var candidateSceneNames: Seq[String] = Seq()
    for (sceneName <- sceneMap.sceneNames) {
      if (!getCurrentScene.choices.isDestination(sceneName)) candidateSceneNames :+= sceneName
    }
    candidateSceneNames
  }

  def getAllSceneNames: Set[String] = sceneMap.sceneNames
}
