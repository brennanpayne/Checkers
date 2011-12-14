package cs540.checkers.demo;
import cs540.checkers.*;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;

/**
 * This checkers player nearsightedly selects moves based on the score of the 
 * immediate board state. Obviously, this player will not perform very well. 
 * <p>
 * This player demonstrates the use of the {@link Utils Utils} class.
 * @author Justin Tritz
 * @author David He
 */
public class DemoPlayer extends CheckersPlayer
{
    /**
     * The static board evaluator this player uses.
     */
    protected Evaluator sbe;
    
    /**
     * Constructs a DemoPlayer on the specified side with the given name.
     * @param name      the name of this player
     * @param side      the side this player is on
     */
    public DemoPlayer(String name, int side)
    { 
        super(name, side);
        /* Use SimpleEvaluator, which depends only on the remaining 
         * material on the board. */
        sbe = new SimpleEvaluator();
    }

    /**
     * Selects a move depending on the score of the board state resulting from 
     * that move. The board state is scored using 
     * {@link SimpleEvaluator SimpleEvaluator}.
     * @param bs        the board state from which to select a move
     */
    public void calculateMove(int[] bs)
    {
        /* Get all the possible moves for this player on the provided board state */
        List<Move> possibleMoves = Utils.getAllPossibleMoves(bs, side);

        /* If this player has no moves, return out */
        if (possibleMoves.size() == 0)
            return;

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        /* Find best board state among those reachable from one move */
        for (Move move : possibleMoves)
        {
            /* Execute the move so we can score the board state resulting from 
             * the move */
            Stack<Integer> rv = Utils.execute(bs, move);

            /* Evaluate this board state */
            int score = sbe.eval(bs);
            /* Negate the score if not RED */
            if (side == BLK)
                score = -score;

            /* Update bestMove if score > bestScore */
            if (score > bestScore)
            {
                bestScore = score;
                bestMove = move;
            }

            /* Revert the move so we can score additional board states. */
            Utils.revert(bs, rv);
        }

        /* Set the best move as the chosen move */
        setMove(bestMove);
    }
}
