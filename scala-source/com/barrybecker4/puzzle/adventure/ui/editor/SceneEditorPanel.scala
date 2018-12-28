// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.puzzle.adventure.Scene
import com.barrybecker4.puzzle.adventure.ui.StoryPanel
import com.barrybecker4.ui.components.GradientButton
import com.barrybecker4.ui.components.ScrollingTextArea
import com.barrybecker4.ui.components.TextInput
import com.barrybecker4.ui.dialogs.ImagePreviewDialog
import javax.swing.BorderFactory
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


object SceneEditorPanel {
  val EDITOR_WIDTH = 900
}

/**
  * Used to edit an individual scene.
  * @param scene the scene to populate the editor with.
  * @author Barry Becker
  */
class SceneEditorPanel(var scene: Scene) extends JPanel with ActionListener {
  private var oldSceneName = scene.name
  private var showImageButton: GradientButton = _
  private var playSoundButton: GradientButton = _
  private var nameInput: TextInput = _
  private var sceneText: ScrollingTextArea = _
  createUI()


  private[editor] def createUI(): Unit = {
    this.setLayout(new BorderLayout)
    this.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 600))
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Edit current Scene"))
    nameInput = new TextInput("name:", scene.name)
    nameInput.setColumns(50)
    sceneText = new ScrollingTextArea
    sceneText.setEditable(true)
    sceneText.setFont(StoryPanel.TEXT_FONT)
    sceneText.setText(scene.text)
    add(nameInput, BorderLayout.NORTH)
    add(sceneText, BorderLayout.CENTER)
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
    buttonPanel.add(showImageButton)
    buttonPanel.add(playSoundButton)
    buttonPanel
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    val source = e.getSource
    if (source eq showImageButton) {
      val imgPreviewDlg = new ImagePreviewDialog(scene.image.get)
      imgPreviewDlg.showDialog
    }
    else if (source eq playSoundButton) scene.playSound()
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
