package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.puzzle.adventure.model.Story

/**
  * @param story the story to export
  */
case class StoryExporter(story: Story) {

  /** Write the story document back to xml.
    * @param destFileName file to write to.
    */
  def saveTo(destFileName: String): Unit = {
    story.rootTag match {
      case XmlScriptImporter.DTD => new XmlScriptExporter(story).saveTo(destFileName)
      case XmlHierarchyImporter.DTD => new XmlHierarchyExporter(story).saveTo(destFileName)
      case _ => throw new IllegalArgumentException("Unexpected root tag name" + story.rootTag)
    }
    println("done saving.")
  }
}
