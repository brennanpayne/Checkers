package cs540.checkers;

import java.util.*;

/**
 * This controller progresses a checkers game by acting on an instance of 
 * {@link CheckersModel CheckerModel}. The crux of this controller's functionality 
 * resides in {@link #step step}, which traverses the state transition table of 
 * <code>CheckersModel</code> one step at a time. <code>step</code> is repeatedly
 * called within {@link #loop loop} until the game can not progress without 
 * external stimuli. Examples of external stimuli include a mouse click, 
 * a timer event, or a move selection. Certain types of external stimuli have 
 * predictable timing - <code>step</code> will indicate in its return code when 
 * these stimuli are expected to occur. <code>loop</code> will then use this 
 * return value to resume looping at the indicated time. 
 * <p>
 * By default, the transition between <code>WAITING</code> to 
 * <code>READY</code> occurs after the active player selects a move. However, 
 * if the <i>turn limit</i> option <code>turnLimit</code> is non-negative, the 
 * transition from WAITING to <code>READY</code> will occur after 
 * <code>turnLimit</code> milliseconds, regardless of whether the active player 
 * has finished selecting its move. 
 * <p>
 * The design of <code>CheckersController</code> follows thread-safety 
 * guidelines. 
 * @see CheckersModel CheckersModel
 * @author David He
 */
public class CheckersController
{
    /** The <code>CheckersModel</code> controlled by this class. */
    protected CheckersModel model;

    /** 
     * The <code>TurnAgent</code> which controls the computation thread.
     */
    protected TurnAgent turnAgent;

    /**
     * The timer used to wake up <code>loop</code> to process events in the future.
     */
    protected Timer timer;

    /**
     * The {@link CountdownClock CountdownClock} used to enforce turn limits. 
     * Each side has its own timer.
     */
    protected CountdownClock[] turnClock;

    protected static final int BREAK_LOOP = -1;
    protected static final int CONTINUE_LOOP = 0;

    /**
     * Creates a <code>CheckersController</code> for the given model with 
     * default settings. By default, turn limits are disabled for both players.
     * @param model         the model to control
     */
    public CheckersController(CheckersModel model)
    {
        this(model, new long[] {-1, -1});
    }

    /**
     * Creates a <code>CheckersController</code> for the given model with 
     * the specified turn time controls.
     * @param model         the model to control
     * @param turnLimit     time in milliseconds the sides have to make a move
     */
    public CheckersController(CheckersModel model, long[] turnLimit)
    {
        this.model = model;
        
        /* Create the turn clock, which enforces for how long each player can
         * think per turn */
        turnClock = new CountdownClock[2];
        for (int i = 0; i < 2; i++)
            turnClock[i] = new DefaultCountdownClock(turnLimit[i]);

        turnAgent = new TurnAgent();
        turnAgent.setCallbackController(this);
        timer = new Timer(true);
    }

    /**
     * Starts a timer which invokes <code>loop(false)</code> at
     * <code>delayTime</code> milliseconds in the future.
     * @param delayTime    the number of milliseconds to wait before calling run.
     * @see #loop loop
     */
    public void loopLater(long delayTime)
    {
        TimerTask task = new TimerTask()
        {
            public void run() { loop(); }
        };
        timer.schedule(task, delayTime);
    }

