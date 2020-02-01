// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import java.io.File
import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.io.StoryImporter._
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}
import org.w3c.dom.Document


object StoryImporter {

  /** all the stories need to be stored at this location */
  val DEFAULT_STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/ludlow/"
  val DEFAULT_FILE = "ludlowScript.xml"
}

/**
  * Import a story from an XML document.
  * The XML document can either use script.dtd or hierarchy.dtd.
  * @param document containing the scene data
  * @author Barry Becker
  */
case class StoryImporter(document: Document, resourcePath: String) {

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

  /** Construct an adventure given an xml document object
    * @param docAndPath (doc containing the scene data, resourcePath)
    */
  def this(docAndPath: (Document, String)) {
    this(docAndPath._1, docAndPath._2)
  }

  /**
    * If args[0] does not have the name of the document to use, use a default.
    * The media files are expected to be relative to the script file.
    * @param fileName the end of the file path. Something like "ludlowScript.xml".
    * @param fileRoot the beginning of the path. Something like "com/user/puzzle/adventure/stories/ludlow/"
    * @return (the loaded Document that contains the adventure, resource root).
    */
  def this(fileName: String = DEFAULT_FILE,
    fileRoot: String = DEFAULT_STORIES_ROOT) {
    this(DomUtil.parseXML(FileUtil.getURL(fileRoot + fileName)), fileRoot)
  }

  def this(file: File) {
    this(DomUtil.parseXMLFile(file), file.getParent + File.separator)
  }

  /**
    * If args[0] does not have the name of the document to use, use a default.
    * @param args command line args (0 or 1 if name of xml doc is specified.)
    * @return (the loaded Document that contains the adventure, fileRoot).
    */
  def this(args: Array[String]) {
    this(
      if (args != null && args.length > 0) args(0) else DEFAULT_FILE,
      if (args != null && args.length > 1) args(1) else DEFAULT_STORIES_ROOT
    )
  }

  def getStory: Story = story
}
