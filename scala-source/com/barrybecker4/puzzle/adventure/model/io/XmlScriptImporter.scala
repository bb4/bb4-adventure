// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene, Story}
import org.w3c.dom.{Document, Node}
import com.barrybecker4.puzzle.adventure.model.Scene.{loadImage, loadSound}
import XmlScriptImporter._

object XmlScriptImporter {
  val DTD = "script"

  private def findFirstChildElement(parent: Node, tag: String): Option[Node] =
    DomNodeUtil.childElementNodes(parent.getChildNodes).find(_.getNodeName == tag)

  private def descriptionForScene(sceneNode: Node): String =
    findFirstChildElement(sceneNode, "description")
      .map(_.getTextContent)
      .getOrElse("")

  def createScene(sceneNode: Node, resourcePath: String, isFirst: Boolean): Scene = {
    val name = DomUtil.getAttribute(sceneNode, "name")
    new Scene(
      name,
      descriptionForScene(sceneNode),
      None,
      new ChoiceList(getChoices(sceneNode)),
      loadSound(name, resourcePath),
      loadImage(name, resourcePath),
      isFirst)
  }

  /** Choices live under a `choices` element; only `choice` element children are used.
    */
  private def getChoices(sceneNode: Node): Seq[Choice] =
    findFirstChildElement(sceneNode, "choices") match {
      case Some(choicesNode) =>
        DomNodeUtil.childElementNodes(choicesNode.getChildNodes)
          .filter(_.getNodeName == "choice")
          .map(createChoice)
      case None =>
        Seq.empty
    }

  private def createChoice(choiceNode: Node): Choice =
    Choice(
      DomUtil.getAttribute(choiceNode, "description"),
      DomUtil.getAttribute(choiceNode, "resultScene"))
}

/**
  * Import a story from an XML document with script DTD.
  *
  * @param document containing the scene data
  * @author Barry Becker
  */
case class XmlScriptImporter(document: Document, resourcePath: String) extends XmlImporter {

  private val story: Story = new Story(
    DomUtil.getAttribute(document.getDocumentElement, "title"),
    DomUtil.getAttribute(document.getDocumentElement, "name"),
    DomUtil.getAttribute(document.getDocumentElement, "author"),
    DomUtil.getAttribute(document.getDocumentElement, "date"),
    resourcePath, DTD,
    extractScenesFromDoc(document))

  def getStory: Story = story

  protected def extractScenesFromDoc(document: Document): Array[Scene] = {
    val root = document.getDocumentElement
    val sceneNodes = DomNodeUtil.childElementNodes(root.getChildNodes).filter(_.getNodeName == "scene")
    sceneNodes.zipWithIndex.map { case (node, idx) =>
      createScene(node, resourcePath, idx == 0)
    }.toArray
  }
}