    /**
     * Advances the checkers game by repeatedly calling <code>step</code> 
     * until the game cannot be progressed further from its present state. 
     * If the return value of <code>step</code> indicates that the game may
     * progress at a specific time in the future, this method schedules a 
     * timer which calls <code>loop</code> at that time.
     * <p>
     * This method is idempotent.
     * @see #step step
     */
    public synchronized void loop()
    {
        long sleepTime = 0;
        while (sleepTime == CONTINUE_LOOP)
        {
            try {
                sleepTime = step();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (sleepTime > 0)
            loopLater(sleepTime);
    }

    /**
     * Attempts to progress the checkers game one step at a time. If some
     * progress was made, such as a state transition in the 
     * <code>CheckersModel</code> this controller acts on, this method returns
     * <code>CONTINUE_LOOP == 0</code>. Otherwise, if the game cannot progress 
     * from its current state without some external stimulus (such as a mouse 
     * click, move selection, or countdown timer completion), this method 
     * returns an integer specifying how many milliseconds in the future the 
     * caller can expect one of these stimuli to occur; the caller should 
     * re-invoke this method at the specified time in the future. However, if 
     * game progression depends on an external stimulus that occurs 
     * indefinitely in the future (such as acquiring a move from 
     * <code>ui.HumanPlayer</code>), this method returns 
     * <code>BREAK_LOOP == -1</code>.
     * <p>
     * This method is nilpotent, and is intended to be called from within a loop.
     * <p>
     * Please note that a return value of <code>0</code> does not necessarily 
     * imply that a state transition in <code>CheckerModel</code> was induced. 
     * @return              an integer specifying how many milliseconds in the
     *                      future the caller should re-invoke this method;
     *                      <code>CONTINUE_LOOP</code> if this method should be 
     *                      re-invoked right away; and <code>BREAK_LOOP</code> 
     *                      if unknown or indeterminate
     */
    protected long step()
    {
        switch (model.getState())
        {
            case ANTE:     return stepAnte();
            case READY:    return stepReady();
            case WAITING:  return stepWaiting();
            case FINISHED: return BREAK_LOOP;
            case INVALID:  return BREAK_LOOP;
            default:       return BREAK_LOOP;
        }
    }

    /**
     * Helper method for {@link #step step}.
     */
    protected long stepAnte()
    {
        model.startGame();
        return CONTINUE_LOOP;
    }

    /**
     * Helper method for {@link #step step}.
     */
    protected long stepWaiting()
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* If a player has chosen a move, use it. */
        if (turnAgent.hasMove())
        {
            /* Execute the move, and continue loop */
            try {
                model.makeMove(turnAgent.getMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }
            return CONTINUE_LOOP;
        }

        /* 
         * If the player has used up the allocalated per-turn time,
         * forcefully obtain a move from the player and execute it. 
         * Otherwise sleep for timeRemain milliseconds and and check 
         * this condition again after waking up. 
         */
        if (turnClock[side].getState() == CountdownClock.FINISHED)
        {
            /* Stop calculation and forcefully obtain a move */
            turnAgent.stopCalculate();

            try { 
                model.makeMove(turnAgent.getForcedMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }
            return CONTINUE_LOOP;
        }
        else
            return turnClock[side].getTimeRemain();
    }

    /**
     * Helper method for {@link #step step}.
     */
    protected long stepReady()
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* Set model state to WAITING */
        model.startWaiting();

        /* Start the turn clock that enforces term limits */
        turnClock[side].reset();
        turnClock[side].resume();

        /* Begin calculations with a TurnAgent */
        turnAgent.startCalculate(player, model.getBoardState());

        return CONTINUE_LOOP;
    }

    /**
     * Terminates the checkers game. This method crashes the game if it is ongoing. 
     * Otherwise, this method does nothing. This is called when the UI exits, 
     * among other situations.
     * @param reason        the reason for terminating the checkers game 
     */
    public synchronized void terminateGame(String reason)
    {
        turnAgent.stopCalculate();

        if (model.getState() == CheckersModel.State.READY ||
            model.getState() == CheckersModel.State.WAITING )
            model.crashGame(reason);
    }

    /**
     * Sets how long the specified side has to select a move each turn, or 
     * <code>-1</code> for no limit. 
     * @param side          an integer representing the side
     * @param limit         if non-negative, time, in milliseconds 
     *                      <code>side</code> may use in selecting a move each 
     *                      turn; if <code>-1</code>, no limit
     */
    public void setTurnLimit(int side, long limit)
    { 
        turnClock[side].setDelay(limit);
    }

    /**
     * Gets how long the specified side has to select a move each turn.
     * @param side          an integer representing the side
     * @return              time, in milliseconds, <code>side</code> may use 
     *                      in selecting a move each turn; or <code>-1</code>
     *                      if no limit
     */
    public long getTurnLimit(int side) { return turnClock[side].getDelay(); }
}
