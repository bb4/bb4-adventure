// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure

import java.util.InputMismatchException
import java.util.Scanner


/**
  * Run your own adventure story.
  * This version runs the adventure in text only mode.
  * @see GraphicalAdventure
  * @author Barry Becker
  */
object TextAdventure extends App {

  val document = Story.importStoryDocument(args)
  val story = new Story(document)
  val scanner = new Scanner(System.in).useDelimiter("\n")
  do {
    val currentScene = story.getCurrentScene
    println(currentScene.print)
    val nextSceneIndex = getNextSceneIndex(currentScene, scanner)
    story.advanceScene(nextSceneIndex)
  } while (!story.isOver)
  scanner.close()

  /** Retrieve the selection from the player using the scanner.
    * @return the next scene to advance to.
    */
  private def getNextSceneIndex(scene: Scene, scanner: Scanner) = {
    var sceneIndex = -1
    if (scene.hasChoices) {
      var nextInt = -1
      var valid = true
      while (nextInt < 1) {
        try
          nextInt = scanner.nextInt
        catch {
          case e: InputMismatchException =>
            valid = false
            scanner.next
        }
        if (nextInt < 1 || !valid)
          println("You must enter a number from among the choices.")
      }
      sceneIndex = nextInt - 1
    }
    sceneIndex
  }
}
