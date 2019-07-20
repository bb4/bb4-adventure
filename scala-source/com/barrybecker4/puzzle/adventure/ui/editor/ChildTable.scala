// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.puzzle.adventure.model.ChoiceList
import com.barrybecker4.ui.table.{TableBase, TableButton, TableButtonListener, TableColumnMeta}
import javax.swing.ListSelectionModel

import scala.collection.Seq
import ChildTable._
import com.barrybecker4.puzzle.adventure.model.{Choice, ChoiceList}


/**
  * Shows a list of the child scenes, allows editing the navigation text,
  * and allows navigating to them.
  * @author Barry Becker
  */
object ChildTable {
  val NEW_CHOICE_DESC_LABEL = " - Put your choice description here -"
  val NAVIGATE_TO_CHILD_BUTTON_ID = "navToChild"
  private[editor] val NAVIGATE_INDEX = 0
  private[editor] val CHOICE_DESCRIPTION_INDEX = 1
  private val NAVIGATE = "Navigate to"
  private val CHOICE_DESCRIPTION = "Choice Description"
  private val CHILD_COLUMN_NAMES = Array(NAVIGATE, CHOICE_DESCRIPTION)
}

class ChildTable(val choices: ChoiceList, var tableButtonListener: TableButtonListener)
  extends TableBase {

  initColumnMeta(ChildTable.CHILD_COLUMN_NAMES)
  initializeTable(choices.choices.asInstanceOf[Seq[AnyRef]])
  getTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  def moveRow(oldRow: Int, newRow: Int): Int = {
    if (newRow >= 0 && newRow < getChildTableModel.getRowCount) {
      getChildTableModel.moveRow(oldRow, oldRow, newRow)
      getTable.setRowSelectionInterval(newRow, newRow)
      return newRow
    }
    oldRow
  }

  /** Add a row based on a player object. */
  override def addRow(choice: Any): Unit = {
    val childChoice = choice.asInstanceOf[Choice]
    val d = new Array[AnyRef](getNumColumns)
    d(NAVIGATE_INDEX) = childChoice.destinationScene
    d(CHOICE_DESCRIPTION_INDEX) = childChoice.description
    getChildTableModel.addRow(d)
  }

  override def updateColumnMeta(columnMeta: Array[TableColumnMeta]): Unit = {
    val navigateCol = columnMeta(NAVIGATE_INDEX)
    val navCellEditor = new TableButton(NAVIGATE_INDEX, NAVIGATE_TO_CHILD_BUTTON_ID)
    navCellEditor.addTableButtonListener(tableButtonListener)
    navCellEditor.setToolTipText("navigate to this scene")
    navigateCol.setCellRenderer(navCellEditor)
    navigateCol.setCellEditor(navCellEditor)
    navigateCol.setPreferredWidth(200)
    navigateCol.setMaxWidth(400)
    columnMeta(ChildTable.CHOICE_DESCRIPTION_INDEX).setPreferredWidth(500)
  }

  override def createTableModel(columnNames: Array[String]) =
    new ChildTableModel(columnNames.asInstanceOf[Array[AnyRef]], 0)

  private[editor] def getChildTableModel = table.getModel.asInstanceOf[ChildTableModel]
}
