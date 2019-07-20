// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import com.barrybecker4.common.xml.DomUtil

/**
  * @param story the story to export
  */
case class StoryExporter(story: Story) {

  /** Write the story document back to xml.
    * @param destFileName file to write to.
    */
  def saveTo(destFileName: String): Unit = {
    try {
      val document = createStoryDocument
      DomUtil.writeXMLFile(destFileName, document, story.rootElement + " .dtd")
      println("done saving.")
    } catch {
      case e: Exception => throw new IllegalStateException("Could not save. ", e)
    }
  }

  /** @return the story document based on the current state of the story. */
  private def createStoryDocument = {
    val document = DomUtil.createNewDocument
    val rootElement = document.createElement(story.rootElement)
    rootElement.setAttribute("author", story.author)
    rootElement.setAttribute("name", story.name)
    rootElement.setAttribute("date", story.date)
    rootElement.setAttribute("title", story.title)
    document.appendChild(rootElement)
    for (sceneName <- story.getSceneMap.sceneNames) {
      val scene: Scene = story.getSceneMap.get(sceneName)
      scene.appendToDocument(document)
    }
    document
  }

}
