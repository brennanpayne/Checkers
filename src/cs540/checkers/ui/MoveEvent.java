package cs540.checkers.ui;
import cs540.checkers.*;

import java.util.EventObject;

/**
 * MoveEvent is used to notify interested parties that a move has been selected.
 * @author David He
 */
public class MoveEvent extends EventObject
{
    protected Move move;

    /**
     * Constructs a MoveEvent object with the selected move.
     * @param move the selected move
     */
    public MoveEvent(Object source, Move move)
    {
        super(source);
        this.move = move;
    }

    /**
     * Gets the selected move.
     * @return the selected move
     */
    public Move getMove()
    {
        return move;
    }
}
