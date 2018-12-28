// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.puzzle.adventure.Story
import com.barrybecker4.puzzle.adventure.Scene
import com.barrybecker4.ui.components.GradientButton
import com.barrybecker4.ui.dialogs.AbstractDialog
import com.barrybecker4.ui.table.TableButtonListener
import com.barrybecker4.ui.util.GUIUtil
import javax.swing.BorderFactory
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


object StoryEditorDialog {
  private val INSTRUCTION_FONT = new Font(GUIUtil.DEFAULT_FONT_FAMILY, Font.PLAIN, 10)
  /** location for images. */
  private val IMAGE_PATH = "com/barrybecker4/puzzle/adventure/ui/images/"
}

/**
  * Allows editing of a story in a separate dialog.
  * You can add/remove/reorder/change scenes in the story.
  * @param story creates a copy of this in case we cancel.
  */
class StoryEditorDialog(val story: Story)
  extends AbstractDialog with ActionListener with TableButtonListener with ListSelectionListener {

  /** The story to edit */
  private var sceneEditor: SceneEditorPanel = _
  private var parentScenes: Seq[Scene] = _
  private var childTable: ChildTable = _
  /** click this when done editing the scene. */
  private val okButton = new GradientButton
  // for adding/removing/reordering scene choice destinations
  private val addButton = new GradientButton
  private val removeButton = new GradientButton
  private val moveUpButton = new GradientButton
  private val moveDownButton = new GradientButton
  private var sceneSelector: JComboBox[String] = _
  private var selectedChildRow = -1

  this.setResizable(true)
  setTitle("Story Editor")
  this.setModal(true)
  showContent()

  override def createDialogContent: JComponent = {
    val mainPanel = new JPanel(new BorderLayout)
    mainPanel.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 700))
    val editingPane = createEditingPane
    val title = new JLabel("Navigate through the scene heirarchy and change values for scenes.")
    title.setBorder(BorderFactory.createEmptyBorder(5, 4, 20, 4))
    title.setFont(StoryEditorDialog.INSTRUCTION_FONT)
    mainPanel.add(title, BorderLayout.NORTH)
    mainPanel.add(editingPane, BorderLayout.CENTER)
    mainPanel.add(createButtonsPanel, BorderLayout.SOUTH)
    mainPanel
  }

  /** Parent table on top.
    * Scene editor in the middle.
    * Child options on the bottom.
    * @return the panel that holds the story editor controls
    */
  private def createEditingPane = {
    val editingPane = new JPanel(new BorderLayout)
    editingPane.add(createParentTablePanel, BorderLayout.NORTH)
    editingPane.add(createSceneEditingPanel, BorderLayout.CENTER)
    editingPane
  }

  /** @return table holding list of scenes that lead to the currently edited scene. */
  private def createParentTablePanel = {
    val parentContainer = new JPanel(new BorderLayout)
    parentScenes = story.getParentScenes
    val parentTable = new ParentTable(parentScenes, this)
    val tableHolder = new JPanel
    tableHolder.setMaximumSize(new Dimension(500, 300))
    parentContainer.setBorder(
      BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Parent Scenes")
    )
    parentContainer.add(new JScrollPane(parentTable.getTable), BorderLayout.WEST)
    parentContainer.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 120))
    parentContainer
  }

  private def createSceneEditingPanel = {
    val container = new JPanel(new BorderLayout)
    sceneEditor = new SceneEditorPanel(story.getCurrentScene)
    container.add(sceneEditor, BorderLayout.CENTER)
    container.add(createChildTablePanel, BorderLayout.SOUTH)
    container
  }

  /** @return table of child scene choices.*/
  private def createChildTablePanel = {
    val childContainer = new JPanel(new BorderLayout)
    childTable = new ChildTable(story.getCurrentScene.choices.get, this)
    childTable.addListSelectionListener(this)
    childContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Choices (to navigate to child scenes)"))
    childContainer.add(new JScrollPane(childTable.getTable), BorderLayout.CENTER)
    childContainer.add(createChildRowEditButtons, BorderLayout.SOUTH)
    childContainer.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 240))
    childContainer
  }

  private def createChildRowEditButtons = {
    val leftButtonsPanel = new JPanel(new FlowLayout)
    initBottomButton(addButton, "Add", "Add a new child scene choice to the current scene before the selected position.")
    initBottomButton(removeButton, "Remove", "Remove the child scene at the selected position.")
    initBottomButton(moveUpButton, "Up", "Move the current scene up one row.")
    initBottomButton(moveDownButton, "Down", "Move the current scene down one row.")
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

  /** Create the buttons that go at the botton ( eg row editing buttons and OK, Cancel, ...)
    * @return ok cancel panel.
    */
  private def createButtonsPanel = {
    val outerPanel = new JPanel(new BorderLayout)
    outerPanel.add(createJumpToPanel, BorderLayout.WEST)
    outerPanel.add(createRightButtons, BorderLayout.EAST)
    outerPanel
  }

  private def createJumpToPanel = {
    val panel = new JPanel(new BorderLayout)
    val label = new JLabel("Jump to Scene")
    sceneSelector = new JComboBox[String](story.getAllSceneNames.toArray)
    sceneSelector.addActionListener(this)
    panel.add(label, BorderLayout.WEST)
    panel.add(sceneSelector, BorderLayout.CENTER)
    panel.setToolTipText("Jump to a specific scene so you can edit from there.")
    panel
  }

  private def createRightButtons = {
    val rightButtonsPanel = new JPanel(new FlowLayout)
    initBottomButton(okButton, "OK", "Save your edits and see the changes in the story. ")
    initBottomButton(cancelButton, "Cancel", "Go back to the story without saving your edits.")
    rightButtonsPanel.add(okButton)
    rightButtonsPanel.add(cancelButton)
    rightButtonsPanel
  }

  /**
    * Called when one of the add/remove/move/ok/cancel buttons are clicked for editing choices.
    */
  override def actionPerformed(e: ActionEvent): Unit = {
    super.actionPerformed(e)
    val source = e.getSource
    val row = selectedChildRow
    val childModel = childTable.getChildTableModel
    if (source eq okButton) ok()
    else if (source eq addButton) addNewChoice(row)
    else if (source eq removeButton) { //System.out.println("remove row");
      val answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete choice " + childModel.getValueAt(row, com.barrybecker4.puzzle.adventure.ui.editor.ChildTable.NAVIGATE_INDEX) + "?")
      if (answer == JOptionPane.YES_OPTION) {
        childModel.removeRow(row)
        story.getCurrentScene.deleteChoice(row)
      }
    }
    else if (source eq moveUpButton) {
      selectedChildRow = childTable.moveRow(row, row - 1)
      updateMoveButtons()
    }
    else if (source eq moveDownButton) {
      selectedChildRow = childTable.moveRow(row, row + 1)
      updateMoveButtons()
    }
    else if (source eq sceneSelector) {
      commitSceneChanges()
      story.advanceToScene(sceneSelector.getSelectedItem.toString)
      showContent()
    }
    // This will prevent this handler from being called multiple times. Don't know why.
    e.setSource(null)
  }

  /**
    * @param row      table row
    * @param col      table column
    * @param buttonId id of buttonEditor clicked.
    */
  override def tableButtonClicked(row: Int, col: Int, buttonId: String): Unit = {
    commitSceneChanges()
    if (ChildTable.NAVIGATE_TO_CHILD_BUTTON_ID.equals(buttonId)) story.advanceScene(row)
    else if (ParentTable.NAVIGATE_TO_PARENT_BUTTON_ID == buttonId) story.advanceToScene(parentScenes(row).name)
    else assert(false, "unexpected id =" + buttonId)
    selectedChildRow = -1
    showContent()
  }

  /** A row in the child table has been selected or selection has changed.
    * @param e event
    */
  override def valueChanged(e: ListSelectionEvent): Unit = {
    selectedChildRow = childTable.getSelectedRow
    //System.out.println("selected row now " + selectedChildRow);
    removeButton.setEnabled(true)
    updateMoveButtons()
  }

  private def updateMoveButtons(): Unit = {
    moveUpButton.setEnabled(selectedChildRow > 0)
    moveDownButton.setEnabled(selectedChildRow < childTable.getNumRows - 1)
  }

  /** Show a dialog that allows selecting the new child scene destination.
    * This will be either an exisiting scene or a new one.
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
      childModel.addNewChildChoice(row, addedSceneName)
      val choiceDescription = childModel.getChoiceDescription(row)
      story.addChoiceToCurrentScene(addedSceneName, choiceDescription)
      newChoiceDlg.close()
    }
  }

  /** @return our edited copy of the story we were passed at construction. */
  def getEditedStory: Story = story

  private def commitSceneChanges(): Unit = {
    sceneEditor.doSave()
    if (sceneEditor.isSceneNameChanged)
      story.sceneNameChanged(sceneEditor.getOldSceneName, sceneEditor.getEditedScene.name)
    // also save the choice text (it may have been modified or reordered)
    childTable.getChildTableModel.updateSceneChoices(story.getCurrentScene)
  }

  private def ok(): Unit = {
    commitSceneChanges()
    this.setVisible(false)
  }
}
