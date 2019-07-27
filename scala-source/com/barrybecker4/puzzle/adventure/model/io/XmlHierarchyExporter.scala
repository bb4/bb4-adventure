// Copyright by Barry G. Becker, 2018-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model.io

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.model.{Choice, Scene, Story}
import org.w3c.dom.{Document, Element}
import scala.collection.mutable

/**
  * @param story the story to export to XML format with "hierarchy" dtd.
  */
class XmlHierarchyExporter(story: Story) extends XmlExporter(story) {

 /** @return the story document based on the current state. */
  override protected def createStoryDocument: Document = {
    val document = DomUtil.createNewDocument
    val rootElement = document.createElement(story.rootTag)
    rootElement.setAttribute("author", story.author)
    rootElement.setAttribute("title", story.title)
    rootElement.setAttribute("date", story.date)
    rootElement.setAttribute("imgpath", "techniques/images") // hardcoded for aikido
    document.appendChild(rootElement)

    // iterate through scenes in scenemap
    // if we ever encounter a scene at the top level that has already been added, skip it
    val visitedScenes = mutable.Set[Scene]()
    val sceneMap = story.getSceneMap

    for (sceneName <- sceneMap.sceneNames) {
      if (sceneName != XmlHierarchyImporter.FAKE_ROOT) {
        val scene: Scene = sceneMap.get(sceneName)
        if (!visitedScenes.contains(scene)) {
          addSceneToDom(rootElement, document, scene, visitedScenes)
        }
      }
    }
    document
  }

  /**
    * Export to a structure like this:
    * * <node id="yonkyo_entrance" label="yonkyo entrance" description="foo">
    * *   <node id="yonkyo_ura" label="yonkyo ura">
    * *     <node id="yonkyo_pin_standing" label="yonkyo pin (standing)"/>
    * *     </node>
    * *   <node id="yonkyo_omote" label="yonkyo omote">
    * *     <use ref="yonkyo_pin_standing" />
    * *   </node>
    * *   <use ref="sankyo_like_continuance_omote"/>
    * * </node>
    * * Note that "use" nodes refer to existing nodes in the hierarchy
    * * Note that the description is optional. Use label if not specified.
    *
    * // for each scene, recursively create child nodes corresponding to choices
    * //   if a scene has already been added earlier, add a "ref" node for it
    *
    * @param parentElement the parent to add child scenes to
    * @param document the document to which to append this scene as a child.
    * @param scene the scene to add along with all its children (recursively)
    * @param visitedScenes don't add duplicate scenes. Instead add ref nodes
    */
  private def addSceneToDom(parentElement: Element,
                          document: Document, scene: Scene,
                          visitedScenes: mutable.Set[Scene]): Unit = {

    val sceneMap = story.getSceneMap

    if (visitedScenes.contains(scene)) {
      val refElem = document.createElement("use")
      refElem.setAttribute("ref", scene.name)
      parentElement.appendChild(refElem)
    } else {
      val sceneElem = document.createElement("node")
      val name = scene.name
      sceneElem.setAttribute("id", name)
      if (scene.label.isEmpty) {
        throw new IllegalStateException("Could not find label for scene " + name)
      } else sceneElem.setAttribute("label", scene.label.get)
      sceneElem.setAttribute("description", scene.description)

      var i = 0
      while (i < scene.choices.get.size) {
        val choice: Choice = scene.getChoices(i)
        addSceneToDom(sceneElem, document, sceneMap.get(choice.destinationScene), visitedScenes)
        i += 1
      }
      parentElement.appendChild(sceneElem)
      visitedScenes.add(scene)
    }
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
