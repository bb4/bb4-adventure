// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import com.barrybecker4.common.xml.DomUtil
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.Element


/**
  * A choice that you can make in a scene to select the next scene.
  * @author Barry Becker
  */
case class Choice(description: String, var destinationScene: String) {

  def this(choiceNode: Node) {
    this(DomUtil.getAttribute(choiceNode, "description"),
      DomUtil.getAttribute(choiceNode, "resultScene"))
  }

  /** Factory method to create a choice DOM element.
    * @return the choice instance.
    */
  def createElement(document: Document): Element = {
    val choiceElem = document.createElement("choice")
    choiceElem.setAttribute("description", description)
    choiceElem.setAttribute("resultScene", destinationScene)
    choiceElem
  }
}
