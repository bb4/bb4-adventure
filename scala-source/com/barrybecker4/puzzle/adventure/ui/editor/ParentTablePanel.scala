// Copyright by Barry G. Becker, 2000-2026. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.table.TableButtonListener
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import com.barrybecker4.puzzle.adventure.model.{Scene, Story}


/**
  * Shows the list of parent scenes that lead to the currently edited scene.
  * @param story the story being edited.
  * @param lastVisitedScene scene navigated from, highlighted in the parent table.
  * @param tableButtonListener receives navigate-to-parent button clicks.
  */
class ParentTablePanel(
    val story: Story,
    lastVisitedScene: Option[Scene],
    tableButtonListener: TableButtonListener)
  extends JPanel {

  val parentScenes: Seq[Scene] = story.getParentScenes

  setLayout(new BorderLayout)
  setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder, "Parent Scenes"))
  setPreferredSize(new Dimension(SceneEditorPanel.EDITOR_WIDTH, 130))

  private val parentTable = new ParentTable(parentScenes, lastVisitedScene, tableButtonListener)
  add(new JScrollPane(parentTable.getTable), BorderLayout.WEST)
}
