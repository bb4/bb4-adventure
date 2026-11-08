package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.*
import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, XmlHierarchyImporter}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class XmlHierarchyExporterSuite extends AnyFunSuite {

  private def cyclicStory(): Story = {
    val b = new Scene(
      "B",
      "b desc",
      Some("bee"),
      new ChoiceList(Seq(Choice("back to A", "A"))),
      None,
      None,
      false)
    val a = new Scene(
      "A",
      "a desc",
      Some("ay"),
      new ChoiceList(Seq(Choice("to B", "B"))),
      None,
      None,
      true)
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
}
