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

  def loadSound(name: String, resourcePath: String): Option[URL] =
    try {
      val auPath = resourcePath + "sounds/" + name + ".au"
      Option(FileUtil.getURL(auPath, failIfNotFound = false)).orElse {
        val wavPath = resourcePath + "sounds/" + name + ".wav"
        Option(FileUtil.getURL(wavPath, failIfNotFound = false))
      }
    } catch {
      case _: NoClassDefFoundError =>
        System.err.println("You are trying to load sound when only text scenes are supported.")
        None
    }

  def loadImage(name: String, resourcePath: String): Option[BufferedImage] = {
    val imagePath = resourcePath + "images/" + name + ".jpg"
    try {
      Some(GUIUtil.getBufferedImage(imagePath))
    } catch {
      case _: NoClassDefFoundError =>
        System.err.println("You are trying to load image when only text scenes are supported. " +
          "If you need this to work, add the jai library to your classpath.")
        None
      case _: IllegalStateException =>
        System.err.println("Could not load image from: " + imagePath)
        None
    }
  }
}

/**
  * Every scene has a name, some text which describes the scene, and a list of
  * choices which the actor chooses from to decide what to do next.
  * There is a "Return to last scene" choice automatically appended to all list of choices.
  * A scene may also have an associated sound and image.
  * @param name unique id/name of the scene. No spaces should be in it.
  * @param description textual description of the scene
  * @param label optional label for the scene
  * @param soundUrl optional URL to a sound for this scene
  * @param image optional image to display with this scene
  * @param isFirst if true, then this is the first/root scene in the story
  * @author Barry Becker
  */
class Scene(var name: String, var description: String, var label: Option[String] = None,
            val choices: ChoiceList = new ChoiceList(),
            val soundUrl: Option[URL] = None, val image: Option[BufferedImage] = None,
            val isFirst: Boolean = false) {

  def this(name: String, description: String, resourcePath: String) = {
    this(
      name,
      description,
      None,
      new ChoiceList(),
      loadSound(name, resourcePath),
      loadImage(name, resourcePath))
  }

  /** Copy constructor.
    * @param scene the scene to initialize from.
    */
  def this(scene: Scene) = {
    this(scene.name, scene.description, scene.label, new ChoiceList(scene),
      scene.soundUrl, scene.image, scene.isFirst)
  }

  def isValidChoice(i: Int): Boolean =
    hasChoices && i > 0 && i <= getChoices.size

  def deleteChoice(choice: Int): Unit = choices.remove(choice)

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
    choices.isDestination(sName)
  }

  def hasSound: Boolean = soundUrl.isDefined

  def playSound(): Unit = {
    if (hasSound) SoundUtil.playSound(soundUrl.get)
  }

  def getImage: BufferedImage =
    image.getOrElse(createPlaceholderImg())

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
    require(choice >= 0 && choice < choices.size,
      s"choice index $choice out of range; size is ${choices.size}")
    getChoices(choice).destinationScene
  }

  /** @return true if there are more than one choice for the user to select from. */
  def hasChoices: Boolean = !choices.isEmpty
  def getChoices: Seq[Choice] = choices.choices

  /** @return true if media is consistent: both image and sound, or neither (text-only scene).
    *         If only one is present, prints a message and returns false.
    */
  def verifyMedia: Boolean = {
    val hasImage = image.isDefined
    if (hasImage == hasSound) true
    else {
      System.out.print(s"scene: $name")
      if (!hasImage) System.out.print(" missing image")
      if (!hasSound) System.out.print(" missing sound")
      println()
      false
    }
  }

  def print: String = {
    val header = s"\n $description\n"
    if (choices.isEmpty) header
    else header + choices.choices.zipWithIndex.map {
      case (c, i) => s"${i + 1}) ${c.description}"
    }.mkString("\n")
  }

  /** @return the text and choices. */
  override def toString: String = this.name
}
