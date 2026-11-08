package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, XmlScriptImporter}
import org.scalatest.funsuite.AnyFunSuite
import org.w3c.dom.Document

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory

class XmlScriptRoundTripSuite extends AnyFunSuite {

  private def parseXml(xml: String): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
    // Exported files may reference a DTD that is not on the classpath.
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    factory.setFeature("http://xml.org/sax/features/validation", false)
    val builder = factory.newDocumentBuilder()
    builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
  }

  private val minimalScript: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<script title="T" name="myname" author="a" date="d">
      |  <scene name="start">
      |    <description>Begin</description>
      |    <choices>
      |      <choice description="Leave" resultScene="end"/>
      |      <choice description="Loop" resultScene="start"/>
      |    </choices>
      |  </scene>
      |  <scene name="end">
      |    <description>Done</description>
      |  </scene>
      |</script>""".stripMargin

  test("script import → export → re-import preserves scenes, descriptions, and destinations") {
    val original = XmlScriptImporter(parseXml(minimalScript), "").getStory
    val out = Files.createTempFile("bb4-script-roundtrip-", ".xml").toFile
    out.deleteOnExit()

    StoryExporter(original).saveTo(out.getAbsolutePath)
    val exportedXml = new String(Files.readAllBytes(out.toPath), StandardCharsets.UTF_8)
    val reimported = XmlScriptImporter(parseXml(exportedXml), "").getStory

    assert(reimported.name == original.name)
    assert(reimported.getAllSceneNames == original.getAllSceneNames)
    for (name <- original.getAllSceneNames) {
      val before = original.getSceneMap.get(name)
      val after = reimported.getSceneMap.get(name)
      assert(after.description == before.description, s"description mismatch for $name")
      assert(
        after.getChoices.map(c => (c.description, c.destinationScene)) ==
          before.getChoices.map(c => (c.description, c.destinationScene)),
        s"choices mismatch for $name")
    }
  }
}
