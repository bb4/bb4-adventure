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
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}


object SceneEditorPanel {
  val EDITOR_WIDTH = 900
}

/**
  * Used to edit an individual scene.
  * @param scene the scene to populate the editor with.
  * @author Barry Becker
  */
class SceneEditorPanel(var scene: Scene, val story: Story) extends JPanel with ActionListener {
  private val oldSceneName = scene.name
  private var showImageButton: GradientButton = _
  private var playSoundButton: GradientButton = _
  private var showPathsButton: GradientButton = _
  private var nameInput: TextInput = _
  private var labelInput: TextInput = _
  private var sceneDescription: ScrollingTextArea = _
  createUI()


  private[editor] def createUI(): Unit = {
    this.setLayout(new BorderLayout)
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Edit current Scene"))

    val topInputs = new JPanel(new BorderLayout)
    nameInput = new TextInput("name:", scene.name)
    nameInput.setColumns(40)
    topInputs.add(nameInput, BorderLayout.NORTH)

    if (scene.label.isDefined) {
      labelInput = new TextInput("label:", scene.label.get)
      labelInput.setColumns(45)
      topInputs.add(labelInput, BorderLayout.CENTER)
    }

    sceneDescription = new ScrollingTextArea
    sceneDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    sceneDescription.setEditable(true)
    sceneDescription.setFont(StoryPanel.TEXT_FONT)
    sceneDescription.setText(scene.description)

    val mainContent = new JPanel()
    mainContent.setLayout(new BorderLayout)
    mainContent.add(topInputs, BorderLayout.NORTH)
    mainContent.add(sceneDescription, BorderLayout.CENTER)

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
    showImageButton.setEnabled(scene.image.isDefined)

    playSoundButton = new GradientButton("Sound")
    playSoundButton.addActionListener(this)
    playSoundButton.setEnabled(scene.hasSound)

    showPathsButton = new GradientButton("Show paths")
    showPathsButton.addActionListener(this)
    showPathsButton.setEnabled(scene.image.isDefined)

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
      case ib if ib == showImageButton =>
        val imgPreviewDlg = new ImagePreviewDialog(scene.image.get)
        imgPreviewDlg.showDialog
      case psb if psb == playSoundButton => scene.playSound()
      case spb if spb == showPathsButton =>
        val showUniquePathsDlg = new ShowUniquePathsDialog(scene, story)
        showUniquePathsDlg.showDialog
    }
  }

  def isSceneNameChanged: Boolean = !(oldSceneName == nameInput.getValue)
  def getOldSceneName: String = oldSceneName
  def getEditedScene: Scene = scene

  /** Persist the scene changes to the story. */
  def doSave(): Unit = {
    if (isSceneNameChanged) scene.setName(nameInput.getValue)
    scene.description = sceneDescription.getText
    if (scene.label.isDefined) {
      scene.label = Some(labelInput.getValue)
    }
  }
}
