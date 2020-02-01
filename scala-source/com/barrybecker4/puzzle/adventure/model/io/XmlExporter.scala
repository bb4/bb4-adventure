// Copyright by Barry G. Becker, 2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}
import org.w3c.dom.Document

abstract class XmlExporter(story: Story) {

  /** Write the story document back to xml.
    * @param destFileName file to write to.
    */
  def saveTo(destFileName: String): Unit = {
    try {
      val document = createStoryDocument
      DomUtil.writeXMLFile(destFileName, document, story.rootTag + ".dtd")
      println("done saving.")
    } catch {
      case e: Exception => throw new IllegalStateException("Could not save. ", e)
    }
  }

  /** @return the story document based on the current state of the story. */
  protected def createStoryDocument: Document

}
