// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}
import org.w3c.dom.Document
import XmlScriptImporter.DTD


object XmlScriptImporter {
  val DTD = "script"
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
      scenes(i) = new Scene(children.item(i), resourcePath, i == 0)
      i += 1
    }
    scenes
  }
}
