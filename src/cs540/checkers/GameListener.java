package cs540.checkers;

import java.util.EventListener;

/**
 * Defines an object which listens for GameEvents. 
 * @author David He
 */
public interface GameListener extends EventListener
{
    /**
     * Invoked when the target of the listener has changed.
     * @param e         a GameEvent object
     */
    void gameChanged(GameEvent e);
}

