package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList, Scene, Story}
import com.barrybecker4.puzzle.adventure.model.io.{XmlHierarchyExporter, XmlHierarchyImporter}
import org.scalatest.funsuite.AnyFunSuite
import org.w3c.dom.{Document, Element, Node}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

class XmlHierarchyExporterSuite extends AnyFunSuite {

  private class TestableExporter(story: Story) extends XmlHierarchyExporter(story) {
    def exportedDocument: Document = createStoryDocument
  }

  private def parseXml(xml: String): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
    val builder = factory.newDocumentBuilder()
    builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
  }

  private def childElements(parent: Node): Seq[Element] =
    (0 until parent.getChildNodes.getLength)
      .map(parent.getChildNodes.item)
      .collect { case e: Element => e }

  test("cyclic hierarchy A↔B exports with <use> for the back-edge") {
    val b = new Scene(
      "b",
      "scene b",
      Some("B"),
      new ChoiceList(Seq(Choice("back to a", "a"))),
      None,
      None,
      false)
    val a = new Scene(
      "a",
      "scene a",
      Some("A"),
      new ChoiceList(Seq(Choice("to b", "b"))),
      None,
      None,
      true)
    val story = new Story("Cycle", "cycle", "author", "date", "", "hierarchy", Array(a, b))

    val doc = new TestableExporter(story).exportedDocument
    val root = doc.getDocumentElement
    val topNodes = childElements(root).filter(_.getNodeName == "node")
    assert(topNodes.size == 1)
    assert(topNodes.head.getAttribute("id") == "a")

    val aChildren = childElements(topNodes.head)
    assert(aChildren.size == 1)
    assert(aChildren.head.getNodeName == "node")
    assert(aChildren.head.getAttribute("id") == "b")

    val bChildren = childElements(aChildren.head)
    assert(bChildren.size == 1)
    assert(bChildren.head.getNodeName == "use")
    assert(bChildren.head.getAttribute("ref") == "a")
  }

  test("hierarchy export preserves imported imgpath") {
    val xml =
      """<?xml version="1.0" encoding="UTF-8"?>
        |<hierarchy title="T" author="a" date="d" imgpath="images/techniques/">
        |  <node id="start" label="Start" description="Begin">
        |    <node id="end" label="End" description="Done"/>
        |  </node>
        |</hierarchy>""".stripMargin

    val imported = XmlHierarchyImporter(parseXml(xml), "").getStory
    assert(imported.imgPath == "images/techniques/")

    val exported = new TestableExporter(imported).exportedDocument
    assert(exported.getDocumentElement.getAttribute("imgpath") == "images/techniques/")
  }
}
