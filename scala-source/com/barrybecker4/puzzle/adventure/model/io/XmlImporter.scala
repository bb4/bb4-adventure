// Copyright by Barry G. Becker, 2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.puzzle.adventure.model.{Scene, Story}
import org.w3c.dom.Document

/**
  * Import XML file into a Scene
  */
trait XmlImporter {

  def getStory: Story

  /** Uses `resourcePath` from the concrete importer instance for media resolution. */
  protected def extractScenesFromDoc(document: Document): Array[Scene]

}
