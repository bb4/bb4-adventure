package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.io.StoryImporter
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class StoryImporterPathSuite extends AnyFunSuite {

  test("resourcePathFor uses parent directory for classpath-style story tree") {
    val file = new File(
      "/projects/bb4-adventure/source/com/barrybecker4/puzzle/adventure/stories/ludlow/file.xml")
    val expected =
      "/projects/bb4-adventure/source/com/barrybecker4/puzzle/adventure/stories/ludlow" +
        File.separator
    assert(StoryImporter.resourcePathFor(file) == expected)
  }

  test("resourcePathFor uses absolute parent when path has no com segment") {
    val file = new File("/tmp/mystory/file.xml")
    val expected = "/tmp/mystory" + File.separator
    assert(StoryImporter.resourcePathFor(file) == expected)
  }

  test("resourcePathFor rejects a file with no parent directory") {
    val file = new File("orphan.xml")
    val thrown = intercept[IllegalArgumentException] {
      StoryImporter.resourcePathFor(file)
    }
    assert(thrown.getMessage.contains("parent directory"))
  }
}
