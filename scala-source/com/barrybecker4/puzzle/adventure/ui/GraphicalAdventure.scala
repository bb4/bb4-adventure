// Copyright by Barry G. Becker, 2000-2018. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import com.barrybecker4.common.xml.DomUtil
import com.barrybecker4.puzzle.adventure.Story
import com.barrybecker4.puzzle.adventure.ui.editor.StoryEditorDialog
import com.barrybecker4.ui.application.ApplicationApplet
import com.barrybecker4.ui.dialogs.PasswordDialog
import com.barrybecker4.ui.util.GUIUtil
import org.w3c.dom.Document
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File


/**
  * Run your own adventure story.
  * This version runs the adventure in Graphical mode (with images and sound).
  * @see TextAdventure
  * @author Barry Becker
  */
object GraphicalAdventure extends App {

  val document = Story.importStoryDocument(args)
  new GraphicalAdventure(Array(), new Story(document))

  /** @param file name of the xml document to import.
    * @return the imported story xml document.
    */
  private def importStoryDocument(file: File): Document = {
    var document: Document = null
    // first try to load it as a file. If that doesn't work, try as a URL.
    if (file.exists) document = DomUtil.parseXMLFile(file)
    document
  }
}

object GraphicalAdventureConsts {
  /** The top secret password - don't tell anyone.
    * This could be Base64 encoded or encrypted to make more secure.
    */
  private[ui] val PASSWORD = "ludlow" //NON-NLS
}

/**
  * @param story initial story to show.
  */
final class GraphicalAdventure(args: Array[String], var story: Story)
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

  def this() {
    this(Array[String](), new Story(Story.importStoryDocument(Array())))
  }

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
    choicePanel = new ChoicePanel(story.getCurrentScene.choices.get)
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

  /** Allow user to edit the current story if they know the password. */
  def editStory(): Unit = { // show password dialog.
    val pwDlg = new PasswordDialog(GraphicalAdventureConsts.PASSWORD)
    val canceled = pwDlg.showDialog
    if (canceled) return
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
    val story = new Story(GraphicalAdventure.importStoryDocument(file))
    setStory(story)
  }

  /** @param fPath fully qualified filename and path to save to.*/
  def saveStory(fPath: String): Unit = {
    getStory.saveStoryDocument(fPath)
    storyEdited = false
  }

  /** called when a button is pressed. */
  override def sceneChanged(selectedChoiceIndex: Int): Unit = {
    story.advanceScene(selectedChoiceIndex)
    refresh()
    choicePanel.setChoices(story.getCurrentScene.choices.get)
    story.getCurrentScene.playSound()
  }

  override def getSize = new Dimension(1000, 700)

  /** Entry point for applet. */
  override def init(): Unit = {
    super.init()
    if (story == null) {
      val document = Story.importStoryDocument(Array[String]())
      val story = new Story(document)
      setStory(story)
    }
  }
}
