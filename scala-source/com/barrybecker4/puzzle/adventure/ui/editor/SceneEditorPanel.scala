// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.puzzle.adventure.ui.StoryPanel
import com.barrybecker4.ui.components.{GradientButton, ImageListPanel, ScrollingTextArea, TextInput}
import com.barrybecker4.ui.dialogs.ImagePreviewDialog
import javax.swing._
import java.awt.{BorderLayout, Component, Dimension, FlowLayout}
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage

import com.barrybecker4.puzzle.adventure.model.Scene


object SceneEditorPanel {
  val EDITOR_WIDTH = 900
}

/**
  * Used to edit an individual scene.
  * @param scene the scene to populate the editor with.
  * @author Barry Becker
  */
class SceneEditorPanel(var scene: Scene, val startScene: Scene) extends JPanel with ActionListener {
  private val oldSceneName = scene.name
  private var showImageButton: GradientButton = _
  private var playSoundButton: GradientButton = _
  private var showPathsButton: GradientButton = _
  private var nameInput: TextInput = _
  private var sceneText: ScrollingTextArea = _
  createUI()


  private[editor] def createUI(): Unit = {
    this.setLayout(new BorderLayout)
    //this.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 600))
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Edit current Scene"))
    nameInput = new TextInput("name:", scene.name)
    nameInput.setColumns(40)

    sceneText = new ScrollingTextArea
    sceneText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    sceneText.setEditable(true)
    sceneText.setFont(StoryPanel.TEXT_FONT)
    sceneText.setText(scene.text)

    val mainContent = new JPanel()
    mainContent.setLayout(new BorderLayout)
    mainContent.add(nameInput, BorderLayout.NORTH)
    mainContent.add(sceneText, BorderLayout.CENTER)

    add(mainContent, BorderLayout.CENTER)
    if (scene.image.isDefined) {
      val imageThumbnail = createImageThumbNail(scene.image.get)
      add(imageThumbnail, BorderLayout.EAST)
    }
    add(createMediaButtons, BorderLayout.SOUTH)
  }

  /** For sound and image and whatever else is associated with the scene.
    * @return image and sound buttons in a panel.
    */
  private def createMediaButtons = {
    val buttonPanel = new JPanel(new FlowLayout)

    showImageButton = new GradientButton("Image")
    showImageButton.addActionListener(this)
    showImageButton.setEnabled(scene.image != null)

    playSoundButton = new GradientButton("Sound")
    playSoundButton.addActionListener(this)
    playSoundButton.setEnabled(scene.hasSound)

    showPathsButton = new GradientButton("Show paths")
    showPathsButton.addActionListener(this)
    showPathsButton.setEnabled(scene.image != null)

    buttonPanel.add(showImageButton)
    buttonPanel.add(playSoundButton)
    buttonPanel.add(showPathsButton)
    buttonPanel
  }

  private def createImageThumbNail(image: BufferedImage): JPanel = {
    val imagePanel = new ImageListPanel
    imagePanel.setBackground(this.getBackground)
    imagePanel.setMaxNumSelections(1)
    imagePanel.setPreferredSize(new Dimension(300, 400))
    imagePanel.setSingleImage(image)
    imagePanel
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    e.getSource match {
      case ib if ib == showImageButton => {
        val imgPreviewDlg = new ImagePreviewDialog(scene.image.get)
        imgPreviewDlg.showDialog
      }
      case psb if psb == playSoundButton => scene.playSound()
      case spb if spb == showPathsButton => {
        val showUniquePathsDlg = new ShowUniquePathsDialog(scene, startScene)
        showUniquePathsDlg.showDialog
      }
    }
  }

  def isSceneNameChanged: Boolean = !(oldSceneName == nameInput.getValue)
  def getOldSceneName: String = oldSceneName
  def getEditedScene: Scene = scene

  /** Persist the scene changes to the story. */
  def doSave(): Unit = {
    if (isSceneNameChanged) scene.setName(nameInput.getValue)
    scene.text = sceneText.getText
  }
}
