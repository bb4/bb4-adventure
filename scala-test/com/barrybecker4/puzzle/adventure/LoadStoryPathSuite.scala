package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.model.io.StoryImporter
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

/** Regression coverage for #23 case 8 / #21: loadStory path resolution must not crash. */
class LoadStoryPathSuite extends AnyFunSuite {

  test("resourcePathFor keeps parent for classpath-style path under com/") {
    val file = new File(
      s"somewhere${File.separator}com${File.separator}barrybecker4${File.separator}puzzle" +
        s"${File.separator}adventure${File.separator}stories${File.separator}ludlow${File.separator}file.xml")
    val folder = StoryImporter.resourcePathFor(file)
    assert(folder.endsWith("ludlow" + File.separator))
    assert(folder.contains("com" + File.separator + "barrybecker4"))
  }

  test("resourcePathFor does not throw for path without com/ segment") {
    val file = new File(s"${File.separator}tmp${File.separator}mystory${File.separator}file.xml")
    val folder = StoryImporter.resourcePathFor(file)
    assert(folder == s"${File.separator}tmp${File.separator}mystory" + File.separator)
  }
}
