package cs540.checkers;

import java.util.*;

/**
 * This interfaces defines how the alpha-beta grader will interact with each 
 * student's alpha-beta player. This interface provides 
 * {@link #getPruneCount getPruneCount} so that each student's alpha-beta 
 * player can be compared against a reference implementation. 
 */
public interface GradedCheckersPlayer
{
    /** 
     * Returns the number of pruned subtrees for the most recent deepening 
     * iteration. This method must not be called while this player is 
     * calculating its move.
     * @return      the number of pruned subtrees in the most recent deepening 
     *              iteration
     */
    public int getPruneCount();
}
