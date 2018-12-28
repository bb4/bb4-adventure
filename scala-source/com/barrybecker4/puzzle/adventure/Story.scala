// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.common.xml.DomUtil
import org.w3c.dom.Document
import java.util
import scala.collection.mutable
import scala.collection.Set
import Story._


object Story {
  private val ROOT_ELEMENT = "script"
  /** all the stories need to be stored at this location */
  val STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/"

  /**
    * If args[0] does not have the name of the document to use, use a default.
    *
    * @param args command line args (0 or 1 if name of xml doc is specified.)
    * @return the loaded Document that contains the adventure.
    */
  def importStoryDocument(args: Array[String]): Document = {
    var document: Document = null
    assert(args != null)
    // default story
    var url = FileUtil.getURL(STORIES_ROOT + "ludlow/ludlowScript.xml")
    if (args.length == 1) {
      System.out.println("args[0]=" + args(0))
      url = FileUtil.getURL(STORIES_ROOT + args(0))
    }
    else if (args.length > 1) {
      System.out.println("importStoryDocument Args=" + args.mkString(", "))
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
  *
  * There are many improvements that  I will leave as an exercise for reader (Brian I hope).
  * Next to each is a number which is the number of hours I expect it would take me to implement.
  *
  * 1) Keep track of the items that the player has. They initially start with about 10 things but then find/use
  * items as their adventure progresses.
  * 2) Automatic fighting with monsters. We know the hit points and armor class of the player and monster.
  * It should be a simple matter to have the compat automatically carried out in order to determine the winner and
  * subtract hit point losses as appropriate. We can also take into other effects like disease or healing effects.
  * The player should also be given the option to flee, or instigate other action during the melee.
  * 3) Add a graphical User Interface. We could have windows that pop up to show the players stats or item inventory.
  * 4) Make multi-player (hard)
  * 7) Allow the user to edit the scenes - live through the UI.
  * When editing a scene you are presented with a form that has all the
  * attributes for the scene including a dropdown for selecting which scenes navigate to it and
  * where you can navigate to from this scene. Save and load the xml that defines the game.
  * 8) This type of application could be used for more than just games. Tutorials or an expert system would
  * be other nice applications.
  * 9) Have probabilistic choices. For example, if you encounter a monster and choose to fight it, then
  * the outcome may be one of several different things. We can also influence the outcome by what sort of
  * items the player has.
  * 10) fix sound deploy in ant
  * 11) add means to edit the network of scene from within the application. Show all scene leading to and from
  * the current scene. Allow editing of scene properties and associating media.
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

  /** Copy constructor. Creates a deep copy.
    * @param story story to copy
    */
  def this(story: Story) {
    this(story.getTitle, story.name, story.author, story.date)
    initializeFrom(story)
  }

  private def initializeFrom(story: Story): Unit = {
    this.resourcePath = story.resourcePath
    if (sceneMap == null)
      sceneMap = createSceneMap(0)
    this.sceneMap.clear()
    copySceneMap(story.getSceneMap)
    this.advanceToScene(story.getCurrentScene.name)
    this.visitedScenes = story.visitedScenes
  }

  /** @return the title of the story */
  def getTitle: String = title

  /** Return to the initial sceen from wherever they be now. */
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
      System.out.println("done saving.")
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
  private def createSceneMap(size: Int) = new mutable.LinkedHashMap[String, Scene]()

  private def copySceneMap(fromMap: mutable.LinkedHashMap[String, Scene]): Unit = {
    for (sceneName <- fromMap.keySet) {
      val scene = fromMap(sceneName)
      // add deep copies of the scene.
      sceneMap.put(sceneName, new Scene(scene))
    }
  }

  private def initFromScenes(scenes: Array[Scene]): Unit = {
    sceneMap = createSceneMap(scenes.length)
    for (scene <- scenes) {
      assert(scene.choices.isDefined)
      sceneMap.put(scene.name, scene)
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
      currentScene = sceneMap(nextSceneName)
      assert(currentScene != null, "Could not find a scene named '" + nextSceneName + "'.")
    }
  }

  /** @return a list of all the scenes that led to the current scene. */
  def getParentScenes: util.List[Scene] = {
    val parentScenes = new util.ArrayList[Scene]
    // loop through all the scenes, and if any of them have us as a child, add to the list
    for (sceneName <- sceneMap.keySet) {
      val s = sceneMap(sceneName)
      if (s.isParentOf(currentScene)) parentScenes.add(s)
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
  def getCandidateDestinationSceneNames: util.List[String] = {
    val candidateSceneNames = new util.ArrayList[String]
    for (sceneName <- sceneMap.keySet) {
      if (!getCurrentScene.choices.get.isDestination(sceneName)) candidateSceneNames.add(sceneName)
    }
    candidateSceneNames
  }

  def getAllSceneNames: Set[String] = sceneMap.keySet

  /** make sure the set of scenes in internally consistent. */
  private def verifyScenes(): Unit = {
    for (scene <- sceneMap.values) {
      scene.verifyMedia
      for (choice <- scene.choices.get.choices) {
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
