package cs540.checkers.ui;
import cs540.checkers.*;

import java.awt.HeadlessException;

/**
 * This checkers player solicits a move from a interactive user using a 
 * {@link CheckersBoard CheckersBoard} widget. A reference to the widget must be 
 * set by {@link #setCheckersBoardWidget setCheckersBoardWidget} before 
 * {@link #calculateMove calculateMove} may be called. 
 * @see CheckersBoardModel CheckersBoardModel
 * @author Justin Tritz
 * @author David He
 */
public class HumanPlayer extends CheckersPlayer implements MoveListener
{
    /**
     * The model of the {@link CheckersBoard CheckersBoard} widget on which to 
     * acquire moves. This must be set using 
     * {@link #setCheckersBoardWidget setCheckersBoardWidget} before 
     * {@link #calculateMove calculateMove} may be called.
     */
    protected CheckersBoardModel cbwidget;

    /**
     * Constructs a human checkers player on the specified side with the given name.
     * @param name      the name of this player
     * @param side      the side this player is on
     */
    public HumanPlayer(String name, int side) 
    { 
        super(name, side);
        cbwidget = null;
    }

    /**
     * Selects a move using the {@link CheckersBoard CheckersBoard} widget.
     * This method prompts the CheckersBoard widget to acquire a move,
     * registers a callback to it, and blocks until an valid move has been 
     * selected. 
     * <p>
     * This method throws an unchecked {@link HeadlessException HeadlessException} 
     * if the reference to the checkers board widget is not set.
     * @param bs        the board state for which to acquire a move for
     */
    public synchronized void calculateMove(int[] bs)
    {
        if (cbwidget == null)
            throw new HeadlessException();

        cbwidget.setEnabled(side);

        cbwidget.addMoveListener(this);

        try {
            wait();
        } catch (InterruptedException e) { }
    }

    public synchronized void moveSelected(MoveEvent e)
    {
        setMove(e.getMove());
        cbwidget.removeMoveListener(this);
        notify();
    }

    public boolean isHuman()
    {
        return true;
    }

    /**
     * Sets the {@link CheckersBoard CheckersBoard} widget from which to acquire moves.
     * @param cb        the <code>CheckersBoardModel</code> to acquire moves from
     */
    public void setCheckersBoardWidget(CheckersBoardModel cb)
    {
        this.cbwidget = cb;
    }
}
