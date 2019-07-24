// Copyright by Barry G. Becker, 2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}
import org.w3c.dom.Document
import XmlHierarchyImporter.DTD

object XmlHierarchyImporter {
  val DTD = "hierarchy"
}

/**
  * Import a story from an XML document with hierarchy.dtd.
  *
  * @param document containing the scene data
  * @author Barry Becker
  */
case class XmlHierarchyImporter(document: Document, resourcePath: String) {

  private val story: Story = new Story(
    DomUtil.getAttribute(document.getDocumentElement, "title"),
    DTD,
    DomUtil.getAttribute(document.getDocumentElement, "author"),
    DomUtil.getAttribute(document.getDocumentElement, "date"),
    resourcePath, DTD,
    extractScenesFromDoc(document, resourcePath + DomUtil))

  def getStory: Story = story

  // id, label, description

  private def extractScenesFromDoc(document: Document, resourcePath: String): Array[Scene] = {
    val root = document.getDocumentElement
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
