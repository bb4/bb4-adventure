package com.barrybecker4.puzzle.adventure.model.io

import org.w3c.dom.{Node, NodeList}

/** Shared DOM helpers for story XML importers. */
private[io] object DomNodeUtil {

  def childElementNodes(nodeList: NodeList): Seq[Node] = {
    val buf = scala.collection.mutable.ArrayBuffer.empty[Node]
    var i = 0
    while (i < nodeList.getLength) {
      val n = nodeList.item(i)
      if (n.getNodeType == Node.ELEMENT_NODE) buf += n
      i += 1
    }
    buf.toSeq
  }
}
