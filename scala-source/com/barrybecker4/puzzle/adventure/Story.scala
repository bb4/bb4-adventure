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
  val DEFAULT_STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/ludlow/"

  /**
    * If args[0] does not have the name of the document to use, use a default.
    * @param args command line args (0 or 1 if name of xml doc is specified.)
    * @return (the loaded Document that contains the adventure, fileRoot).
    */
  def importStoryDocument(args: Array[String]): (Document, String) = {
    if (args == null || args.isEmpty)
      importStoryDocument()
    else if (args.length == 1)
      importStoryDocument(args(0))
    else
      importStoryDocument(args(0), args(1))
  }

  /**
    * If args[0] does not have the name of the document to use, use a default.
    * The media files are expected to be relative to the script file.
    * @param fileName the end of the file path. Something like "ludlowScript.xml".
    * @param fileRoot the beginning of the path. Something like "com/user/puzzle/adventure/stories/ludlow/"
    * @return (the loaded Document that contains the adventure, resource root).
    */
  def importStoryDocument(fileName: String = "ludlowScript.xml",
                          fileRoot: String = DEFAULT_STORIES_ROOT): (Document, String) = {
    var document: Document = null
    println("creating url from " + fileRoot + fileName)
    val url = FileUtil.getURL(fileRoot + fileName)
    println("about to parse url=" + url + "\n story file location")
    document = DomUtil.parseXML(url)
    //DomUtil.printTree(document, 0);
    (document, fileRoot)
  }

  private def extractScenesFromDoc(document: Document, resourcePath: String): Array[Scene] = {
    val root = document.getDocumentElement

    val schemaName: String = root.getTagName
    println(s"schema name: ${root.getTagName}")

    val children = root.getChildNodes
    val scenes = new Array[Scene](children.getLength)
    var i = 0
    while (i < children.getLength) {
      scenes(i) = new Scene(children.item(i), resourcePath, i == 0)
      i += 1
    }
    scenes
  }
}

/**
  * Run your own adventure story. It can actually represent anything that is in a graph.
  * Just modify the script on disk and run. You can also edit with the StoryEditor UI.
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
            val author: String = "", var date: String = "",
            var resourcePath: String = "",
            val scenes: Array[Scene]) {

  assert(scenes.nonEmpty)
  /** The scene where the user is now. */
  private var currentScene: Scene = scenes(0)
  /** A stack of currently visited scenes. There may be duplicates if you visit the same scene twice. */
  private var visitedScenes: Seq[Scene] = Seq()
  /** Maps scene name to the scene. Preserves order of scenes. */
  private var sceneMap: SceneMap = new SceneMap()
  sceneMap.initFromScenes(scenes)

  /** Construct an adventure given an xml document object
    * @param document containing the scene data
    */
  def this(document: Document, resourcePath: String) {
    this(
      DomUtil.getAttribute(document.getDocumentElement, "title"),
      DomUtil.getAttribute(document.getDocumentElement, "name"),
      DomUtil.getAttribute(document.getDocumentElement, "author"),
      DomUtil.getAttribute(document.getDocumentElement, "date"),
      resourcePath,
      extractScenesFromDoc(document, resourcePath)
    )
  }

  /** Construct an adventure given an xml document object
    * @param docAndPath (doc containing the scene data, resourcePath)
    */
  def this(docAndPath: (Document, String)) {
    this(docAndPath._1, docAndPath._2)
  }

  def this(story: Story) {
    this(story.title, story.name, story.author, story.date, story.resourcePath, story.scenes)
    currentScene = story.currentScene
    initializeFrom(story)
  }

  private[adventure] def initializeFrom(story: Story): Unit = {
    this.resourcePath = story.resourcePath
    sceneMap = story.getSceneMap.copy()
    advanceToScene(story.getCurrentScene.name)
    visitedScenes = story.visitedScenes
  }

  /** @return the title of the story */
  def getTitle: String = title

  /** Return to the initial scene from wherever they be now. */
  def resetToFirstScene(): Unit = {
    currentScene = sceneMap.getFirst
  }

  def sceneNameChanged(oldSceneName: String, newSceneName: String): Unit =
    sceneMap.sceneNameChanged(oldSceneName, newSceneName)

  /** Write the story document back to xml.
    * @param destFileName file to write to.
    */
  def saveStoryDocument(destFileName: String): Unit = {
    try {
      val document = createStoryDocument
      DomUtil.writeXMLFile(destFileName, document, "script.dtd")
      println("done saving.")
    } catch {
      case e: Exception => throw new IllegalStateException("Could not save. ", e)
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
    for (sceneName <- sceneMap.sceneNames) {
      val scene: Scene = sceneMap.get(sceneName)
      scene.appendToDocument(document)
    }
    document
  }

  /** must be ordered */
  private def createSceneMap() = new mutable.LinkedHashMap[String, Scene]()

  private def initFromScenes(scenes: Array[Scene]): Unit = {
    sceneMap.initFromScenes(scenes)
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
    this.getCurrentScene.choices.get.add(Choice(choiceDescription, newSceneName))
  }

  /** @return a list of all the existing scenes that we could navigate to
    *         that are not already included in the current scene's list of choices.
    */
  def getCandidateDestinationSceneNames: Seq[String] = {
    var candidateSceneNames: Seq[String] = Seq()
    for (sceneName <- sceneMap.sceneNames) {
      if (!getCurrentScene.choices.get.isDestination(sceneName)) candidateSceneNames :+= sceneName
    }
    candidateSceneNames
  }

  def getAllSceneNames: Set[String] = sceneMap.sceneNames
}
