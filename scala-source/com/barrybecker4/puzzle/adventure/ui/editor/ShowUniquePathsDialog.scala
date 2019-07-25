package com.barrybecker4.puzzle.adventure.ui.editor

import java.awt.{BorderLayout, Color, Dimension, FlowLayout, Font}
import java.awt.event.ActionListener
import com.barrybecker4.common.app.AppContext
import com.barrybecker4.puzzle.adventure.model.{Scene, Story, UniquePathsFinder}
import com.barrybecker4.ui.components.ImageListsScrollPanel
import com.barrybecker4.ui.dialogs.AbstractDialog
import javax.swing.{JComponent, JPanel}
import ShowUniquePathsDialog._


object ShowUniquePathsDialog {
  val IMAGE_HT = 100
}

/**
  * Use the ImageListsScrollPanel to show all the unique paths from the
  * start scene to the current one.
  *
  * @author Barry Becker
  */
class ShowUniquePathsDialog(val currentScene: Scene, story: Story)
    extends AbstractDialog with ActionListener {

  private val pathsFinder = UniquePathsFinder(story)
  private val paths = pathsFinder.findUniquePaths(currentScene)
  //println(paths.mkString(", "))

  private val image = currentScene.getImage

  this.setResizable(true)
  setTitle("All unique paths from the start") // AppContext.getLabel(...)
  this.setModal(true)
  showContent()

  override protected def createDialogContent: JComponent = {
    val mainPanel = new JPanel(new BorderLayout)
    mainPanel.add(createImageListsPanel, BorderLayout.CENTER)
    mainPanel.add(createButtonsPanel, BorderLayout.SOUTH)
    mainPanel
  }

  private def createImageListsPanel: JPanel = {
    val imagePanel = new ImageListsScrollPanel(IMAGE_HT)
    imagePanel.setPreferredSize(new Dimension(700, 400))

    val imageLists = paths.map(list => list.map(scene => (scene.getImage, scene.name)))

    imagePanel.setImageListsWithTips(imageLists)
    imagePanel
  }

  /** Create the buttons that go at the bottom ( eg OK, Cancel, ...) */
  protected def createButtonsPanel: JPanel = {
    val buttonsPanel = new JPanel(new FlowLayout)
    initBottomButton(cancelButton, AppContext.getLabel("CANCEL"), "Cancel image prview")
    buttonsPanel.add(cancelButton)
    buttonsPanel
  }

}
