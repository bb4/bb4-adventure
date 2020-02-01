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
  private var sceneSelector: JComboBox[String] = _
  private var sceneTextInput: TextInput = _
  private var selectedDestinationScene: String = _
  showContent()

  override def createDialogContent: JComponent = {
    val outerPanel = new JPanel(new BorderLayout)
    val mainPanel = new JPanel
    val layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS)
    mainPanel.setLayout(layout)
    sceneSelector = new JComboBox[String](candidateDestinations.sorted.toArray)
    sceneSelector.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneSelector.setBorder(BorderFactory.createTitledBorder(
      "Select an existing scene or type in the name for a new scene.")
    )
    val orLabel = new JLabel("or")
    orLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneTextInput = new TextInput("New scene name")
    sceneTextInput.setColumns(30)
    sceneTextInput.setAlignmentX(Component.LEFT_ALIGNMENT)
    sceneTextInput.setBorder(BorderFactory.createTitledBorder("Enter the name for a new scene."))
    mainPanel.add(sceneSelector)
    mainPanel.add(orLabel)
    mainPanel.add(sceneTextInput)
    outerPanel.add(mainPanel, BorderLayout.CENTER)
    outerPanel.add(createButtonsPanel, BorderLayout.SOUTH)
    outerPanel
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
    if (customSceneName != "")
      selectedDestinationScene = customSceneName
    else
      selectedDestinationScene = sceneSelector.getSelectedItem.toString
    this.setVisible(false)
  }
}
