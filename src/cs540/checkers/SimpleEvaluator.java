package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;

/**
 * This simplistic static board evaluator assigns points for material.  Each 
 * pawn remaining on the board contributes one point, and each remaining king 
 * remaining on the board contributes two points. 
 */
public class SimpleEvaluator implements Evaluator
{
    public int eval(int[] bs)
    {
        int[] pawns = new int[2],
            kings = new int[2] ;

        for (int i = 0; i < H * W; i++)
        {
            int v = bs[i];
            switch(v)
            {
                case RED_PAWN:
                case BLK_PAWN:
                    pawns[v % 4] += 1;
                    break;
                case RED_KING:
                case BLK_KING:
                    kings[v % 4] += 1;
                    break;
            }
        }

        return 1 * (pawns[RED] - pawns[BLK]) + 
               2 * (kings[RED] - kings[BLK]);
    }
}
