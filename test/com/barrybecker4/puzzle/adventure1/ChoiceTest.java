// Copyright by Barry G. Becker, 2017. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.puzzle.adventure1;


import com.barrybecker4.puzzle.adventure.Choice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Barry Becker
 */
public class ChoiceTest {

    @Test
    public void testChoiceConstruction() throws Exception {
        com.barrybecker4.puzzle.adventure.Choice choice = new Choice("room", "parlor");
        assertEquals("Unexpected desc", "room", choice.getDescription());
        assertEquals("Unexpected destination", "parlor", choice.getDestination());
    }
}
