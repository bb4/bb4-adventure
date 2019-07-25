// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene, Story}
import org.w3c.dom.{Document, Node}
import com.barrybecker4.puzzle.adventure.model.Scene.{loadImage, loadSound}
import XmlScriptImporter._


object XmlScriptImporter {
  val DTD = "script"

  def createScene(sceneNode: Node, resourcePath: String, isFirst: Boolean): Scene = {
    val name = DomUtil.getAttribute(sceneNode, "name")

    new Scene(name,
      sceneNode.getFirstChild.getTextContent,
      Some(new ChoiceList(getChoices(sceneNode))),
      loadSound(name, resourcePath),
      loadImage(name, resourcePath),
      isFirst)
  }

  /** if there are choices they will be the second element (right after description).
    * @return extracted choices from a sceneNode.
    */
  private def getChoices(sceneNode: Node): Seq[Choice] = {
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
        choices :+= createChoice(choiceList.item(i))
        i += 1
      }
    }
    choices
  }

  private def createChoice(choiceNode: Node): Choice = {
    Choice(DomUtil.getAttribute(choiceNode, "description"),
      DomUtil.getAttribute(choiceNode, "resultScene"))
  }
}

/**
  * Import a story from an XML document with script DTD.
  *
  * @param document containing the scene data
  * @author Barry Becker
  */
case class XmlScriptImporter(document: Document, resourcePath: String) {

  private val story: Story = new Story(
    DomUtil.getAttribute(document.getDocumentElement, "title"),
    DomUtil.getAttribute(document.getDocumentElement, "name"),
    DomUtil.getAttribute(document.getDocumentElement, "author"),
    DomUtil.getAttribute(document.getDocumentElement, "date"),
    resourcePath, DTD,
    extractScenesFromDoc(document, resourcePath))

  def getStory: Story = story

  private def extractScenesFromDoc(document: Document, resourcePath: String): Array[Scene] = {
    val root = document.getDocumentElement
    val children = root.getChildNodes
    val scenes = new Array[Scene](children.getLength)
    var i = 0
    while (i < children.getLength) {
      scenes(i) = createScene(children.item(i), resourcePath, i == 0)
      i += 1
    }
    scenes
  }
}
