// Copyright by Barry G. Becker, 2000-2021. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import java.io.File
import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, StoryImporter}


/**
  * Use this application to edit the xml file containing all aikido techniques.
  * See https://github.com/bb4/bb4-aikido-app.
  * @author Barry Becker
  */
object AikidoEditor {

  def main(args: Array[String]): Unit = {

    // Use this "../bb4-aikido-app/deployment/techniques/" if you want images to show too
    val AIKIDO_RESOURCE_ROOT = "com/barrybecker4/puzzle/adventure/stories/aikido/"

    val theArgs =
      if (args == null || args.isEmpty) Array[String]("techniques.xml", AIKIDO_RESOURCE_ROOT)
      else args

    new GraphicalAdventure(Array(),
      new StoryImporter(theArgs).getStory, null)
  }

}
