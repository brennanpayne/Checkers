package cs540.checkers.ui;
import cs540.checkers.*;

import java.util.*;

/**
 * This class contains static methods used for computing partial moves.
 * @author David He
 */
public class PMoveUtils
{
    public static int INVALID = 0;

    /**
     * Returns whether <code>pmove</code> is a partial move for the given side.
     * @param bs        the board state on which to make the partial move
     * @param side      the side to make the partial move
     * @param pmove     the sequence of locations to determine as a partial move
     * @return          true if <code>pmove</code> is a partial move; false otherwise
     */
    public static boolean isValidPartialMove(int[] bs, int side, MutableMove pmove)
    {
        if (pmove.size() > 0 && bs[pmove.get(0)] % 4 != side)
            return false;

        List<int[]> ops = Utils.convertMoveToPairwise(pmove);

        if (Utils.isWalk(pmove))
        {
            /* The partial move is a complete walk move */
            if (Utils.isForcedJump(bs, side))
                return false;

            int[] op = ops.get(0);
            return Utils.canWalk(bs, op[0], op[1]);
        }
        else
        {
            /* The partial move is a partial jump move, a move with one 
             * location, or a move with zero locations. */
            int[] pbs = bs.clone();

            for (int[] op : ops)
            {
                if (!Utils.canJump(pbs, op[0], op[1]))
                    return false;

                Utils.jump(pbs, op[0], op[1]);
            }
            return true;
        }
    }

    /**
     * Executes the specified partial move on the specified board state.
     * For performance reasons, this method does not check whether 
     * <code>pmove</code> is a valid partial move. It is the caller's 
     * responsibility to ensure that <code>pmove</code> is a valid partial move.
     * @param bs        the board state on which to perform the partial move
     * @param pmove     the partial move to perform
     * @see #isValidPartialMove isValidPartialMove
     */
    public static void executePartialMove(int[] bs, MutableMove pmove)
    {
        List<int[]> ops = Utils.convertMoveToPairwise(pmove);

        if (Utils.isWalk(pmove))
        {
            int[] op = ops.get(0);
            Utils.walk(bs, op[0], op[1]);
        }
        else
        {
            for (int[] op : ops)
                Utils.jump(bs, op[0], op[1]);
        }
    }
}
