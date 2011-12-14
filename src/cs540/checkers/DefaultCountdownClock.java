package cs540.checkers;

/**
 * This class implements a countdown clock which conforms to the 
 * <code>CountdownClock</code> interface. 
 * @see CountdownClock CountdownClock
 * @author David He
 */
public class DefaultCountdownClock implements CountdownClock
{
    protected long delay;
    protected int state;
    protected long remain;
    protected long resumeTime;
    
    /**
     * Creates a countdown clock which counts down from <code>delay</code> 
     * milliseconds.
     * @param delay     the time, in milliseconds, that this clock will count 
     *                  down from
     */
    public DefaultCountdownClock(long delay)
    {
        this.delay = delay;
        state = PAUSED;
        reset();
    }

    public void reset()
    {
        touch();
        state = PAUSED;
        remain = delay;
    }

    public void resume()
    {
        touch();
        if (delay >= 0 && state == PAUSED)
            state = RUNNING;
    }

    public void pause()
    {
        touch();
        if (state == RUNNING)
            state = PAUSED;
    }

    /**
     * Maintains the state of the clock by computing the difference in current
     * time since the last call to this method.
     */
    protected void touch()
    {
        long currentTime = System.currentTimeMillis();
        if (state == RUNNING)
            remain -= (currentTime - resumeTime);
        resumeTime = currentTime;

        if (delay >= 0 && remain <= 0)
            state = FINISHED;
    }

    public long getTimeRemain()
    {
        touch();
        if (delay < 0)
            return -1;
        if (remain < 0)
            return 0;
        return remain;
    }

    public int getState()
    {
        touch();
        return state;
    }

    public long getDelay() { return delay; }
    public void setDelay(long delay) { this.delay = delay; }
}
