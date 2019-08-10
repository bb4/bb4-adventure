// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.components.GradientButton
import com.barrybecker4.ui.table.TableButtonListener
import com.barrybecker4.ui.util.GUIUtil
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import com.barrybecker4.puzzle.adventure.model.Story


/**
  * Shows list of child/destination scenes.
  * You can add/remove/reorder/change these scenes.
  * @param story the story to show children for.
  */
class ChildTablePanel(val story: Story, tableButtonListener: TableButtonListener)
  extends JPanel with ActionListener with ListSelectionListener {

  private var childTable: ChildTable = _

  // for adding/removing/reordering scene choice destinations
  private val addButton = new GradientButton
  private val removeButton = new GradientButton
  private val moveUpButton = new GradientButton
  private val moveDownButton = new GradientButton
  private var selectedChildRow = -1

  setLayout(new BorderLayout)
  childTable = new ChildTable(story.getCurrentScene.choices, tableButtonListener)
  childTable.addListSelectionListener(this)
  setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder,
    "Choices (to navigate to child scenes)"))
  add(new JScrollPane(childTable.getTable), BorderLayout.CENTER)
  add(createChildRowEditButtons, BorderLayout.SOUTH)
  setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 240))

  def updateSceneChoices(): Unit = {
    childTable.getChildTableModel.updateSceneChoices(story.getCurrentScene)
  }

  def clearSelection(): Unit = {
    selectedChildRow = -1
  }

  private def createChildRowEditButtons = {
    val leftButtonsPanel = new JPanel(new FlowLayout)
    initButton(addButton, "Add",
      "Add a new child scene choice to the current scene before the selected position.")
    initButton(removeButton, "Remove",
      "Remove the child scene at the selected position.")
    initButton(moveUpButton, "Up",
      "Move the current scene up one row.")
    initButton(moveDownButton, "Down",
      "Move the current scene down one row.")
    addButton.setIcon(GUIUtil.getIcon(StoryEditorDialog.IMAGE_PATH + "plus.gif"))
    removeButton.setIcon(GUIUtil.getIcon(StoryEditorDialog.IMAGE_PATH + "minus.gif"))
    moveUpButton.setIcon(GUIUtil.getIcon(StoryEditorDialog.IMAGE_PATH + "up_arrow.png"))
    moveDownButton.setIcon(GUIUtil.getIcon(StoryEditorDialog.IMAGE_PATH + "down_arrow.png"))
    removeButton.setEnabled(false)
    moveUpButton.setEnabled(false)
    moveDownButton.setEnabled(false)
    leftButtonsPanel.add(addButton)
    leftButtonsPanel.add(removeButton)
    leftButtonsPanel.add(moveUpButton)
    leftButtonsPanel.add(moveDownButton)
    leftButtonsPanel
  }

  /** Initialize one of the buttons */
  protected def initButton(button: GradientButton, buttonText: String, buttonToolTip: String): Unit = {
    button.setText(buttonText)
    button.setToolTipText(buttonToolTip)
    button.addActionListener(this)
    button.setMinimumSize(new Dimension(45, 25))
  }

  /** Called when one of the add/remove/move/ok/cancel buttons are clicked for editing choices.
    */
  override def actionPerformed(e: ActionEvent): Unit = {
    val source = e.getSource
    val row = selectedChildRow
    val childModel = childTable.getChildTableModel

    source match {
      case add if add eq addButton => addNewChoice(row)
      case remove if remove eq removeButton =>
        val answer = JOptionPane.showConfirmDialog(this,
          "Are you sure you want to delete choice " + childModel.getValueAt(row, ChildTable.NAVIGATE_INDEX) + "?")
        if (answer == JOptionPane.YES_OPTION) {
          childModel.removeChildChoice(row)
          story.getCurrentScene.deleteChoice(row)
        }
      case moveUp if moveUp eq moveUpButton =>
        selectedChildRow = childTable.moveRow(row, row - 1)
        updateMoveButtons()
      case moveDown if moveDown eq moveDownButton =>
        selectedChildRow = childTable.moveRow(row, row + 1)
        updateMoveButtons()
    }
  }

  /** A row in the child table has been selected or selection has changed.
    * @param e event
    */
  override def valueChanged(e: ListSelectionEvent): Unit = {
    selectedChildRow = childTable.getSelectedRow
    removeButton.setEnabled(true)
    updateMoveButtons()
  }

  private def updateMoveButtons(): Unit = {
    moveUpButton.setEnabled(selectedChildRow > 0)
    moveDownButton.setEnabled(selectedChildRow < childTable.getNumRows - 1)
  }

  /** Show a dialog that allows selecting the new child scene destination.
    * This will be either an existing scene or a new one.
    * A new row is automatically added to the table.
    * @param newRow row of the new choice in the child table.
    */
  private def addNewChoice(newRow: Int): Unit = {
    val row = if (newRow < 0) 0
    else newRow
    val newChoiceDlg = new NewChoiceDialog(story.getCandidateDestinationSceneNames)
    val childModel = childTable.getChildTableModel
    val canceled = newChoiceDlg.showDialog
    if (!canceled) {
      val addedSceneName = newChoiceDlg.getSelectedDestinationScene
      val initialLabel: String =
        if (story.getSceneMap.contains(addedSceneName))
          story.getSceneMap.get(addedSceneName).label.getOrElse(ChildTable.DEFAULT_CHOICE_DESC_LABEL)
        else ChildTable.DEFAULT_CHOICE_DESC_LABEL
      childModel.addNewChildChoice(row, addedSceneName, initialLabel)
      val choiceDescription = childModel.getChoiceDescription(row)
      story.addChoiceToCurrentScene(addedSceneName, choiceDescription)
      newChoiceDlg.close()
    }
  }
}
