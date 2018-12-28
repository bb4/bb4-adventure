// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure1;

import com.barrybecker4.puzzle.adventure.Choice;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * A choice that you can make in a scene.
 *
 * @author Barry Becker
 */
@SuppressWarnings("ClassWithTooManyMethods")
public class ChoiceList implements List<com.barrybecker4.puzzle.adventure.Choice> {

    private List<com.barrybecker4.puzzle.adventure.Choice> choices;


    /**
     * Default constructor.
     */
    ChoiceList() {
        choices = new ArrayList<>();
    }

    /**
     * Constructor.
     * @param scene use the choices from this scene to initialize from.
     */
    ChoiceList(Scene scene) {
        this();
        choices.addAll(scene.getChoices());
    }

    /**
     * Constructor.
     * Create an initialized choice list.
     * @param sceneNode to initialize from.
     * @param isFirst true if this is the first scene.
     */
    ChoiceList(Node sceneNode, boolean isFirst) {
        // if there are choices they will be the second element (right after description).
        NodeList children = sceneNode.getChildNodes();
        if (children.getLength() > 1) {
            Node choicesNode = children.item(1);
            NodeList choiceList = choicesNode.getChildNodes();
            int numChoices = choiceList.getLength();
            choices = new ArrayList<Choice>(numChoices + (isFirst? 0 : 1));
            for (int i=0; i<numChoices; i++) {
                assert choiceList.item(i) != null;
                choices.add(new com.barrybecker4.puzzle.adventure.Choice(choiceList.item(i)));
            }
        } else {
            choices = new ArrayList<com.barrybecker4.puzzle.adventure.Choice>();
        }
    }

    /**
     * @param sceneName  sceneName to look for as a destination.
     * @return true if sceneName is one of our choices.
     */
    boolean isDestination(String sceneName) {
        for (com.barrybecker4.puzzle.adventure.Choice c : choices) {
            if (c.destinationScene().equals(sceneName)) {
                return true;
            }
        }
        return false;
    }

    void sceneNameChanged(String oldSceneName, String newSceneName) {
        for (com.barrybecker4.puzzle.adventure.Choice c : choices) {
            if (c.destinationScene().equals(oldSceneName)) {
                c.destinationScene = newSceneName;
            }
        }
    }

    /**
     * update the order and descriptions
     * @param choiceMap new order and descriptions to update with.
     */
    public void update(LinkedHashMap<String, String> choiceMap)  {
        assert choiceMap.size() == choices.size() :
                "choiceMap.size()=" + choiceMap.size() + " not equal choices.size()=" + choices.size();
        List<com.barrybecker4.puzzle.adventure.Choice> newChoices = new ArrayList<com.barrybecker4.puzzle.adventure.Choice>(choiceMap.size());
        for (String dest : choiceMap.keySet()) {
            newChoices.add(new com.barrybecker4.puzzle.adventure.Choice(choiceMap.get(dest), dest));
        }
        choices = newChoices;
    }

    public int size() {
        return choices.size();
    }

    public boolean isEmpty() {
        return choices.isEmpty();
    }

    public boolean contains(Object o) {
        return choices.contains(o);
    }

    public Iterator<com.barrybecker4.puzzle.adventure.Choice> iterator() {
        return choices.iterator();
    }

    public Object[] toArray() {
       return choices.toArray();
    }

    public boolean add(com.barrybecker4.puzzle.adventure.Choice choice) {
        return choices.add(choice);
    }

    public boolean remove(Object o) {
        return choices.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        choices.clear();
    }

    public com.barrybecker4.puzzle.adventure.Choice get(int index) {
        return choices.get(index);
    }

    public com.barrybecker4.puzzle.adventure.Choice set(int index, com.barrybecker4.puzzle.adventure.Choice element) {
        return  choices.set(index, element);
    }

    public void add(int index, com.barrybecker4.puzzle.adventure.Choice element) {
        choices.add(index, element);
    }

    public com.barrybecker4.puzzle.adventure.Choice remove(int index) {
        return choices.remove(index);
    }

    public int indexOf(Object o) {
        return choices.indexOf(o);
    }

    // unsupported stuff. implement only if needed.
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean addAll(Collection<? extends com.barrybecker4.puzzle.adventure.Choice> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean addAll(int index, Collection<? extends com.barrybecker4.puzzle.adventure.Choice> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ListIterator<com.barrybecker4.puzzle.adventure.Choice> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ListIterator<com.barrybecker4.puzzle.adventure.Choice> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public List<Choice> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
