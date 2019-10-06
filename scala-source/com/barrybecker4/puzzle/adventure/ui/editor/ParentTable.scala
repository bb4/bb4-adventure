// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import com.barrybecker4.ui.table.TableBase
import com.barrybecker4.ui.table.TableButton
import com.barrybecker4.ui.table.TableButtonListener
import com.barrybecker4.ui.table.TableColumnMeta
import javax.swing.table.DefaultTableModel
import ParentTable._
import com.barrybecker4.puzzle.adventure.model.Scene


object ParentTable {
  var NAV_FROM_ID = "navdFrom"
  val NAVIGATE_TO_PARENT_BUTTON_ID = "navToParent"

  private val NAVIGATED_FROM_INDEX = 0
  private[editor] val NAVIGATE_TO_INDEX = 1
  private val NUM_CHILDREN_INDEX = 2
  private val NAVIGATED_FROM = "from"
  private val NAVIGATE_TO = "Navigate to"
  private val NUM_CHILDREN = "Child Scenes"
  private val PARENT_COLUMN_NAMES = Array(NAVIGATED_FROM, NAVIGATE_TO, NUM_CHILDREN)
}

/** Shows a list of the parent scenes and allows navigating to them.
  * The first column shows if this is the parent that we just navigate from.
  * @param scenes to initialize the rows in the table with.
  * @param tableButtonListener called when button in row is clicked.
  * @author Barry Becker
  */
class ParentTable(val scenes: Seq[Scene], val prevScene: Option[Scene],
                  var tableButtonListener: TableButtonListener) extends TableBase {
  initColumnMeta(PARENT_COLUMN_NAMES)
  initializeTable(scenes.asInstanceOf[Seq[_]])

  /** Add a row based on a player object
    * @param scene scene to add
    */
  override def addRow(scene: Any): Unit = {
    val parentScene = scene.asInstanceOf[Scene]
    val d = new Array[AnyRef](getNumColumns)
    d(NAVIGATED_FROM_INDEX) =
      (prevScene.isDefined && prevScene.get.name == parentScene.name).asInstanceOf[AnyRef]
    d(NAVIGATE_TO_INDEX) = parentScene.name
    d(NUM_CHILDREN_INDEX) = parentScene.getChoices.length.asInstanceOf[AnyRef]
    getParentTableModel.addRow(d)
  }

  override def updateColumnMeta(columnMeta: Array[TableColumnMeta]): Unit = {
    val navigateCol = columnMeta(NAVIGATE_TO_INDEX)
    val navCellEditor = new TableButton(NAVIGATE_TO_INDEX, NAVIGATE_TO_PARENT_BUTTON_ID)
    navCellEditor.addTableButtonListener(tableButtonListener)
    navigateCol.setCellRenderer(navCellEditor)
    navigateCol.setCellEditor(navCellEditor)
    navigateCol.setPreferredWidth(210)
    navigateCol.setMaxWidth(500)
    columnMeta(NAVIGATED_FROM_INDEX).setPreferredWidth(10)
    columnMeta(NUM_CHILDREN_INDEX).setMinWidth(40)
    columnMeta(NUM_CHILDREN_INDEX).setPreferredWidth(100)
  }

  override def createTableModel(columnNames: Array[String]) =
    new ParentTableModel(columnNames.asInstanceOf[Array[AnyRef]], 0)

  private[editor] def getParentTableModel =
    table.getModel.asInstanceOf[DefaultTableModel]
}
