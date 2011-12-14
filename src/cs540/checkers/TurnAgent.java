package cs540.checkers;

/** 
 * This class provides an easy interface for controlling when a 
 * <code>CheckersPlayer</code> calculates its move. <code>TurnAgent</code> 
 * controls the <i>computation thread</i> on which the player computes it 
 * move, and provides methods to retrieve the move once it is selected.
 * <p>
 * Instances of <code>TurnAgent</code> are typically commissioned as a helper 
 * by a <code>CheckersController</code>, which may additionally provide a callback 
 * that <code>TurnAgent</code> invokes when its player finishes selecting a move. 
 * <p>
 * TurnAgent provides an abstraction of this move acquisition process. 
 * In the event that this mechanism is changed, only the implementation of 
 * this class needs to be modified.
 *
 * @see CheckersController CheckersController
 * @see CheckersPlayer CheckersPlayer
 * @author Justin Tritz
 * @author David He
 */
@SuppressWarnings("deprecation")
public class TurnAgent
{
    /** The current computation thread. */
    protected Thread thread;

    /** The checkers player currently associated with the thread. */
    protected CheckersPlayer cp;

    /** True if the checkers player is thinking; false otherwise */
    protected volatile boolean running;

    /** 
     * If not <code>null</code>, this <code>CheckersController</code> will be 
     * notified when a checkers player decides on a move. 
     */
    protected CheckersController callback_controller = null;

    /**
     * Constructs a <code>TurnAgent</code>.
     */
    public TurnAgent() { }
    
    /**
     * Starts a computation thread which will solicit the specified 
     * <code>CheckersPlayer</code> for the best move on the given board state.
     * The callback, if set, will be invoked when the computation is finished.
     * @param cp        the checkers player to solicit for the best move
     * @param bs        the board state to compute the move on
     */
    public synchronized void startCalculate(final CheckersPlayer cp, final int[] bs)
    {
        this.cp = cp;

        thread = new Thread() {
            public void run()
            {
                synchronized(cp)
                {
                    try {
                        cp.calculateMove(bs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    running = false;
                }

                /* Invoke the callback, if enabled */
                if (callback_controller != null)
                    callback_controller.loopLater(1);
            }
        } ;

        running = true;
        thread.start();
    }

    /**
     * Stops the computation thread.
     */
    public synchronized void stopCalculate()
    {
        //thread.interrupt();
        thread.stop();
        running = false;
    }

    /**
     * Returns whether the checkers player has selected a move.
     * @return move     true if the checkers player has selected a move,
     *                  false otherwise.
     * @see #getMove getMove
     */
    public synchronized boolean hasMove()
    {
        if (running)
            return false;
        else
            return true;
    }

    /**
     * Retrieves the move chosen by the checkers player. This method throws 
     * {@link IllegalStateException InvalidStateException} if the player has 
     * not finished computation. 
     * @return move     the move chosen by the checkers player
     * @see #hasMove hasMove
     */
    public synchronized Move getMove()
    {
        if (running)
            throw new IllegalStateException();

        Move move = cp.getMove();
        if (move == null)
            return null;
        else
            return new Move(move);
    }

    /**
     * Forcefully retrieves the move chosen by the checkers player. If the player
     * has not finished computation, this method returns the best move computed
     * so far. 
     * @return move     the move chosen by the checkers player, or the best move
     *                  so far if the player is still thinking
     */
    public synchronized Move getForcedMove()
    {
        Move move = cp.getMove();
        if (move == null)
            return null;
        else
            return new Move(move);
    }

    /**
     * Sets the {@link CheckersController CheckersController} to notify once 
     * the player finishes selecting a move. If <code>ctl</code> is <code>null</code>, 
     * this callback is disabled. The controller must be set <i>before</i> 
     * <code>startCalculate</code> is invoked in order for the notification
     * to take place. 
     * @param ctl       the <code>CheckersController</code> to notify when the 
     *                  player selects its move.
     * @see #startCalculate startCalculate
     */
    public synchronized void setCallbackController(CheckersController ctl)
    {
        this.callback_controller = ctl;
    }
}
