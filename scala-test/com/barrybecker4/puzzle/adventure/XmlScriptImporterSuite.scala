package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.io.XmlScriptImporter
import org.scalatest.funsuite.AnyFunSuite
import org.w3c.dom.Document

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

class XmlScriptImporterSuite extends AnyFunSuite {

  private def parseXml(xml: String): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
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
      |    </choices>
      |  </scene>
      |  <scene name="end">
      |    <description>Done</description>
      |  </scene>
      |</script>""".stripMargin

  test("XmlScriptImporter reads scenes and choices from minimal document") {
    val doc = parseXml(minimalScript)
    val story = XmlScriptImporter(doc, "").getStory
    assert(story.name == "myname")
    assert(story.getSceneMap.contains("start"))
    assert(story.getSceneMap.contains("end"))
    val start = story.getSceneMap.get("start")
    assert(start.description.contains("Begin"))
    assert(start.getChoices.map(_.destinationScene) == Seq("end"))
  }
}
