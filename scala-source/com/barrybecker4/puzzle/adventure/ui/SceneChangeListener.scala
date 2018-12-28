// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

/**
  * Called when you advance to a different scene of the story.
  * @author Barry Becker
  */
trait SceneChangeListener {

  /** @param selectionIndex the selected choice leading to the next scene in the story. */
  def sceneChanged(selectionIndex: Int): Unit

}
