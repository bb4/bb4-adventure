package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.io.{StoryImporter, XmlHierarchyImporter}
import org.scalatest.funsuite.AnyFunSuite
import org.w3c.dom.Document

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

class XmlHierarchyImporterSuite extends AnyFunSuite {

  private def parseXml(xml: String): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(false)
    val builder = factory.newDocumentBuilder()
    builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
  }

  /** Parent P has child A (which defines X) then a sibling use of X. */
  private val forwardUseHierarchy: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<hierarchy title="H" name="h" author="a" date="d">
      |  <node id="P" label="parent" description="Parent">
      |    <node id="A" label="alpha" description="Alpha">
      |      <node id="X" label="ex" description="Ex"/>
      |    </node>
      |    <use ref="X"/>
      |  </node>
      |</hierarchy>""".stripMargin

  private val unknownUseHierarchy: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<hierarchy title="H" name="h" author="a" date="d">
      |  <node id="P" label="parent" description="Parent">
      |    <node id="A" label="alpha" description="Alpha"/>
      |    <use ref="missing_id"/>
      |  </node>
      |</hierarchy>""".stripMargin

  test("XmlHierarchyImporter resolves forward use refs under earlier sibling subtrees") {
    val story = XmlHierarchyImporter(parseXml(forwardUseHierarchy), "").getStory
    assert(story.getSceneMap.contains("P"))
    assert(story.getSceneMap.contains("A"))
    assert(story.getSceneMap.contains("X"))
    val parentChoices = story.getSceneMap.get("P").getChoices
    assert(parentChoices.map(_.destinationScene) == Seq("A", "X"))
    assert(parentChoices.find(_.destinationScene == "X").get.description == "ex")
  }

  test("XmlHierarchyImporter fails clearly for unknown use ref") {
    val ex = intercept[IllegalArgumentException] {
      XmlHierarchyImporter(parseXml(unknownUseHierarchy), "").getStory
    }
    assert(ex.getMessage.toLowerCase.contains("missing_id") ||
      ex.getMessage.toLowerCase.contains("unknown"))
  }

  private val aikidoResourceRoot =
    "com/barrybecker4/puzzle/adventure/stories/aikido/"

  test("XmlHierarchyImporter loads shipped techniques.xml with sibling forward use refs") {
    val story = StoryImporter.fromDefaults("techniques.xml", aikidoResourceRoot).getStory
    assert(story.getSceneMap.contains("katate_dori"))
    assert(story.getSceneMap.contains("left_sumi_entrance_low"))
    val parentChoices = story.getSceneMap.get("katate_dori").getChoices
    assert(
      parentChoices.exists(c =>
        c.destinationScene == "left_sumi_entrance_low" && c.description.nonEmpty))
  }
}
