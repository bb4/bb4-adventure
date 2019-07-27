// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model

import java.awt.{Color, Font}
import java.awt.image.BufferedImage
import java.net.URL
import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.puzzle.adventure.model.Scene._
import com.barrybecker4.sound.SoundUtil
import com.barrybecker4.ui.util.{GUIUtil, ImageUtil}


object Scene {

  val PLACEHOLDER_FONT = new Font(GUIUtil.DEFAULT_FONT_FAMILY, Font.PLAIN, 12)

  def loadSound(name: String, resourcePath: String): Option[URL] = {
    var soundUrl: Option[URL] = None
    try {
      val soundPath = resourcePath + "sounds/" + name + ".au"
      soundUrl = Option(FileUtil.getURL(soundPath, failIfNotFound = false))
    } catch {
      case e: NoClassDefFoundError =>
        System.err.println("You are trying to load sound when only text scenes are supported.")
    }
    soundUrl
  }

  def loadImage(name: String, resourcePath: String): Option[BufferedImage] = {
    var image: Option[BufferedImage] = None
    val imagePath = resourcePath + "images/" + name + ".jpg"
    try {
      println("reading image from " + imagePath)
      image = Some(GUIUtil.getBufferedImage(imagePath))
    } catch {
      case e: NoClassDefFoundError =>
        System.err.println("You are trying to load image when only text scenes are supported. " +
          "If you need this to work, add the jai library to your classpath.")
      case ise: IllegalStateException =>
        System.err.println("Could not load image from " + imagePath)
    }
    image
  }
}

/**
  * Every scene has a name, some text which describes the scene, and a list of
  * choices which the actor chooses from to decide what to do next.
  * There is a "Return to last scene" choice automatically appended to all list of choices.
  * A scene may also have an associated sound and image.
  * @param name id/name of the scene
  * @param description textual description of the scene
  * @param label optional label for the scene
  * @param soundUrl optional URL to a sound for this scene
  * @param image optional image to display with this scene
  * @param isFirst if true, then this is the first scene in the story
  * @author Barry Becker
  */
class Scene(var name: String, var description: String, val label: Option[String] = None,
            val choices: Option[ChoiceList] = None,
            val soundUrl: Option[URL] = None, val image: Option[BufferedImage] = None,
            val isFirst: Boolean = false) {

  def this(name: String, description: String, resourcePath: String) {
    this(name, description, None, None,
      loadSound(name, resourcePath), loadImage(name, "name"))
  }

  /** Copy constructor.
    * @param scene the scene to initialize from.
    */
  def this(scene: Scene) {
    this(scene.name, scene.description, scene.label, Some(new ChoiceList(scene)),
      scene.soundUrl, scene.image, scene.isFirst)
  }

  def isValidChoice(i: Int): Boolean =
    hasChoices && i > 0 && i <= getChoices.size

  def deleteChoice(choice: Int): Unit = choices.get.remove(choice)

  /** When changing the name we must call sceneNameChanged on
    * the listeners that are interested in the change.
    * @param name new scene name
    */
  def setName(name: String): Unit = {
    this.name = name
  }

  /** @param scene to see if parent
    * @return true if the specified scene is our immediate parent.
    */
  def isParentOf(scene: Scene): Boolean = {
    val sName = scene.name
    choices.get.isDestination(sName)
  }

  def hasSound: Boolean = soundUrl.isDefined

  def playSound(): Unit = {
    if (hasSound) SoundUtil.playSound(soundUrl.get)
  }

  def getImage: BufferedImage = {
     if (image.isDefined) image.get else createPlaceholderImg()
  }

  private def createPlaceholderImg(): BufferedImage = {
    val placeHolderImg = ImageUtil.createCompatibleImage(200, 100)
    val g = placeHolderImg.createGraphics()
    g.setPaintMode()
    g.setFont(PLACEHOLDER_FONT)
    g.setColor(Color.YELLOW)
    g.drawString(name, 10, 50)
    placeHolderImg
  }

  /** @param choice navigate to the scene indicated by this choice.
    * @return the name of the next scene given the number of the choice.
    */
  def getNextSceneName(choice: Int): String = {
    assert(choice >= 0 || choice < choices.size)
    getChoices(choice).destinationScene
  }

  /** @return true if there are more than one choice for the user to select from. */
  def hasChoices: Boolean = choices.isDefined
  def getChoices: Seq[Choice] = choices.get.choices

  /** Prints what is missing if anything for this scene.
    * @return false if something is missing.
    */
  def verifyMedia: Boolean = {
    if (image == null) {
      throw new IllegalStateException("Image is null for " + this.name)
    }
    if (image.isDefined || !hasSound) {
      System.out.print("scene: " + name)
      if (image == null) System.out.print(" missing image")
      if (!hasSound) System.out.print(" missing sound")
      println("")
      return false
    }
    true
  }

  def print: String = {
    var s: String = s"\n $description\n"
    if (choices.isDefined) {
      s += choices.get.choices.zipWithIndex.map {
        case (c, i) => (i + 1) + ") " + c.description
      }.mkString("\n")
    }
    s
  }

  /** @return the text and choices. */
  override def toString: String = this.name
}
