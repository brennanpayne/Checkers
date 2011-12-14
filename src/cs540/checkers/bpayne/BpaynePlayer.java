
/* Don't forget to change this line to cs540.checkers.<username> */
package cs540.checkers.bpayne;

import cs540.checkers.*;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;

/*
 * This is a skeleton for an alpha beta checkers player. Please copy this file
 * into your own directory, i.e. src/<username>/, and change the package 
 * declaration at the top to read
 *     package cs540.checkers.<username>;
 * , where <username> is your cs department login.
 */
/** This is a skeleton for an alpha beta checkers player. */
public class BpaynePlayer extends CheckersPlayer implements GradedCheckersPlayer
{
	/** The number of pruned subtrees for the most recent deepening iteration. */
	protected int pruneCount;
	protected Evaluator sbe;

	public BpaynePlayer(String name, int side)
	{ 
		super(name, side);
		// Use BetterEvaluator to score terminal nodes
		sbe = new BetterEvaluator();
	}

	public void calculateMove(int[] bs)
	{

		Random generator = new Random();

		BoardState boardState = new BoardState(bs, side);
		/* Get all the possible moves for this player on the provided board state */
		List<Move> possibleMoves = boardState.getAllPossibleMoves();

		/* If this player has no moves, return out */
		if (possibleMoves.size() == 0)
			return;

		Move bestMove = null;
		int bestScore = Integer.MIN_VALUE;
		
		for(int curDepth = 1; curDepth < this.depthLimit; curDepth+=2){
			/* Find best board state among those reachable from one move */
			bestScore = Integer.MIN_VALUE;
			bestMove = null;
			pruneCount = 0;
			
			for (Move move : possibleMoves)
			{
				/* Execute the move so we can score the board state resulting from 
				 * the move */
				
				boardState.execute(move);

				int score = minValue(Integer.MIN_VALUE, Integer.MAX_VALUE, curDepth - 1, boardState);

				/* Update bestMove if score > bestScore */
				if (score > bestScore)
				{
					bestMove = move;
					bestScore = score;

				}
				else if(score == bestScore && generator.nextDouble() > .7){
					bestMove = move;
					bestScore = score;
				}

				/* Revert the move so we can score additional board states. */
				boardState.revert();
			}

			setMove(bestMove);
			
			if(Utils.verbose == true)
				System.out.println("Best Move: " + bestMove + "\tScore: " + bestScore);
			/* Set the best move as the chosen move */
		}
	}



	private int maxValue(int alpha, int beta, int depth, BoardState bs){

		int maxSide = side;

		List<Move> possibleMoves = bs.getAllPossibleMoves(); 

		if (possibleMoves.size() == 0 || depth == 0){
			int score = sbe.eval(bs.D);
			if(maxSide == BLK)
				return -score;
			else
				return score;
		}

		for (Move move : possibleMoves)
		{
			/* Execute the move so we can score the board state resulting from 
			 * the move */

			bs.execute(move);
			alpha = Math.max(alpha, (minValue(alpha, beta, (depth-1), bs)));
			bs.revert();

			if(alpha >= beta){
				pruneCount++;
				return beta;
			}
		}

		/* Revert the move so we can score additional board states. */

		return alpha;
	}


	private int minValue(int alpha, int beta, int depth, BoardState bs){

		List<Move> possibleMoves = bs.getAllPossibleMoves(); 

		if (possibleMoves.size() == 0 || depth == 0){
			int score = sbe.eval(bs.D);
			if(side == BLK)
				return -score;
			else
				return score;
		}

		for (Move move : possibleMoves)
		{
			/* Execute the move so we can score the board state resulting from 
			 * the move */

			bs.execute(move);
			beta = Math.min(beta, (maxValue(alpha, beta, (depth-1), bs)));
			bs.revert();
			/* Revert the move so we can score additional board states. */

			if(alpha >= beta){
				pruneCount++;
				return alpha;
			}
		}
		return beta;
	}

	public int getPruneCount()
	{
		return pruneCount;
	}
}
