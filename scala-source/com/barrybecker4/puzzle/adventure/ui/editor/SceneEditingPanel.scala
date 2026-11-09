// Copyright by Barry G. Becker, 2000-2026. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.table.TableButtonListener
import javax.swing.JPanel
import java.awt.BorderLayout
import com.barrybecker4.puzzle.adventure.model.Story


/**
  * Composes the current-scene property editor and child-choice table.
  * @param story the story being edited.
  * @param tableButtonListener receives navigate-to-child button clicks.
  */
class SceneEditingPanel(val story: Story, tableButtonListener: TableButtonListener)
  extends JPanel {

  val sceneEditor: SceneEditorPanel = new SceneEditorPanel(story.getCurrentScene, story)
  val childTablePanel: ChildTablePanel = new ChildTablePanel(story, tableButtonListener)

  setLayout(new BorderLayout)
  add(sceneEditor, BorderLayout.CENTER)
  add(childTablePanel, BorderLayout.SOUTH)
}
