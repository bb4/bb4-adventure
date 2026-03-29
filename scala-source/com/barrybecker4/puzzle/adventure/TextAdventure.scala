// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import java.util.Scanner
import scala.util.Try

import com.barrybecker4.puzzle.adventure.model.Scene
import com.barrybecker4.puzzle.adventure.model.io.StoryImporter


/**
  * Run your own adventure story.
  * This version runs the adventure in text only mode.
  * @see GraphicalAdventure
  * @author Barry Becker
  */
object TextAdventure {

  /** Parse a 1-based choice number from a line of input (for tests and REPL-style play). */
  private[adventure] def parseChoiceLine(line: String): Option[Int] =
    Try(line.trim.toInt).toOption

  def main(args: Array[String]): Unit = {
    val story = StoryImporter.fromArgs(args).getStory
    val scanner = new Scanner(System.in).useDelimiter("\n")
    while (!story.isOver) {
      val currentScene = story.getCurrentScene
      println(currentScene.print)
      val nextSceneIndex = getNextSceneIndex(currentScene, () => scanner.nextLine())
      story.advanceScene(nextSceneIndex)
    }
    scanner.close()
  }

  /** @param readLine next line from the player (lazy each attempt)
    * @return 0-based index for [[com.barrybecker4.puzzle.adventure.model.Story.advanceScene]], or -1 if no choices.
    */
  private[adventure] def getNextSceneIndex(scene: Scene, readLine: () => String): Int =
    if (!scene.hasChoices) -1
    else {
      var choice1Based = -1
      while (!scene.isValidChoice(choice1Based)) {
        parseChoiceLine(readLine()) match {
          case Some(i) => choice1Based = i
          case None =>
        }
        if (!scene.isValidChoice(choice1Based))
          println("You must enter a number from among the choices.")
      }
      choice1Based - 1
    }
}
