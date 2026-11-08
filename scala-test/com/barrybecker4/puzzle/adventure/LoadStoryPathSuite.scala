package com.barrybecker4.puzzle.adventure

import com.barrybecker4.puzzle.adventure.ui.GraphicalAdventure
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class LoadStoryPathSuite extends AnyFunSuite {

  test("resource folder helper keeps classpath-style path under com/") {
    val parent = s"somewhere${File.separator}com${File.separator}barrybecker4${File.separator}puzzle${File.separator}adventure${File.separator}stories${File.separator}ludlow"
    val folder = GraphicalAdventure.resourceFolderFromParent(parent)
    assert(folder.startsWith("com" + File.separator))
    assert(folder.contains("stories" + File.separator + "ludlow"))
  }

  test("resource folder helper does not throw for path without com/ segment") {
    val parent = s"${File.separator}tmp${File.separator}mystory"
    val folder = GraphicalAdventure.resourceFolderFromParent(parent)
    assert(folder == parent)
  }
}
