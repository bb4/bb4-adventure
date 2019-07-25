package com.barrybecker4.puzzle.adventure.ui.editor

import java.awt.{BorderLayout, Color, Dimension, FlowLayout, Font}
import java.awt.event.ActionListener
import java.awt.image.BufferedImage

import com.barrybecker4.common.app.AppContext
import com.barrybecker4.puzzle.adventure.model.{Scene, Story, UniquePathsFinder}
import com.barrybecker4.ui.components.ImageListsScrollPanel
import com.barrybecker4.ui.dialogs.AbstractDialog
import com.barrybecker4.ui.util.{GUIUtil, ImageUtil}
import javax.swing.{JComponent, JPanel}
import ShowUniquePathsDialog._


object ShowUniquePathsDialog {
  val PLACEHOLDER_FONT = new Font(GUIUtil.DEFAULT_FONT_FAMILY, Font.PLAIN, 14)
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

  private val image = getDisplayImage(currentScene)

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

    val imageLists = paths.map(list => list.map(getDisplayImage))

    imagePanel.setImageLists(imageLists)
    imagePanel
  }

  /** Create the buttons that go at the bottom ( eg OK, Cancel, ...) */
  protected def createButtonsPanel: JPanel = {
    val buttonsPanel = new JPanel(new FlowLayout)
    initBottomButton(cancelButton, AppContext.getLabel("CANCEL"), "Cancel image prview")
    buttonsPanel.add(cancelButton)
    buttonsPanel
  }

  private def getDisplayImage(scene: Scene): BufferedImage = {
    val img = scene.image
    if (img.isDefined) img.get else createPlaceholderImg(scene)
  }

  private def createPlaceholderImg(scene: Scene): BufferedImage = {
    val placeHolderImg = ImageUtil.createCompatibleImage(200, 100)
    val g = placeHolderImg.createGraphics()
    g.setPaintMode()
    g.setFont(PLACEHOLDER_FONT)
    g.setColor(Color.YELLOW)
    g.drawString(scene.name, 10, 80)
    placeHolderImg
  }
}
