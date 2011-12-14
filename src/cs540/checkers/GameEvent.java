package cs540.checkers;

import java.util.EventObject;

/**
 * GameEvent is used to notify interested parties that a checkers game has progressed.
 * @author David He
 */
public class GameEvent extends EventObject
{
    protected String action;
    protected String details;

    /**
     * Constructs a GameEvent object with the selected parameters.
     * @param action        a string specifying the action
     */
    public GameEvent(Object source, String action, String details)
    {
        super(source);
        this.action = action;
        this.details = details;
    }

    /**
     * Gets the action.
     * @return the action 
     */
    public String getAction()
    {
        return action;
    }

    /**
     * Gets the details.
     * @return the details 
     */
    public String getDetails()
    {
        return details;
    }
}
