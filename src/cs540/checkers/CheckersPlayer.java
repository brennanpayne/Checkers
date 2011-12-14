package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

/**
 * This abstract class provides a context for a checkers player to calculate
 * its move. All checkers players must inherit this class and implement the
 * abstract {@link #calculateMove calculateMove} method. 
 * @author Justin Tritz
 * @author David He
 */
public abstract class CheckersPlayer 
{
    /** The name of this player. */
    protected String name;

    /** The side that this player is on. */
    protected int side;

    /**
     * The move chosen by this player when <code>calculateMove</code>
     * method is called.  The player should update this variable as
     * soon as it finds a better move, because the calculateMove method
     * will be interrupted by a timer after some amount of time.
     * @see #calculateMove calculateMove
     */
    protected volatile Move chosenMove;

    /**
     * The maximum depth of the iterative deepening search. This parameter 
     * assumes that all checkers players use an iterative deepening search.
     * This assumption is not valid, and thus, derived classes are not
     * required to respect this parameter. Derived classes should still respect
     * this parameter when it makes sense. Students' alpha-beta players 
     * must respect this parameter in order to be graded correctly. 
     * <p>
     * <code>depthLimit</code> is indexed from <code>0</code>, as follows:
     * No expansion occurs when <code>depthLimit</code> is <code>0</code>;
     * with <code>depthLimit == 1</code>, the game tree stops at board
     * states reachable within one move. Note that pruning does not occur until 
     * <code>depthLimit &gt;= 3</code>. 
     * <p>
     * Classes may assume that value is positive, even though 
     * <code>depthLimit == 0</code> makes semantic sense.
     * <p>
     * By default, this value is set to some high value (like <code>1000</code>),
     * so iterative deepening continues until time runs out. 
     */
    protected int depthLimit;

    /**
     * Constructs a CheckersPlayer on the specified side with the given name.
     * This should be called via <code>super(...)</code> whenever a subclass is 
     * created.
     * @param name      the name of this player
     * @param side      the side this player is on
     */
    public CheckersPlayer(String name, int side)
    {
        this.name = name;
        this.side = side;
        depthLimit = 1000;
    }

    /**
     * Calculates the best move to make for the given board state.
     * <p>
     * Implementations of this method can except the following guarantees:
     * <ul>
     * <li>This method will run inside its own thread;
     * <li>At least one move can be made from the given board state, i.e. 
     * {@link Utils#getAllPossibleMoves Utils.getPossibleMoves} will not return
     * an empty list;
     * <li>This method is called exactly once on every turn this side makes; and
     * <li>The board state provided is the result of a legal sequence of moves.
     * </ul>
     * @param bs        the board state for which to calculate the best move
     */
    public abstract void calculateMove(int[] bs);

    /**
     * Retrieve the most recent move chosen by this player.
     * @return the move chosen by this player
     */
    public final Move getMove()
    {
        return chosenMove;
    }

    /**
     * Sets the most recent move chosen by this player. 
     * <p>
     * Subclasses may call this method to change the chosen move or access
     * <code>chosenMove</code> directly.
     * @param move      the most recent move chosen by this player
     */
    protected synchronized void setMove(Move move)
    {
        this.chosenMove = move;
    }

    /**
     * Gets the name of this player.
     * @return      the string containing the name of this player
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the maximum iterative depth of the iterative deepening search. This
     * method must not be called while this player is calculating a move. 
     * @param depthLimit    the maximum depth of the iterative deepening 
     *                      search. <code>depthLimit</code> must be positive. 
     */
    public void setDepthLimit(int depthLimit)
    {
        this.depthLimit = depthLimit;
    }

    /**
     * Gets the maximum iterative depth of the iterative deepening search.
     * @return              the maximum depth of the iterative deepening 
     *                      search
     */
    public int getDepthLimit()
    {
        return depthLimit;
    }

    /**
     * Returns a string representation of this checkers player.
     * @return      a string representation of this checkers player
     */
    public String toString()
    {
        return name;
    }

    /**
     * Returns whether this checkers player primarily solicits moves from an
     * interactive player.  This information is used by CheckersController so 
     * that interactive players may circumvent time controls.
     * @return          true if this class is a human player; false otherwise
     */
    public boolean isHuman()
    {
        return false;
    }
}
