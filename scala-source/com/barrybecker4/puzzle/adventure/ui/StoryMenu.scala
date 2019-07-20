// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import com.barrybecker4.common.util.FileUtil
import com.barrybecker4.puzzle.adventure.StoryImporter
import com.barrybecker4.ui.file.ExtensionFileFilter
import com.barrybecker4.ui.file.FileChooserUtil
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File


object StoryMenu {
  private val EXT = "xml"
}

/**
  * File menu for story application.
  * You can open, save, or edit a story file.
  * @param storyApp the initially selected game.
  * @author Barry Becker
  */
class StoryMenu(var storyApp: GraphicalAdventure) extends JMenu("Story") with ActionListener {

  this.setBorder(BorderFactory.createEtchedBorder)
  setBorder(BorderFactory.createEtchedBorder)
  private var openItem = createMenuItem("Open")
  private var saveItem  = createMenuItem("Save")
  private var editItem = createMenuItem("Edit")
  private var exitItem = createMenuItem("Exit")
  add(openItem)
  add(saveItem)
  add(editItem)
  add(exitItem)

  /** Called when the user has selected a different story file option.
    * @param e action event
    */
  override def actionPerformed(e: ActionEvent): Unit = {
    val item = e.getSource.asInstanceOf[JMenuItem]
    if (item eq openItem) openStory()
    else if (item eq saveItem) saveStory()
    else if (item eq editItem) storyApp.editStory()
    else if (item eq exitItem) if (confirmExit) System.exit(0)
    else assert(false, "unexpected menuItem = " + item.getName)
  }

  /** If there are modifications, confirm before exiting.
    * @return true if exiting was confirm or if no edit was made so confirm not needed.
    */
  private def confirmExit: Boolean = {
    if (storyApp.isStoryEdited) {
      val choice = JOptionPane.showConfirmDialog(this,
        "You have unsaved changes. Are you sure you want to exit?",
        "Confirm Quit", JOptionPane.YES_NO_OPTION)
      if (choice == JOptionPane.NO_OPTION)
        return false
    }
    true
  }

  /** View the story that the user opens from the file chooser. */
  private def openStory(): Unit = {
    val file = FileChooserUtil.getSelectedFileToOpen(StoryMenu.EXT, getDefaultDir)
    if (file != null) storyApp.loadStory(file)
  }

  /** Save the current story to a file. */
  private def saveStory(): Unit = {
    val file = FileChooserUtil.getSelectedFileToSave(StoryMenu.EXT, getDefaultDir)
    if (file != null) { // if it does not have the .sgf extension already then add it
      var fPath = file.getAbsolutePath
      fPath = ExtensionFileFilter.addExtIfNeeded(fPath, StoryMenu.EXT)
      storyApp.saveStory(fPath)
    }
  }

  private def getDefaultDir = {
    val defaultDir = FileUtil.getHomeDir + "source/" + StoryImporter.DEFAULT_STORIES_ROOT
    new File(defaultDir)
  }

  /** Create a menu item.
    * @param name name of the menu item. The label.
    * @return the menu item to add.
    */
  private def createMenuItem(name: String): JMenuItem = {
    val item = new JMenuItem(name)
    item.addActionListener(this)
    item
  }
}
