package cs540.checkers.demo;
import cs540.checkers.*;

import java.util.*;

/**
 * This checkers player selects moves uniformly at random from its legal moves. 
 * @author Justin Tritz
 * @author David He
 */
public class RandomPlayer extends CheckersPlayer
{
    /**
     * Constructs a RandomPlayer on the specified side with the given name.
     * @param name      the name of this player
     * @param side      the side this player is on
     */
    public RandomPlayer(String name, int side) { super(name, side); }

    /**
     * Selects a move uniformly at random from all legal moves on the given 
     * board state.
     * @param bs        the board state from which to randomly select a move
     */
    public void calculateMove(int[] bs)
    {
        /* Get all the possible moves for this player on the provided board state */
        List<Move> possibleMoves = Utils.getAllPossibleMoves(bs, side);

        /* If this player has no moves, return out */
        if (possibleMoves.size() == 0)
            return;

        /* Choose a random number from 0 to the number of possible moves. */
        int rand = (int)(Math.random() * possibleMoves.size());

        /* Set the chosen move to be the move with the random index. */
        setMove(possibleMoves.get(rand));
    }
}
