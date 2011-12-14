package cs540.checkers;

import java.util.*;

/**
 * This class represents a move on a checkers board.
 * A move by a checkers piece consists of a sequence of locations. 
 * The starting location of the checkers piece is the first item in the 
 * sequence, and subsequent locations are the remaining items in the 
 * sequence.
 * <p>
 * For example, a walk move is represented as a sequence of length two.
 * The first item is the starting location; the second item is the ending
 * location.
 *
 * @see Move Move
 * @author Justin Tritz
 * @author David He
 */

public class MutableMove extends ArrayList<Integer>
{
    public MutableMove() { ensureCapacity(8); }
    public MutableMove(Collection<Integer> m) { super(m); }

    /**
     * Returns this move formatted as a string.
     * @return this move formatted as a string
     */
    public String toString()
    {
        return Utils.reprMove(this);
    }
}
