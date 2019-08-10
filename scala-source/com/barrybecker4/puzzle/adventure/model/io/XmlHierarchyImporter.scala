// Copyright by Barry G. Becker, 2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene, Story}
import org.w3c.dom.{Document, Node, NodeList}
import com.barrybecker4.puzzle.adventure.model.Scene.{loadImage, loadSound}
import XmlHierarchyImporter._
import scala.collection.mutable.ArrayBuffer


object XmlHierarchyImporter {

  val DTD = "hierarchy"

  // name of special node that is put at the rot if there is not a single root
  val FAKE_ROOT = "fake_root"

  /** return the description of available, else use the label */
  private def getNodeDesc(sceneNode: Node): String = {
    val label = DomUtil.getAttribute(sceneNode, "label")
    DomUtil.getAttribute(sceneNode, "description", label)
  }
}

/**
  * Import a story from an XML document with hierarchy.dtd.
  * @param document containing the scene data
  * @author Barry Becker
  */
case class XmlHierarchyImporter(document: Document, resourcePath: String) extends XmlImporter {

  private var idToLabelMap = Map[String, String]()

  private val story: Story = new Story(
    DomUtil.getAttribute(document.getDocumentElement, "title"),
    DTD,
    DomUtil.getAttribute(document.getDocumentElement, "author"),
    DomUtil.getAttribute(document.getDocumentElement, "date"),
    resourcePath, DTD,
    extractScenesFromDoc(document, resourcePath + DomUtil))

  def getStory: Story = story

  /**
    * The child nodes are embedded in their parents like this:
    * <node id="yonkyo_entrance" label="yonkyo entrance" description="foo">
    *   <node id="yonkyo_ura" label="yonkyo ura">
    *     <node id="yonkyo_pin_standing" label="yonkyo pin (standing)"/>
    *     </node>
    *   <node id="yonkyo_omote" label="yonkyo omote">
    *     <use ref="yonkyo_pin_standing" />
    *   </node>
    *   <use ref="sankyo_like_continuance_omote"/>
    * </node>
    * Note that "use" nodes refer to existing nodes in the hierarchy
    * Note that the description is optional. Use label if not specified.
    * @param document doc to extract from
    * @param resourcePath where the media is
    * @return all the scenes in a flat array
    */
  protected def extractScenesFromDoc(document: Document, resourcePath: String): Array[Scene] = {
    val root = document.getDocumentElement
    val children = root.getChildNodes
    val scenes = new ArrayBuffer[Scene]()

    // since there can be only one start node, if there is more than one
    // child at the root, add a fake parent for those nodes.
    if (children.getLength > 1) {
      val rootNode = new Scene(FAKE_ROOT,
        "-", None, new ChoiceList(getChoices(children)),
        None, None,
        true)
      scenes.append(rootNode)

      var i = 0
      while (i < children.getLength) {
        appendScenesRootedAt(children.item(i), scenes, isFirst = false)
        i += 1
      }
    }
    else appendScenesRootedAt(children.item(0), scenes, isFirst = true)

    scenes.toArray
  }

  /** Append the scene for the specified sceneNode and all of its children.
    */
  def appendScenesRootedAt(sceneNode: Node,
                           scenes: ArrayBuffer[Scene], isFirst: Boolean) {
    // first append the root node
    val name = DomUtil.getAttribute(sceneNode, "id")
    val label = DomUtil.getAttribute(sceneNode, "label")
    val rootScene = new Scene(name,
      getNodeDesc(sceneNode),
      Some(label), new ChoiceList(getChoices(sceneNode.getChildNodes)),
      loadSound(name, resourcePath), loadImage(name, resourcePath),
      isFirst)
    idToLabelMap += name -> label
    scenes.append(rootScene)

    // then append all of its children
    var i = 0
    val children = sceneNode.getChildNodes
    while (i < children.getLength) {
      appendScenesRootedAt(children.item(i), scenes, isFirst = false)
      i += 1
    }
  }

  /** The choices will be the labels and ids of all the children.
    * @return extracted choices from a sceneNode.
    */
  private def getChoices(children: NodeList): Seq[Choice] = {
    var choices: Seq[Choice] = Seq()
    for (i <- 0 until children.getLength) {
      val child = children.item(i)
      choices :+= createChoice(child)
    }
    choices
  }

  private def createChoice(node: Node): Choice = {

    node.getNodeName match {
      case "node" =>
        val id = DomUtil.getAttribute(node, "id")
        Choice(DomUtil.getAttribute(node, "label"), id)
      case "use" =>
        val id = DomUtil.getAttribute(node, "ref")
        Choice(idToLabelMap(id), id)
      case _ =>
        throw new IllegalArgumentException("Unexpected tag: " + node.getNodeName)
    }
  }
}
