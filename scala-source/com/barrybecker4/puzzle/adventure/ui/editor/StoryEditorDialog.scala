// Copyright by Barry G. Becker, 2000-2026. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.components.GradientButton
import com.barrybecker4.ui.dialogs.AbstractDialog
import com.barrybecker4.ui.table.TableButtonListener
import com.barrybecker4.ui.util.GUIUtil
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}

import scala.compiletime.uninitialized


object StoryEditorDialog {
  private val INSTRUCTION_FONT = new Font(GUIUtil.DEFAULT_FONT_FAMILY, Font.PLAIN, 10)
  /** location for images. */
  val IMAGE_PATH = "com/barrybecker4/puzzle/adventure/ui/images/"
}

/**
  * Allows editing of a story in a separate dialog.
  * It consists of 3 parts:
  *   - list of parent scenes
  *   - current scene property editor
  *   - list of child scenes
  * You can add/remove/reorder/change scenes in the story.
  * @param story creates a copy of this in case we cancel.
  */
class StoryEditorDialog(val story: Story)
  extends AbstractDialog with ActionListener with TableButtonListener {

  /** Rebuilt when navigation refreshes dialog content via showContent(). */
  private var sceneEditor: SceneEditorPanel = uninitialized
  private var parentScenes: Seq[Scene] = uninitialized
  private var childTablePanel: ChildTablePanel = uninitialized

  /** click this when done editing the scene. */
  private val okButton = new GradientButton
  private var sceneSelector: JComboBox[String] = uninitialized
  private var lastVisitedScene: Option[Scene] = None

  this.setResizable(true)
  setTitle("Story Editor")
  this.setModal(true)
  showContent()

  override def createDialogContent: JComponent = {
    val mainPanel = new JPanel(new BorderLayout)
    mainPanel.setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 800))
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
    val parentTablePanel = new ParentTablePanel(story, lastVisitedScene, this)
    parentScenes = parentTablePanel.parentScenes
    val sceneEditingPanel = new SceneEditingPanel(story, this)
    sceneEditor = sceneEditingPanel.sceneEditor
    childTablePanel = sceneEditingPanel.childTablePanel
    editingPane.add(parentTablePanel, BorderLayout.NORTH)
    editingPane.add(sceneEditingPanel, BorderLayout.CENTER)
    editingPane
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
    val label = new JLabel("Jump to Scene ")
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

  /** Called when one of the bottom buttons are clicked
    */
  override def actionPerformed(e: ActionEvent): Unit = {
    super.actionPerformed(e)
    val source = e.getSource

    source match {
      case okB if okB eq okButton => ok()
      case sel if sel eq sceneSelector =>
        commitSceneChanges()
        story.advanceToScene(sceneSelector.getSelectedItem.toString)
        showContent()
      case c if c == cancelButton => // do nothing
      case n if n == null => // do nothing
      case _ => throw new IllegalArgumentException("Unexpected button: " + source)
    }
  }

  /** @param row table row
    * @param col table column
    * @param buttonId id of buttonEditor clicked.
    */
  override def tableButtonClicked(row: Int, col: Int, buttonId: String): Unit = {
    commitSceneChanges()
    lastVisitedScene = Some(story.getCurrentScene)
    if (ChildTable.NAVIGATE_TO_CHILD_BUTTON_ID.equals(buttonId)) {
      childTablePanel.clearSelection()
      story.advanceScene(row)
    }
    else if (ParentTable.NAVIGATE_TO_PARENT_BUTTON_ID == buttonId)
      story.advanceToScene(parentScenes(row).name)
    else assert(assertion = false, "unexpected id =" + buttonId)
    showContent()
  }

  /** @return our edited copy of the story we were passed at construction. */
  def getEditedStory: Story = story

  private def commitSceneChanges(): Unit = {
    if (sceneEditor.isSceneNameChanged &&
        story.getSceneMap.contains(sceneEditor.getProposedSceneName)) {
      JOptionPane.showMessageDialog(
        this,
        "A scene named \"" + sceneEditor.getProposedSceneName + "\" already exists. Choose a different name.",
        "Duplicate scene name",
        JOptionPane.ERROR_MESSAGE)
      sceneEditor.revertProposedSceneName()
    }
    sceneEditor.doSave()
    if (sceneEditor.isSceneNameChanged &&
        !story.sceneNameChanged(sceneEditor.getOldSceneName, sceneEditor.getEditedScene.name)) {
      sceneEditor.revertProposedSceneName()
      sceneEditor.doSave()
    }
    childTablePanel.updateSceneChoices()
  }

  private def ok(): Unit = {
    commitSceneChanges()
    this.setVisible(false)
  }
}
