// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import java.util.InputMismatchException
import java.util.Scanner

import com.barrybecker4.puzzle.adventure.model.Scene
import com.barrybecker4.puzzle.adventure.model.io.StoryImporter


/**
  * Run your own adventure story.
  * This version runs the adventure in text only mode.
  * @see GraphicalAdventure
  * @author Barry Becker
  */
object TextAdventure {

  def main(args: Array[String] ): Unit = {
    val story = new StoryImporter(args).getStory
    val scanner = new Scanner(System.in).useDelimiter("\n")
    while (!story.isOver) {
      val currentScene = story.getCurrentScene
      println(currentScene.print)
      val nextSceneIndex = getNextSceneIndex(currentScene, scanner)
      story.advanceScene(nextSceneIndex)
    }
    scanner.close()
  }


  /** Retrieve the selection from the player using the scanner.
    * @return the next scene to advance to.
    */
  private def getNextSceneIndex(scene: Scene, scanner: Scanner) = {
    var sceneIndex = -1
    if (scene.hasChoices) {
      var nextInt = -1
      var valid = true
      while (!scene.isValidChoice(nextInt)) {
        try
          nextInt = scanner.nextInt
        catch {
          case e: InputMismatchException =>
            valid = false
            scanner.next
        }
        if (!scene.isValidChoice(nextInt) || !valid)
          println("You must enter a number from among the choices.")
      }
      sceneIndex = nextInt - 1
    }
    sceneIndex
  }
}
