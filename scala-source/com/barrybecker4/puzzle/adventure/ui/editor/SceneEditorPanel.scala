// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.puzzle.adventure.ui.StoryPanel
import com.barrybecker4.ui.components.{GradientButton, ImageListPanel, ScrollingTextArea, TextInput}
import com.barrybecker4.ui.dialogs.ImagePreviewDialog
import javax.swing._
import java.awt.{BorderLayout, Dimension, FlowLayout}
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

  private val nameInput: TextInput = {
    val input = new TextInput("name:", scene.name)
    input.setColumns(40)
    input
  }
  private val labelInput: Option[TextInput] = scene.label.map { label =>
    val input = new TextInput("label:", label)
    input.setColumns(45)
    input
  }
  private val sceneDescription: ScrollingTextArea = {
    val area = new ScrollingTextArea
    area.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    area.setEditable(true)
    area.setFont(StoryPanel.TEXT_FONT)
    area.setText(scene.description)
    area
  }
  private val showImageButton = new GradientButton("Image")
  private val playSoundButton = new GradientButton("Sound")
  private val showPathsButton = new GradientButton("Show paths")

  createUI()


  private[editor] def createUI(): Unit = {
    this.setLayout(new BorderLayout)
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Edit current Scene"))

    val mainContent = new JPanel(new BorderLayout)
    mainContent.add(buildTopInputsPanel(), BorderLayout.NORTH)
    mainContent.add(sceneDescription, BorderLayout.CENTER)

    add(mainContent, BorderLayout.CENTER)
    addThumbnailIfPresent()
    add(createMediaButtons, BorderLayout.SOUTH)
  }

  private def buildTopInputsPanel(): JPanel = {
    val topInputs = new JPanel(new BorderLayout)
    topInputs.add(nameInput, BorderLayout.NORTH)
    labelInput.foreach(topInputs.add(_, BorderLayout.CENTER))
    topInputs
  }

  private def addThumbnailIfPresent(): Unit =
    scene.image.foreach { img =>
      add(createImageThumbNail(img), BorderLayout.EAST)
    }

  /** For sound and image and whatever else is associated with the scene.
    * @return image and sound buttons in a panel.
    */
  private def createMediaButtons = {
    val buttonPanel = new JPanel(new FlowLayout)

    showImageButton.addActionListener(this)
    showImageButton.setEnabled(scene.image.isDefined)

    playSoundButton.addActionListener(this)
    playSoundButton.setEnabled(scene.hasSound)

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
    labelInput.foreach { input =>
      scene.label = Some(input.getValue)
    }
  }
}
