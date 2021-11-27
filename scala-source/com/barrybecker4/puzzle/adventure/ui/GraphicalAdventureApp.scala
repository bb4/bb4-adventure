// Copyright by Barry G. Becker, 2000-2021. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import GraphicalAdventureConsts.PASSWORD
import com.barrybecker4.puzzle.adventure.model.io.StoryImporter
import com.barrybecker4.puzzle.adventure.ui.GraphicalAdventure

/**
  * Run your own adventure story.
  * This version runs the adventure in Graphical mode (with images and sound).
  * @see TextAdventure
  * @author Barry Becker
  */
object GraphicalAdventureApp {

  def main(args: Array[String]): Unit = {
    val theArgs = if (args == null) Array[String]() else args
    new GraphicalAdventure(Array(), new StoryImporter(theArgs).getStory, PASSWORD)
  }

}

object GraphicalAdventureConsts {
  /** The top secret password - don't tell anyone.
    * This could be Base64 encoded or encrypted to make more secure.
    */
  private[ui] val PASSWORD = "ludlow" //NON-NLS
}
