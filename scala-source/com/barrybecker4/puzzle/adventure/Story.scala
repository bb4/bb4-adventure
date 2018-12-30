// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.common.xml.DomUtil
import org.w3c.dom.Document
import scala.collection.mutable
import scala.collection.Set
import Story._


object Story {
  private val ROOT_ELEMENT = "script"
  /** all the stories need to be stored at this location */
  val STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/"

  /**
    * If args[0] does not have the name of the document to use, use a default.
    * @param args command line args (0 or 1 if name of xml doc is specified.)
    * @return the loaded Document that contains the adventure.
    */
  def importStoryDocument(args: Array[String]): Document = {
    var document: Document = null
    assert(args != null)
    // default story
    var url = FileUtil.getURL(STORIES_ROOT + "ludlow/ludlowScript.xml")
    if (args.length == 1) {
      println("args[0]=" + args(0))
      url = FileUtil.getURL(STORIES_ROOT + args(0))
    }
    else if (args.length > 1) {
      println("importStoryDocument Args=" + args.mkString(", "))
      url = FileUtil.getURL(STORIES_ROOT + args(1))
    }
    //throw new IllegalStateException("bad url=" + url + "args="+ args);
    println("about to parse url=" + url + "\n story file location")
    document = DomUtil.parseXML(url)
    //DomUtil.printTree(document, 0);
    document
  }
}

/**
  * Run your own adventure story.
  * Just modify the script in SceneData and run.
  * This program is meant as a very simple example of how you can
  * approach creating a simple adventure game on a computer.
  * @param title  title of the story
  * @param name name of the story used as an identifier for by convention
  *             resolution of locations and things like that. e.g. ludlow.
  * @param author person who created the story document.
  * @param date  date story was created.
  * @author Barry Becker
  */
