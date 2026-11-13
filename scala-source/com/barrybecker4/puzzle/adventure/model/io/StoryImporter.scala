// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import java.io.File
import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.Story
import org.w3c.dom.Document


object StoryImporter {

  /** all the stories need to be stored at this location */
  val DEFAULT_STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/ludlow/"
  val DEFAULT_FILE = "ludlowScript.xml"

  def fromArgs(args: Array[String]): StoryImporter = {
    val fileName = if (args != null && args.length > 0) args(0) else DEFAULT_FILE
    val fileRoot = if (args != null && args.length > 1) args(1) else DEFAULT_STORIES_ROOT
    fromDefaults(fileName, fileRoot)
  }

  /**
    * Resource root for a story file chosen from disk: the file's parent directory.
    * Does not substring-match on "com/" — that was brittle and crashed when absent.
    */
  def resourcePathFor(file: File): String = {
    val parent = file.getParent
    if (parent == null)
      throw new IllegalArgumentException(s"Story file must have a parent directory: $file")
    parent + File.separator
  }

  def fromFile(file: File): StoryImporter =
    DomUtil.parseXMLFile(file) match {
      case Some(document) =>
        new StoryImporter(document, resourcePathFor(file))
      case None =>
        throw new IllegalArgumentException(s"Could not parse XML file: $file")
    }

  def fromDefaults(
      fileName: String = DEFAULT_FILE,
      fileRoot: String = DEFAULT_STORIES_ROOT
  ): StoryImporter =
    new StoryImporter(DomUtil.parseXML(FileUtil.getURL(fileRoot + fileName)), fileRoot)

  def fromDocument(docAndPath: (Document, String)): StoryImporter =
    new StoryImporter(docAndPath._1, docAndPath._2)
}

/**
  * Import a story from an XML document.
  * The XML document can either use script.dtd or hierarchy.dtd.
  * @param document containing the scene data
  * @author Barry Becker
  */
class StoryImporter private (document: Document, resourcePath: String) {

  println("The resource root is : " + resourcePath)
  private val schemaType = document.getDocumentElement.getTagName

  private val story: Story = schemaType match {
    case XmlScriptImporter.DTD =>
      XmlScriptImporter(document, resourcePath).getStory
    case XmlHierarchyImporter.DTD =>
      XmlHierarchyImporter(document, resourcePath).getStory
    case _ =>
      throw new IllegalArgumentException("Unexpected dtd: " + schemaType)
  }

  def getStory: Story = story
}
