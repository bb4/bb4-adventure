// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import java.io.File

import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, StoryImporter}


/**
  * Use this application to edit the xml file containing all aikido techniques.
  * See https://github.com/bb4/bb4-aikido-app.
  * @author Barry Becker
  */
object AikidoEditor extends App {

  val aikidoResourceRoot = "../bb4-aikido-app/deployment/techniques/"
  println("homeDir = " + FileUtil.getHomeDir )

  val techniqueXmlFile: File =
    new File(FileUtil.getHomeDir  + aikidoResourceRoot + "techniques.xml")

  new GraphicalAdventure(Array(),
    new StoryImporter(techniqueXmlFile).getStory, null)
}
