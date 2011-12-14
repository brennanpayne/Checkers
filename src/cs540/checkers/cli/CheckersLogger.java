package cs540.checkers.cli;
import cs540.checkers.*;

import java.io.*;

/*
 * @author Justin Tritz
 * @author David He
 */
public class CheckersLogger implements GameListener
{
    /**
     * The print stream to log to.
     * @see #setLog setLog
     */
    protected PrintStream log;

    protected CheckersModel cm;

    public CheckersLogger(CheckersModel cm, OutputStream log)
    {
        this.log = new PrintStream(log);
        this.cm = cm;
        cm.addGameListener(this);
    }

    public void gameChanged(GameEvent e)
    {
        log.printf("%s %s\n", e.getAction(), e.getDetails());
    }
}
