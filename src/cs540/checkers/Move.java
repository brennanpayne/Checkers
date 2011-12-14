package cs540.checkers;

import java.util.*;

/**
 * This class represents an immutable move on a checkers board .
 * A move by a checkers piece consists of a sequence of locations. 
 * The starting location of the checkers piece is the first item in the 
 * sequence, and subsequent locations are the remaining items in the 
 * sequence.
 * <p>
 * For example, a walk move is represented as a sequence of length two.
 * The first item is the starting location; the second item is the ending
 * location.

 * @see MutableMove MutableMove
 * @author Justin Tritz
 * @author David He
 */

public class Move extends AbstractList<Integer>
{
    private List<Integer> m;

    public Move() { this.m = new ArrayList<Integer>(); }
    public Move(Collection<Integer> m) { this.m = new ArrayList<Integer>(m); }

    public Integer get(int index) { return m.get(index); }
    public int size() { return m.size(); }

    /**
     * Returns this move formatted as a string.
     * @return this move formatted as a string
     */
    public String toString()
    {
        return Utils.reprMove(this);
    }
}
