package cs540.checkers.bpayne;
import static cs540.checkers.CheckersConsts.*;

import cs540.checkers.Evaluator;

/**
 * This simplistic static board evaluator assigns points for material.  Each 
 * pawn remaining on the board contributes one point, and each remaining king 
 * remaining on the board contributes two points. 
 */
public class BetterEvaluator implements Evaluator
{
	public int eval(int[] bs)
	{	
		int kingValue = 0;
		int redPawn = 0;
		int blackPawn = 0;
		int redKing = 0;
		int blackKing = 0;
		int score = Integer.MIN_VALUE;
		
		int totalPiecesLeft = 0;
		//count number of pieces
		for (int i = 0; i < H * W; i++)
		{
			int v = bs[i];
			switch(v)
			{
			case RED_PAWN:
				redPawn++;
				break;
			case BLK_PAWN:
				blackPawn++;
				break;
			case RED_KING:
				redKing++;
				break;
			case BLK_KING:
				blackKing++;
				break;
			}
			totalPiecesLeft++;
		}
		
		int numBlack = blackPawn + blackKing;
		int numRed = redPawn + redKing;
		
		if(totalPiecesLeft <= 4)
			return ((redPawn + 2*redKing) - (blackPawn + 2*redKing));
		
		//Pieces become more valuable as the game goes on
		if(totalPiecesLeft > 12)
			score = ((numRed) - (numBlack)) * 100;
		else
			score = ((numRed) - (numBlack)) 
			* (160 - (totalPiecesLeft) * 5);
		
		int pawnValue = -1;
		
		if(totalPiecesLeft >= 20){
			pawnValue = 10;
			kingValue = 12;
		}else if(totalPiecesLeft >= 14 ){
			pawnValue = 12;
			kingValue = 14;
		}else if(totalPiecesLeft >= 10){
			pawnValue = 14;
			kingValue = 16;
		}else{
			pawnValue = 16;
			kingValue = 20;
		}
		
		int kv = kingValue;
		
		//Value board for kings location
		final int[] kingValueBoard = 
			{0, kv, 0, kv, 0, kv, 0, kv-3,
			 kv, 0, kv+1, 0, kv+1, 0, kv, 0,
			 0, kv+2, 0 , kv+3, 0, kv+3, 0, kv+1,
			 kv+1, 0 , kv+5, 0, kv+5, 0, kv+1, 0,
			 0, kv+3, 0, kv+5, 0 ,kv+5, 0, kv+1,
			 kv+1, 0, kv+3, 0 ,kv+3, 0 ,kv+2, 0,
			 0, kv, 0, kv+1, 0, kv+1, 0, kv,
			 kv-3, 0, kv, 0, kv, 0, kv,0
			 };
		
		int pv = pawnValue;
		
		final int[] redPawnValueBoard = 
		{0, pv+8, 0, pv+8, 0, pv+8, 0, pv+8,
		 pv+1, 0, pv+4, 0, pv+4, 0, pv+4, 0,
		 0, pv+2, 0 , pv+3, 0, pv+3, 0, pv+1,
		 pv+1, 0 , pv+4, 0, pv+4, 0, pv+1, 0,
		 0, pv+3, 0, pv+4, 0 ,pv+4, 0, pv+1,
		 pv+1, 0, pv+3, 0 ,pv+3, 0 ,pv+3, 0,
		 0, pv, 0, pv+1, 0, pv+1, 0, pv,
		 pv+4, 0, pv+4, 0, pv+4, 0, pv+4,0
		 };
		
		final int[] blackPawnValueBoard = 
		{0, pv+4, 0, pv+4, 0, pv+4, 0, pv+4,
		 pv, 0, pv+1, 0, pv+1, 0, pv, 0,
		 0, pv+2, 0 , pv+3, 0, pv+3, 0, pv+1,
		 pv+1, 0 , pv+4, 0, pv+4, 0, pv+1, 0,
		 0, pv+3, 0, pv+4, 0 ,pv+4, 0, pv+1,
		 pv+1, 0, pv+3, 0 ,pv+3, 0 ,pv+2, 0,
		 0, pv+4, 0, pv+4, 0, pv+4, 0, pv+1,
		 pv+8, 0, pv+8, 0, pv+8, 0, pv+8,0
		 };

		//Add kings value to the score
		int kingScore = 0;
		int redEdge = 0 ;
		int blackEdge = 0;
		int pawnScore = 0;
		
		for(int i = 0; i < (H * W); i++){
			int temp = bs[i];
			
			switch(temp){
			case RED_PAWN:
				if(isEdge(i))
					redEdge++;
				pawnScore += redPawnValueBoard[i];
				break;
			case BLK_PAWN:
				if(isEdge(i))
					blackEdge++;
				pawnScore -= blackPawnValueBoard[i];
				break;
			case RED_KING:
				if(isEdge(i))
					redEdge++;
				kingScore += kingValueBoard[i];
				break;
			case BLK_KING:
				if(isEdge(i))
					blackEdge++;
				kingScore -= kingValueBoard[i];
				break;
			default:
				break;
			}
		}
		
		
		
		score += kingScore;
		score += pawnScore;
		
		//Keeping pawns off edges
		score -= 2 * (redEdge - blackEdge);	

		return score;
	}
	
	
	
	//Edges = 8, 24, 40, 56, 7, 23, 39, 55
	//Corners = 7, 56
	public boolean isEdge(int i){
		
		switch(i){
			case 8:
			case 24:
			case 40:
			case 55:
			case 7:
			case 23:
			case 39:
			case 56:
				return true;
			default:
				
		}
		return false;
	}
	
}
