package cs540.checkers.ui;
import cs540.checkers.*;

import java.awt.event.*;
import javax.swing.event.*;

/**
 * This controller extends {@link CheckersController CheckersController} 
 * with additional functionality.
 * <p>
 * If the <code>moveOnClick</code> option is enabled, the <code>READY</code> 
 * to <code>WAITING</code> transition occurs only after a mouse click.
 * <p>
 * The transition from <code>READY</code> to <code>WAITING</code> occurs 
 * after a <i>turn delay</i>, which serves to improve visual clarity for 
 * CheckersUI. Otherwise, the transition occurs too rapidly to be noticed.
 * <p>
 * If the active player is an interactive player (as determined by 
 * {@link CheckersPlayer#isHuman CheckersPlayer.isHuman}), the transition from 
 * <code>WAITING</code> to <code>READY</code> will ignore the turn limit. 
 * {@link #step step} will never force a partially selected move from an 
 * interactive player.
 *
 * @author David He
 */
public class CheckersUIController extends CheckersController implements ActionListener
{
    /**
     * The {@link CountdownClock CountdownClock} that implements turnDelay, 
     * which inserts a small pause between the transition from <code>READY</code> 
     * to <code>WAITING</code>.
     */
    protected CountdownClock turnDelayClock;

    /**
     * Whether turn delay is enabled.
     */
    protected boolean turnDelay;

    /**
     * Whether the <code>READY</code> to <code>WAITING</code> transition 
     * requires a mouse click.
     */
    protected boolean[] moveOnClick;

    protected volatile boolean isClick;

    /**
     * Creates a <code>CheckersUIController</code> for the given model with 
     * default settings. By default, turn limits are disabled and 
     * <code>moveOnClick</code> is false for both players.
     * @param model         the model to control
     */
    public CheckersUIController(CheckersModel model)
    {
        this(model, new long[] {-1, -1}, new boolean[] {false, false});
    }

    /**
     * Creates a <code>CheckersUIController</code> for the given model with 
     * the specified turn time controls and <code>moveOnClick</code> settings.
     * @param model         the model to control
     * @param turnLimit     time in milliseconds the sides have to make a move
     * @param moveOnClick   whether to wait for an interactive click before each move
     */
    public CheckersUIController(CheckersModel model, long[] turnLimit, 
            boolean[] moveOnClick)
    {
        super(model, turnLimit);

        this.moveOnClick = moveOnClick.clone();

        /* Create the clock to enforce delays in between turns, which helps
         * bring clarity to the UI */
        turnDelayClock = new DefaultCountdownClock(-1);
        turnDelay = true;
        updateTurnDelay();
    }

    public void actionPerformed(ActionEvent e)
    {
        /* Mouse clocked */
        if (e.getSource() instanceof CheckersBoardModel)
        {
            isClick = true;
            loop();
        }
    }

    public synchronized void loop()
    {
        super.loop();
        isClick = false;
    }

    /**
     * Calculates this controller's turn delay based on the turn limit,
     * and applies the delay to the countdown clock. 
     * Turn delay is a pause between each turn, which helps bring clarity
     * to the CheckersUI. Turn delay is activated even when --step is not 
     * specified. It can, however, be overridden by a manual click. 
     * We scale the turn delay linearly with the allowed turn time for each 
     * player, clamped to 250ms and 1000ms.
     */
    protected void updateTurnDelay()
    {
        long turnDelayTime = 0;
        for (int i = 0; i < 2; i++)
            turnDelayTime += turnClock[i].getDelay();
        turnDelayTime /= 10;
        turnDelayTime = Math.max(turnDelayTime,  250);
        turnDelayTime = Math.min(turnDelayTime, 1000);

        if (turnDelay)
            turnDelayClock.setDelay(turnDelayTime);
        else
            turnDelayClock.setDelay(-1);
    }

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

            turnDelayClock.reset();
            turnDelayClock.resume();

            return CONTINUE_LOOP;
        }

        /* Don't force moves from interactive players. */
        if (player.isHuman())
            return BREAK_LOOP;

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

            turnDelayClock.reset();
            turnDelayClock.resume();

            return CONTINUE_LOOP;
        }
        else
            return turnClock[side].getTimeRemain();
    }

    protected long stepReady()
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* Skip this entire next section if isClick == true */
        if (!isClick)
        {
            /* Do not continue if moveOnClick is on */
            if (getMoveOnClick(side))
                return BREAK_LOOP;

            /* Otherwise, wait for turn delay */
            if (turnDelayClock.getState() == CountdownClock.RUNNING)
                return turnDelayClock.getTimeRemain();
        }

        /* Set model state to WAITING */
        model.startWaiting();

        /* Start the turn clock that enforces term limits */
        turnClock[side].reset();
        turnClock[side].resume();

        /* Begin calculations with a TurnAgent and register callback */
        turnAgent.startCalculate(player, model.getBoardState());

        return CONTINUE_LOOP;
    }

    /**
     * Sets whether turn delay is enabled.
     * @param turnDelay     whether turn delay is enabled
     */
    public void setTurnDelay(boolean turnDelay)
    {
        this.turnDelay = turnDelay;
        updateTurnDelay();
    }

    public void setTurnLimit(int side, long limit)
    { 
        super.setTurnLimit(side, limit);
        updateTurnDelay();
    }

    /**
     * Sets whether a click is required before each move by the specified side.
     * @param side          an integer representing the side
     * @param b             whether a move is required
     */
    public void setMoveOnClick(int side, boolean b) { moveOnClick[side] = b; }

    /**
     * Gets whether a click is required before each move by the specified side.
     * @param side          an integer representing the side
     * @return              whether a move is required
     */
    public boolean getMoveOnClick(int side) { return moveOnClick[side]; }
}
