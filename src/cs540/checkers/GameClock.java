package cs540.checkers;

/**
 * This interface defines the methods of a game clock.
 * <p>
 * A game clock to implement this interface updates its state only when one of 
 * its methods is invoked. The game clock will not generate events. 
 * Code which uses the game clock must poll its status periodically. 
 * @see <a href="http://en.wikipedia.org/wiki/Game_clock">Description and 
 * photographs of <i>game clock</i></a>
 *
 * @author David He
 */
public interface GameClock
{
    /** The value indicating the PAUSED state */
    public static final int PAUSED    = 0;
    /** The value indicating the RUNNING state */
    public static final int RUNNING   = 1;
    /** The value indicating the FINISHED state */
    public static final int FINISHED  = 2;

    /** 
     * Presses the button of the active player. This will stop the clock 
     * against the active player and start the clock against the opposing 
     * player. The opposing player then becomes the active player. 
     */
    public void press();
    
    /** Pauses this game clock. */
    public void pause();

    /** Resumes this game clock. */
    public void resume();

    /**
     * Returns how much time, in milliseconds, the specified side has used
     * so far in the game.
     * @param side      the side to query
     * @return          how much time <code>side</code> has used so far,
     *                  in milliseconds
     */
    public long getGameTime(int side);

    /**
     * Returns how much time, in milliseconds, the specified side has 
     * remaining in the game.
     * @param side      the side to query
     * @return          how much time <code>side</code> has remaining, in 
     *                  milliseconds
     */
    public long getGameTimeRemain(int side);

    /**
     * Returns how much time, in milliseconds, he specified side has 
     * used in its most recent turn. The most recent turn is the ongoing turn
     * if <code>side</code> is the active side, and the previous turn if 
     * otherwise. This method returns <code>0</code> if the most recent turn
     * does not exist. 
     * @param side      the side to query
     * @return          how much time <code>side</code> has used in its most
     *                  recent turn, in milliseconds.
     */
    public long getTurnTime(int side);

    /**
     * Gets the active side.
     * @return          the integer specifying the side that is active
     */
    public int getSide();

    /**
     * Gets the state of this game clock.
     * @return          the integer specifying the state of this clock
     */
    public int getState();
}
