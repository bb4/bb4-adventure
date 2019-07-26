// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, Scene, Story}
import org.w3c.dom.{Document, Element}

/**
  * Export to a structure like this:
  * <node id="yonkyo_entrance" label="yonkyo entrance" description="foo">
  *   <node id="yonkyo_ura" label="yonkyo ura">
  *     <node id="yonkyo_pin_standing" label="yonkyo pin (standing)"/>
  *     </node>
  *   <node id="yonkyo_omote" label="yonkyo omote">
  *     <use ref="yonkyo_pin_standing" />
  *   </node>
  *   <use ref="sankyo_like_continuance_omote"/>
  * </node>
  * Note that "use" nodes refer to existing nodes in the hierarchy
  * Note that the description is optional. Use label if not specified.
  * @param story the story to export to XML format with "hierarchy" dtd.
  */
class XmlHierarchyExporter(story: Story) extends XmlExporter(story) {

  /** @return the story document based on the current state of the story. */
  override protected def createStoryDocument: Document = {
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
