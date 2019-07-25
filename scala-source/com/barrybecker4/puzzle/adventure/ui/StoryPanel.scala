// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import com.barrybecker4.ui.components.ImageListPanel
import javax.swing.JSplitPane
import javax.swing.JTextArea
import java.awt.{Color, Dimension, Font, Graphics}
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}


object StoryPanel {
  val TEXT_FONT = new Font("Courier", Font.PLAIN, 12)
  private val INITIAL_LEFT_WIDTH = 600
}

/**
  * This panel is responsible for drawing the Text describing the current scene.
  * @param story story for which to show text and image in the panel.
  * @author Barry Becker
  */
class StoryPanel(var story: Story) extends JSplitPane {
  setContinuousLayout(true)
  setDividerLocation(StoryPanel.INITIAL_LEFT_WIDTH)
  private var textArea = createTextArea
  private var imagePanel = createImagePanel
  add(imagePanel, JSplitPane.RIGHT)
  add(textArea, JSplitPane.LEFT)

  private def createTextArea = {
    val textArea = new JTextArea
    textArea.setFont(StoryPanel.TEXT_FONT)
    textArea.setWrapStyleWord(true)
    textArea.setLineWrap(true)
    textArea.setEditable(false)
    textArea.setMinimumSize(new Dimension(StoryPanel.INITIAL_LEFT_WIDTH / 2, 300))
    textArea
  }

  private def createImagePanel = {
    val imagePanel = new ImageListPanel
    imagePanel.setMaxNumSelections(1)
    imagePanel.setPreferredSize(new Dimension(700, 200))
    imagePanel
  }

  /** Render the Environment on the screen. */
  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    textArea.setText(story.getCurrentScene.text)

    val scene = story.getCurrentScene
    imagePanel.setSingleImage(scene.getImage)
  }
}
