// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import javax.swing.table.DefaultTableModel
import scala.collection.mutable
import ChildTable._
import com.barrybecker4.puzzle.adventure.model.{Choice, Scene}


/**
  * Basically the DefaultTableModel with a few customizations.
  * @author Barry Becker
  */
class ChildTableModel(columnNames: Array[AnyRef], rowCount: Int)
  extends DefaultTableModel(columnNames, rowCount) {

  def this(data: Array[Array[AnyRef]], columnNames: Array[AnyRef]) {
    this(columnNames, 0)
    throw new UnsupportedOperationException("This constructor is not supported")
  }

  /** Make the text for the scene choice descriptions match the scene passed in.
    * Also the order may have changed, so that needs to be checked as well.
    * @param currentScene scene to update to.
    */
  def updateSceneChoices(currentScene: Scene): Unit = {
    val choiceMap = new mutable.LinkedHashMap[String, String]()
    var i = 0
    while (i < getRowCount) {
      val dest = getValueAt(i, NAVIGATE_INDEX).asInstanceOf[String]
      choiceMap += dest -> getValueAt(i, CHOICE_DESCRIPTION_INDEX).toString
      i += 1
    }
    currentScene.choices.update(choiceMap)
  }

  def getChoiceDescription(row: Int): String =
    this.getValueAt(row, CHOICE_DESCRIPTION_INDEX).asInstanceOf[String]

  /** Set the scene name of the current add row and add another add row.
    * @param row            location to add the new choice
    * @param addedSceneName name pf the scene to add.
    */
  def addNewChildChoice(row: Int, addedSceneName: String, initialLabel: String): Unit = {
    val d = new Array[AnyRef](this.getColumnCount)
    d(NAVIGATE_INDEX) = addedSceneName
    d(CHOICE_DESCRIPTION_INDEX) = initialLabel
    this.insertRow(row, d)
    this.fireTableRowsInserted(row, row) // need this
  }

  override def getColumnClass(col: Int): Class[_] = {
    dataVector.elementAt(0) match {
      case list: List[_] => list(col).getClass
      case vec: java.util.Vector[_] => vec.get(col).getClass
    }
  }

  override def isCellEditable(row: Int, column: Int) = true

  override def setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int): Unit = {
    assert(columnIndex == CHOICE_DESCRIPTION_INDEX)
    val rowVector = dataVector.elementAt(rowIndex).asInstanceOf[java.util.Vector[String]]
    rowVector.set(columnIndex, aValue.asInstanceOf[String])
    fireTableCellUpdated(rowIndex, columnIndex)
  }
}
