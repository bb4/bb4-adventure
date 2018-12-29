// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure.ui

import com.barrybecker4.puzzle.adventure.Choice
import com.barrybecker4.puzzle.adventure.ChoiceList
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


/**
  * This panel shows a list of options for what to do next.
  * You press the button to take the action.
  * @author Barry Becker
  */
class ChoicePanel(val choices: ChoiceList) extends JPanel with ActionListener {
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  private var sceneChangeListeners: Set[SceneChangeListener] = Set()
  setChoices(choices)

  /** Update the list of options shown
    * @param choices the choices
    */
  def setChoices(choices: ChoiceList): Unit = {
    this.removeAll()
    // for each choice add a button and text.
    var i = 1
    for (choice <- choices.choices) {
      addOption(i, choice)
      i += 1
    }
    this.revalidate()
    this.repaint()
  }

  def addSceneChangeListener(listener: SceneChangeListener): Unit =
    sceneChangeListeners += listener

  def removeSceneChangeListener(listener: SceneChangeListener): Unit =
    sceneChangeListeners -= listener

  private def addOption(index: Int, choice: Choice): Unit = {
    val choiceElement = new JPanel
    choiceElement.setLayout(new BoxLayout(choiceElement, BoxLayout.X_AXIS))
    val button = new JButton(Integer.toString(index))
    button.addActionListener(this)
    val label = new JLabel(choice.description)
    choiceElement.add(button)
    choiceElement.add(label)
    choiceElement.add(Box.createHorizontalGlue)
    this.add(choiceElement)
  }

  /** Called when a button is pressed. */
  override def actionPerformed(e: ActionEvent): Unit = {
    val sourceButton = e.getSource.asInstanceOf[JButton]
    val selectedChoiceIndex = sourceButton.getText.toInt - 1
    for (listener <- sceneChangeListeners) {
      listener.sceneChanged(selectedChoiceIndex)
    }
  }
}