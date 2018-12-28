// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui.editor

import javax.swing.table.DefaultTableModel


/** Basically the DefaultTableModel with a few customizations
  * @author Barry Becker
  */
@SerialVersionUID(0)
class ParentTableModel(columnNames: Array[AnyRef], rowCount: Int)
  extends DefaultTableModel(columnNames, rowCount) {

  def this(data: Array[Array[AnyRef]], columnNames: Array[AnyRef]) {
    this(columnNames, 0)
    throw new UnsupportedOperationException("This constructor not supported")
  }

  override def getColumnClass(col: Int): Class[_] = {
    dataVector.elementAt(0) match {
      case list: List[_] => list(col).getClass
      case vec: java.util.Vector[_] => vec.get(col).getClass
    }
    //val v = dataVector.elementAt(0).asInstanceOf[List[_]]
    //v.get(col).getClass
  }

  override def isCellEditable(row: Int, column: Int): Boolean = column == ParentTable.NAVIGATE_INDEX
}
