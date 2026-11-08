package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.*
import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, XmlHierarchyExporter, XmlHierarchyImporter}
import org.scalatest.funsuite.AnyFunSuite
import org.w3c.dom.Document

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
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

  private def cyclicStory(): Story = {
    val b = new Scene(
      "B",
      "b desc",
      Some("bee"),
      new ChoiceList(Seq(Choice("back to A", "A"))),
      None,
      None)
    val a = new Scene(
      "A",
      "a desc",
      Some("ay"),
      new ChoiceList(Seq(Choice("to B", "B"))),
      None,
      None)
    new Story("Cycle", "cycle", "a", "d", "", XmlHierarchyImporter.DTD, Array(a, b))
  }

  test("hierarchy export of A→B→A completes and emits use for the back-edge") {
    val story = cyclicStory()
    val out = Files.createTempFile("bb4-hierarchy-cycle-", ".xml").toFile
    out.deleteOnExit()

    StoryExporter(story).saveTo(out.getAbsolutePath)

    val xml = new String(Files.readAllBytes(out.toPath), StandardCharsets.UTF_8)
    assert(xml.contains("<use") && xml.contains("""ref="A""""),
      s"expected use back-edge to A in:\n$xml")
    assert(xml.contains("""id="A""""))
    assert(xml.contains("""id="B""""))
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