class Story(val title: String = "", val name: String = "",
            val author: String = "", var date: String = "") {
  /** The scene where the user is now. */
  private var currentScene: Scene = _
  private var resourcePath: String = _
  /** A stack of currently visited scenes. There may be duplicates if you visit the same scene twice. */
  private var visitedScenes: Seq[Scene] = Seq()
  /** Maps scene name to the scene. Preserves order of scenes. */
  private var sceneMap: mutable.LinkedHashMap[String, Scene] = _

  /** Construct an adventure given an xml document object
    * @param document containing the scene data
    */
  def this(document: Document) {
    this(
      DomUtil.getAttribute(document.getDocumentElement, "title"),
      DomUtil.getAttribute(document.getDocumentElement, "name"),
      DomUtil.getAttribute(document.getDocumentElement, "author"),
      DomUtil.getAttribute(document.getDocumentElement, "date"),
    )

    val root = document.getDocumentElement
    resourcePath = STORIES_ROOT + name + "/"
    val children = root.getChildNodes
    val scenes = new Array[Scene](children.getLength)
    var i = 0
    while (i < children.getLength) {
      scenes(i) = new Scene(children.item(i), resourcePath, i == 0)
      i += 1
    }
    initFromScenes(scenes)
  }

  def this(story: Story) {
    this(story.title, story.name, story.author, story.date)
    currentScene = story.currentScene
    initializeFrom(story)
  }

  private[adventure] def initializeFrom(story: Story): Unit = {
    this.resourcePath = story.resourcePath
    if (sceneMap == null)
      sceneMap = createSceneMap()
    sceneMap.clear()
    sceneMap = copySceneMap(story.getSceneMap)
    advanceToScene(story.getCurrentScene.name)
    visitedScenes = story.visitedScenes
  }

  /** @return the title of the story */
  def getTitle: String = title

  /** Return to the initial scene from wherever they be now. */
  def resetToFirstScene(): Unit = {
    currentScene = sceneMap.values.iterator.next
  }

  /** Write the story document back to xml.
    * @param destFileName file to write to.
    */
  def saveStoryDocument(destFileName: String): Unit = {
    try {
      val document = createStoryDocument
      DomUtil.writeXMLFile(destFileName, document, "script.dtd")
      println("done saving.")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  private def getSceneMap = sceneMap

  /** @return the story document based on the current state of the story. */
  private def createStoryDocument = {
    val document = DomUtil.createNewDocument
    val rootElement = document.createElement(Story.ROOT_ELEMENT)
    rootElement.setAttribute("author", author)
    rootElement.setAttribute("name", name)
    rootElement.setAttribute("date", date)
    rootElement.setAttribute("title", title)
    document.appendChild(rootElement)
    for (sceneName <- sceneMap.keySet) {
      val scene: Scene = sceneMap(sceneName)
      scene.appendToDocument(document)
    }
    document
  }

  /** Construct an adventure given a list of scenes.
    * @param scenes array of scenes to use in this story.
    */
  def this(scenes: Array[Scene]) {
    this()
    initFromScenes(scenes)
  }

  /** must be ordered */
  private def createSceneMap() = new mutable.LinkedHashMap[String, Scene]()

  private def copySceneMap(fromMap: mutable.LinkedHashMap[String, Scene]): mutable.LinkedHashMap[String, Scene] = {
    println("now copying these scenes from fromMap: " + fromMap.keySet)
    val map = new mutable.LinkedHashMap[String, Scene]()
    for (sceneName <- fromMap.keySet) {
      val scene = fromMap(sceneName)
      // add deep copies of the scene.
      map.put(sceneName, new Scene(scene))
    }
    map
  }

  private def initFromScenes(scenes: Array[Scene]): Unit = {
    sceneMap = createSceneMap()
    for (scene <- scenes) {
      assert(scene.choices.isDefined)
      sceneMap += scene.name -> scene
    }
    verifyScenes()
    currentScene = scenes(0)
    visitedScenes = Seq()
  }

  def getCurrentScene: Scene = currentScene
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
        " not found among map keys: " + sceneMap.keySet.mkString(","))
      currentScene = sceneMap(nextSceneName)
      assert(currentScene != null, "Could not find a scene named '" + nextSceneName + "'.")
    }
  }

  /** @return a list of all the scenes that led to the current scene. */
  def getParentScenes: Seq[Scene] = {
    var parentScenes: Seq[Scene] = Seq()
    // loop through all the scenes, and if any of them have us as a child, add to the list
    for (sceneName <- sceneMap.keySet) {
      val s = sceneMap(sceneName)
      if (s.isParentOf(currentScene)) parentScenes :+= s
    }
    parentScenes
  }

  /** @param newSceneName      name of the new scene. It may or may not exist already.
    * @param choiceDescription text describing what you will do to go to the destination.
    */
  def addChoiceToCurrentScene(newSceneName: String, choiceDescription: String): Unit = {
    // if we do not already have this scene, we need to create it.
    if (!sceneMap.contains(newSceneName)) {
      val newScene = new Scene(newSceneName, " --- describe the scene here ---", resourcePath)
      sceneMap.put(newSceneName, newScene)
    }
    this.getCurrentScene.choices.get.add(Choice(choiceDescription, newSceneName))
  }

  /** @return a list of all the existing scenes that we could navigate to
    *         that are not already included in the current scene's list of choices.
    */
  def getCandidateDestinationSceneNames: Seq[String] = {
    var candidateSceneNames: Seq[String] = Seq()
    for (sceneName <- sceneMap.keySet) {
      if (!getCurrentScene.choices.get.isDestination(sceneName)) candidateSceneNames :+= sceneName
    }
    candidateSceneNames
  }

  def getAllSceneNames: Set[String] = sceneMap.keySet

  /** make sure the set of scenes in internally consistent. */
  private def verifyScenes(): Unit = {
    for (scene <- sceneMap.values) {
      scene.verifyMedia
      for (choice <- scene.getChoices) {
        val dest = choice.destinationScene
        if (dest != null && sceneMap.get(choice.destinationScene) == null)
          throw new IllegalStateException(
            "No scene named " + choice.destinationScene + " desc=" + choice.description)
      }
    }
  }

  /** Since the name of one of the scenes has changed we need to update the sceneMap. */
  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit = {
    val changedScene = sceneMap.remove(oldSceneName)
    //println("oldScene name=" + oldSceneName +
    // "  newSceneName="+ newSceneName+"  changedScene=" + changedScene.getName())
    sceneMap.put(newSceneName, changedScene.get)
    // also need to update the references to named scenes in the choices.
    for (sceneName <- sceneMap.keySet) {
      sceneMap(sceneName).choices.get.sceneNameChanged(oldSceneName, newSceneName)
    }
  }
}