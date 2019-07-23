package com.barrybecker4.puzzle.adventure.ui.editor

import java.awt.{BorderLayout, Color, Dimension, FlowLayout, Font}
import java.awt.event.ActionListener
import java.awt.image.BufferedImage

import com.barrybecker4.common.app.AppContext
import com.barrybecker4.puzzle.adventure.model.Scene
import com.barrybecker4.ui.components.ImageListPanel
import com.barrybecker4.ui.dialogs.AbstractDialog
import com.barrybecker4.ui.util.{GUIUtil, ImageUtil}
import javax.swing.{JComponent, JPanel}
import ShowUniquePathsDialog._


object ShowUniquePathsDialog {
  val PLACEHOLDER_FONT = new Font(GUIUtil.DEFAULT_FONT_FAMILY, Font.PLAIN, 14)
}

/**
  * Use the ImageListsScrollPanel to show all the unique paths from the
  * start scene to the current one.
  *
  * @author Barry Becker
  */
class ShowUniquePathsDialog(val currentScene: Scene, startScene: Scene)
    extends AbstractDialog with ActionListener {

  private val image =
    if (currentScene.image.isEmpty) createPlaceholderImg(currentScene) else currentScene.image.get

  this.setResizable(true)
  setTitle(AppContext.getLabel("IMAGE_PREVIEW"))
  this.setModal(true)
  showContent()

  override protected def createDialogContent: JComponent = {
    val mainPanel = new JPanel(new BorderLayout)
    mainPanel.add(createImagePanel, BorderLayout.CENTER)
    mainPanel.add(createButtonsPanel, BorderLayout.SOUTH)
    mainPanel
  }

  private def createImagePanel = {
    val imagePanel = new ImageListPanel
    imagePanel.setMaxNumSelections(1)
    imagePanel.setPreferredSize(new Dimension(700, 400))
    imagePanel.setSingleImage(image)
    imagePanel
  }

  /** Create the buttons that go at the bottom ( eg OK, Cancel, ...) */
  protected def createButtonsPanel: JPanel = {
    val buttonsPanel = new JPanel(new FlowLayout)
    initBottomButton(cancelButton, AppContext.getLabel("CANCEL"), "Cancel image prview")
    buttonsPanel.add(cancelButton)
    buttonsPanel
  }

  private def createPlaceholderImg(scene: Scene): BufferedImage = {
    val placeHolderImg = ImageUtil.createCompatibleImage(200, 100)
    val g = placeHolderImg.createGraphics()
    g.setPaintMode()
    g.setFont(PLACEHOLDER_FONT)
    g.setColor(Color.YELLOW)
    g.drawString(currentScene.name, 10, 80)
    placeHolderImg
  }
}
