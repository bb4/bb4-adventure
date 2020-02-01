// Copyright by Barry G. Becker, 2000-2019. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import com.barrybecker4.puzzle.adventure.ui.editor.StoryEditorDialog
import com.barrybecker4.ui.application.ApplicationApplet
import com.barrybecker4.ui.dialogs.PasswordDialog
import com.barrybecker4.ui.util.GUIUtil
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import com.barrybecker4.puzzle.adventure.model.io.{StoryExporter, StoryImporter}
import com.barrybecker4.puzzle.adventure.model.Story


/**
  * @param story initial story to show.
  */
final class GraphicalAdventure(args: Array[String],
                               var story: Story, editPassword: String = null)
  extends ApplicationApplet(args) with SceneChangeListener {

  val frame: JFrame = GUIUtil.showApplet(this)
  val storyMenu = new StoryMenu(this)
  val menubar = new JMenuBar
  menubar.add(storyMenu)
  frame.setJMenuBar(menubar)
  frame.invalidate()
  frame.validate()
  private var choicePanel: ChoicePanel = _
  private var mainPanel: JPanel = _
  private var storyEdited: Boolean = false

  override def getName: String = story.getTitle

  /** Build the user interface with parameter input controls at the top. */
  override def createMainPanel: JPanel = {
    mainPanel = new JPanel
    mainPanel.setLayout(new BorderLayout)
    setStory(story)
    mainPanel
  }

  /** If a new story is loaded, call this method to update the ui.
    * @param story new story to present.
    */
  def setStory(story: Story): Unit = {
    if (story == null) return
    mainPanel.removeAll()
    this.story = story
    val storyPanel = new StoryPanel(this.story)
    // setup for initial scene
    choicePanel = new ChoicePanel(story.getCurrentScene.choices)
    story.getCurrentScene.playSound()
    choicePanel.addSceneChangeListener(this)
    mainPanel.add(storyPanel, BorderLayout.CENTER)
    mainPanel.add(choicePanel, BorderLayout.SOUTH)
    refresh()
  }

  def getStory: Story = story

  private[ui] def refresh(): Unit = {
    mainPanel.invalidate()
    mainPanel.validate()
    mainPanel.repaint()
  }

  /** Allow user to edit the current story if they know the password.
    * If expected pw is null, they do not need to enter one.
    */
  def editStory(): Unit = {
    if (editPassword != null) {
      val pwDlg = new PasswordDialog(editPassword)
      val canceled = pwDlg.showDialog
      if (canceled) return
    }

    val storyEditor = new StoryEditorDialog(new Story(story))
    val editingCanceled = storyEditor.showDialog
    if (!editingCanceled) { // show the edited version.
      story.initializeFrom(storyEditor.getEditedStory)
      story.resetToFirstScene()
      setStory(story)
      storyEdited = true
    }
  }

  def isStoryEdited: Boolean = storyEdited

  def loadStory(file: File): Unit = {
    println("parent = " + file.getParent)
    val matchPrefix = "com" + File.separatorChar
    val idx = file.getParent.indexOf(matchPrefix)
    val folder = file.getParent.substring(idx)
    val story = new StoryImporter(file.getName, folder + File.separatorChar).getStory
    setStory(story)
  }

  /** @param fPath fully qualified filename and path to save to.*/
  def saveStory(fPath: String): Unit = {
    StoryExporter(getStory).saveTo(fPath)
    storyEdited = false
  }

  /** called when a button is pressed. */
  override def sceneChanged(selectedChoiceIndex: Int): Unit = {
    story.advanceScene(selectedChoiceIndex)
    refresh()
    choicePanel.setChoices(story.getCurrentScene.choices)
    story.getCurrentScene.playSound()
  }

  override def getSize = new Dimension(1000, 700)

  /** Entry point for applet. */
  override def init(): Unit = {
    super.init()
    if (story == null) {
      val story = new StoryImporter(Array[String]()).getStory
      setStory(story)
    }
  }
}
