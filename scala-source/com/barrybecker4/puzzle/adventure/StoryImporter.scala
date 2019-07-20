// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.StoryImporter._
import org.w3c.dom.Document
import scala.collection.{Set, mutable}


object StoryImporter {

  /** all the stories need to be stored at this location */
  val DEFAULT_STORIES_ROOT = "com/barrybecker4/puzzle/adventure/stories/ludlow/"
  val DEFAULT_FILE = "ludlowScript.xml"

  private def extractScenesFromDoc(document: Document, resourcePath: String): Array[Scene] = {
    val root = document.getDocumentElement
    println(s"schema name: ${root.getTagName}")

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

/**
  * Import a story from an XML document.
  * The XML document can either use script.dtd or hierarchy.dtd.
  * @param document containing the scene data
  *
  * @author Barry Becker
  */
case class StoryImporter(document: Document, resourcePath: String) {

  private val story: Story = new Story(DomUtil.getAttribute(document.getDocumentElement, "title"),
    DomUtil.getAttribute(document.getDocumentElement, "name"),
    DomUtil.getAttribute(document.getDocumentElement, "author"),
    DomUtil.getAttribute(document.getDocumentElement, "date"),
    resourcePath, document.getDocumentElement.getTagName,
    extractScenesFromDoc(document, resourcePath))

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
