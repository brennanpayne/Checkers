package cs540.checkers;

/**
 * This class implements a game clock which conforms to the <code>GameClock</code>
 * interface.
 * @see GameClock GameClock
 * @author David He
 */
public class DefaultGameClock implements GameClock
{
    protected long[] gameLimit;
    protected long[] gameRemain;
    protected int side;
    protected int state;

    protected long resumeTime;
    protected long[] gameRemainPrev;

    /**
     * Creates a simple game clock with default parameters. Each side has
     * five minutes for the game, and RED is the first to move. The initial
     * state will be PAUSED.
     */
    public DefaultGameClock()
    {
        this(new long[] {300 * 1000, 300 * 1000} , Utils.INITIAL_SIDE);
    }

    /**
     * Creates a simple game clock with the specified parameters. The initial
     * state will be PAUSED.
     * @param gameLimit     how much time each player has for the game, in 
     *                      milliseconds
     * @param side          the side to move first
     */
    public DefaultGameClock(long[] gameLimit, int side)
    {
        this.gameLimit = gameLimit.clone();
        this.gameRemain = gameLimit.clone();
        this.side= side;
        state = PAUSED;
        gameRemainPrev = gameRemain.clone();
    }

    public long getGameTime(int _side)
    {
        touch();
        return gameLimit[_side] - gameRemain[_side];
    }

    public long getGameTimeRemain(int _side)
    {
        touch();
        return gameRemain[_side];
    }

    public long getTurnTime(int _side)
    {
        touch();
        return gameRemainPrev[_side] - gameRemain[_side];
    }

    public void press()
    {
        pause();
        if (state == PAUSED)
        {
            side = Utils.otherSide(side);
            gameRemainPrev[side] = gameRemain[side];
        }
        resume();
    }

    public void pause()
    {
        touch();
        if (state == RUNNING)
            state = PAUSED;
    }

    public void resume()
    {
        touch();
        if (state == PAUSED)
            state = RUNNING;
    }

    /**
     * Maintains the state of the clock by computing the difference in current
     * time since the last call to this method.
     */
    protected void touch()
    {
        long currentTime = System.currentTimeMillis();
        if (state == RUNNING)
            gameRemain[side] -= (currentTime - resumeTime);
        resumeTime = currentTime;

        if (gameRemain[side] <= 0)
            state = FINISHED;
    }

    public int getState()
    { 
        touch();
        return state;
    }

    public int getSide() { return side; }
}
