// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, Scene, Story}
import org.w3c.dom.{Document, Element}


class XmlHierarchyExporter(story: Story) {

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
  private def createStoryDocument = {
    val document = DomUtil.createNewDocument
    val rootElement = document.createElement(story.rootTag)
    rootElement.setAttribute("author", story.author)
    rootElement.setAttribute("name", story.name)
    rootElement.setAttribute("date", story.date)
    rootElement.setAttribute("title", story.title)
    document.appendChild(rootElement)
    for (sceneName <- story.getSceneMap.sceneNames) {
      val scene: Scene = story.getSceneMap.get(sceneName)
      appendSceneToDocument(document, scene)
    }
    document
  }


  /** @param document the document to which to append this scene as a child. */
  private def appendSceneToDocument(document: Document, scene: Scene): Unit = {
    val sceneElem = document.createElement("scene")

    val name = scene.name
    sceneElem.setAttribute("name", name)
    val descElem = document.createElement("description")
    descElem.setTextContent(scene.text)
    sceneElem.appendChild(descElem)
    val choicesElem = document.createElement("choices")
    sceneElem.appendChild(choicesElem)
    var i = 0
    while (i < scene.choices.get.size) {
      val choice: Choice = scene.getChoices(i)
      choicesElem.appendChild(createChoiceElement(document, choice))
      i += 1
    }
    val rootElement = document.getDocumentElement
    rootElement.appendChild(sceneElem)
  }

  /** Factory method to create a choice DOM element.
    * @return the choice instance.
    */
  private def createChoiceElement(document: Document, choice: Choice): Element = {
    val choiceElem = document.createElement("choice")
    choiceElem.setAttribute("description", choice.description)
    choiceElem.setAttribute("resultScene", choice.destinationScene)
    choiceElem
  }

}
