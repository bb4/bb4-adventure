// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.model


/**
  * A choice that you can make in a scene to select the next scene.
  * @author Barry Becker
  */
case class Choice(description: String, var destinationScene: String)
