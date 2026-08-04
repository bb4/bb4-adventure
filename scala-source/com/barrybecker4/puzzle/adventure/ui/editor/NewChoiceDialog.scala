// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.components.GradientButton
import com.barrybecker4.ui.components.TextInput
import com.barrybecker4.ui.dialogs.AbstractDialog
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


/**
  * Allow the user to select the name of the destination scene
  * or type in the name of a new scene.
  * @param candidateDestinations used to populate the choice list
  * @author Barry Becker
  */
class NewChoiceDialog(var candidateDestinations: Seq[String]) extends AbstractDialog with ActionListener {

  this.setResizable(false)
  setTitle("New Scene Choice")
  this.setModal(true)

  /** click this when done selecting a name for the destination scene. */
  private val okButton = new GradientButton
  private val sceneSelector = new JComboBox[String](candidateDestinations.sorted.toArray)
  private val sceneTextInput: TextInput = {
    val input = new TextInput("New scene name")
    input.setColumns(30)
    input
  }
  private var selectedDestinationScene = ""
  showContent()

  override def createDialogContent: JComponent = {
    val outerPanel = new JPanel(new BorderLayout)
    outerPanel.add(buildPickerColumn(), BorderLayout.CENTER)
    outerPanel.add(createButtonsPanel, BorderLayout.SOUTH)
    outerPanel
  }

  private def buildPickerColumn(): JComponent = {
    val mainPanel = new JPanel
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS))
    sceneSelector.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneSelector.setBorder(BorderFactory.createTitledBorder(
      "Select an existing scene or type in the name for a new scene."))
    val orLabel = new JLabel("or")
    orLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneTextInput.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneTextInput.setBorder(BorderFactory.createTitledBorder("Enter the name for a new scene."))
    mainPanel.add(sceneSelector)
    mainPanel.add(orLabel)
    mainPanel.add(sceneTextInput)
    mainPanel
  }

  def getSelectedDestinationScene: String = selectedDestinationScene

  /** create the buttons that go at the button ( eg OK, Cancel, ...)
    * @return buttons panel.
    */
  private[editor] def createButtonsPanel = {
    val buttonsPanel = new JPanel
    initBottomButton(okButton, "OK",
      "Use the selected scene as the new choice destination. ")
    initBottomButton(cancelButton, "Cancel",
      "Do not select any scene.")
    buttonsPanel.add(okButton)
    buttonsPanel.add(cancelButton)
    buttonsPanel
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    super.actionPerformed(e)
    val source = e.getSource
    if (source eq okButton) ok()
  }

  private[editor] def ok(): Unit = {
    val customSceneName = sceneTextInput.getValue
    selectedDestinationScene =
      if (customSceneName != "") customSceneName
      else sceneSelector.getSelectedItem.toString
    this.setVisible(false)
  }
}
