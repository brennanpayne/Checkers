package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;
import java.io.*;

/**
 * This class provides a set of static utility methods for the game of checkers.
 * <ul>
 * <li>{@link #execute execute} makes a move on a board state;
 * <li>{@link #revert revert} undoes a move made by execute;
 * <li>{@link #isValidMove isValidMove} tests whether a move is legal;
 * <li>{@link #getAllPossibleMoves getAllPossibleMoves} finds all legal moves
 * on a given board state;
 * <li>{@link #reprBoardState reprBoardState} and 
 * {@link #parseBoardState parseBoardState} converts a boardstate to/from a 
 * <code>String</code> representation;
 * <li>{@link #loadBoardState loadBoardState} loads a board state from a file;
 * <li>{@link #equalsBoardState equalsBoardState} tests whether two board states are equal;
 * <li>{@link #reprSide reprSide}, {@link #reprCheckersPiece reprCheckersPiece}, 
 * {@link #reprLocation reprLocation}, and {@link #reprMove reprMove} 
 * represent sides, checkers pieces, locations, and moves, respectively, as strings;
 * <li>{@link #isWalk isWalk} tests whether a move is a walk.
 * </ul>
 *
 * In addition,
 * <ul>
 * <li>{@link #walk walk}, {@link #jump jump}, and {@link #crownKings crownKings} 
 * are helper methods to {@link #execute execute};
 * <li>{@link #isForcedJump isForcedJump}, {@link #hasJump hasJump}, 
 * {@link #canJump canJump}, and {@link #canWalk canWalk} are helper methods to 
 * {@link #isValidMove isValidMove};
 * <li>{@link #findWalkMoves findWalkMoves} and {@link #findJumpMoves findJumpMoves} 
 * are helper methods to {@link #getAllPossibleMoves getAllPossibleMoves}.
 * </ul>
 *
 * <p>
 * In all of preceding methods and throughout the rest of the framework, 
 * a common set of data structures are used to represent the characteristics 
 * of a checkers game.
 * <p>
 * The <b>sides</b> <i>red</i> and <i>black</i> are enumerated as 
 * <code>int</code>s <code>0</code> and <code>1</code>, respectively. In 
 * addition, some classes use a side called <code>NEITHER</code> that 
 * represents an unknown, intermediate, or indeterminate side. This enumeration 
 * is defined by the fields {@link CheckersConsts#RED RED} and 
 * {@link CheckersConsts#BLK BLK}, and 
 * {@link CheckersConsts#NEITHER NEITHER} in 
 * {@link CheckersConsts CheckersConsts}.
 * <p>
 * Checkers <b>pieces</b> are also enumerated as <code>int</code>s, by the 
 * fields {@link CheckersConsts#RED_PAWN RED_PAWN}, 
 * {@link CheckersConsts#RED_PAWN RED_KING}, 
 * {@link CheckersConsts#RED_PAWN BLK_PAWN}, and 
 * {@link CheckersConsts#RED_PAWN BLK_KING} in 
 * {@link CheckersConsts CheckersConsts}. This enumeration 
 * additionally provides {@link CheckersConsts#BLANK BLANK}, which is not 
 * a checkers piece, but represents the lack of a checkers piece.
 * <p>
 * The <b>locations</b> of a checkers board index the squares of the chcekcers 
 * board. They are:
 * <blockquote><pre>
 *       BLK
 *
 *   01  03  05  07
 * 08  10  12  14   
 *   17  19  21  23
 * 24  26  28  30   
 *   33  35  37  39
 * 40  42  44  46   
 *   49  51  53  55
 * 56  58  60  62   
 *
 *       RED
 * </pre></blockquote>
 * <p>
 * A <b>move</b> is represented by the sequence of locations through which a checkers piece travels.
 * An <i>immutable move</i>, which can not be modified once constructed, is represented by {@link Move Move}. {@link MutableMove MutableMove} represents a modifiable move. Both <code>Move</code> and <code>MutableMove</code> are subclasses of {@link List java.util.List<Integer>}.
 * <p>
 * A <i>walk move</i> is a move where the source and destination locations are
 * diagonally adjacent. A <i>jump</i> is the act of moving diagonally over a 
 * square while removing its contents. A <i>jump move</i> consists of one or more 
 * jumps. Jump moves are distinguished from jumps.
 * <p>
 * A <b>board state</b> is represented by an <code>int[]</code> array whose 
 * indices are the  <code>64</code> board locations. The element at index 
 * <code>i</code> is the <code>int</code> representing the checkers piece at 
 * location <code>i</code>; the element is <code>BLANK</code> if location 
 * <code>i</code> is blank.
 * <p>
 * A <b><i>partial</i> board state</b> is a term used by several helper
 * methods to refer to a board state that is in the middle of a move execution. 
 * For example, invoking <code>execute</code> on a move consisting of two jumps
 * results in a partial board state after the first jump.
 * <p>
 * A <b>revert stack</b> is a LIFO list which contains the contents of a board 
 * state before the board state is modified, so that the modifications can be
 * undone at a later time. Revert stacks are returned by {@link #execute execute} 
 * and its helper methods {@link #walk walk}, {@link #jump jump}, and 
 * {@link #crownKings crownKings}. These revert stacks can be passed to 
 * {@link #revert revert}, which will restore the board state to its original
 * values.
 * <p>
 * The revert stack consists of <code>int</code>s stored in (location, value) 
 * pairs. The item on the bottom is a location on the checkers board, and the 
 * item on the top is the piece at that location before the modification. 
 * <code>revert</code> traverses the stack, restoring the original piece at 
 * each location. Other classes are free to manipulate the revert stack as long 
 * as its structure is maintained. 
 * <p>
 * The following example demonstrates the use of the structures and methods 
 * described above. The method <code>doSomething</code>, when given board state 
 * <code>bs</code> and the active side <code>side</code>, gets the list of all
 * possible moves, selects the first move on that list, makes that move, undoes
 * that move, selects a random move from the list, then performs that random 
 * move. 
 * <blockquote><pre>
 * 
 * public void doSomething(int[] bs, int side)
 * {
 *     // Get a list of all valid moves for the active side on the board state
 *     List<Move> moveList = Utils.getAllPossibleMoves(bs, side);
 *
 *     // Select the first move on the list of valid moves
 *     // (assumes moveList is not empty)
 *     Move move = moveList.get(0);
 *
 *     // Execute the selected move
 *     Stack<Integer> revertStack = Utils.execute(bs, move);
 *
 *     // Undo the selected move
 *     Utils.revert(bs, revertStack);
 *     
 *     // Select a random move from the list of valid moves, then execute it. 
 *     int rand = (int)(Math.random() * moveList.size());
 *     Utils.execute(bs, moveList.get(rand));
 * }
 * </pre></blockquote>
 * <p>
 * You may elect to use {@link BoardState BoardState} as an object-orientated 
 * approach to representing board states. The methods of <code>BoardState</code> 
 * provides are roughly parallel to the static methods in this class. 
 *
 * @see Move Move
 * @see MutableMove MutableMove
 * @see CheckersConsts CheckersConsts
 * @see BoardState BoardState
 * @author Justin Tritz
 * @author David He
 */

public class Utils
{
    /** 
     * The static, globally scoped variable that all classes should reference
     * to determine whether to verbosely print output.
     */
    public volatile static boolean verbose = false;

    /**
     * Returns the side of <code>side</code>'s opponent.
     * This method returns <code>NEITHER</code> if <code>side</code> is <code>NEITHER</code>
     * <p>
     * If <code>side</code> is not <code>NEITHER</code>, then this method returns the 
     * equivalent of <code>(side + 1) / 2</code>.
     * @param side      the side to find the opponent of
     * @return          the side of <code>side</code>'s opponent
     */
    public static int otherSide(int side)
    {
        if (side == RED) return BLK;
        if (side == BLK) return RED;
        return NEITHER;
    }

    /**
     * The location offsets to diagonally adjacent squares. The four diagonals
     * to a location <code>a</code> are <code>a - 9, a-7, a + 7, a+9</code>.
     */
    public static final int[] DIAG = new int[] {-9, -7, 7, 9};
 
    /**
     * Scores the specified board state using a evaluation function that
     * assigns points for material. Each remaining pawn contributes one point, 
     * and each remaining king contributes two points. 
     * <p>
     * The score will always be evaluated relative to <code>RED</code>. 
     * Thus, a positive score indicates a <code>RED</code> material advantage, and a 
     * negative score indicates a <code>BLK</code> material advantage.
     *
     * @param bs        the board state for which to generate a score
     * @return          the score for the provided board state
     */
    public static int scoreBoardState(int[] bs)
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

    /**
     * Returns whether the specified move is a walk move. This method checks 
     * that <code>move</code> consists of exactly two locations, and 
     * that the locations are diagonally adjacent. This method does not attempt
     * to verify whether <code>move</code> is a legal walk move on a specific 
     * board state.
     * @param move      the move's list of locations, for determining whether
     *                  the move is a walk move
     * @return          true if <code>move</code> is a walk move
     */
    public static boolean isWalk(List<Integer> move)
    {
        /* Walk moves have exactly two locations */
        if (move.size() != 2)
            return false;

        /* The source and destination location must be exactly one square
         * apart diagonally */
        int a = move.get(0), b = move.get(1);
        return 
            Math.abs(a / W - b / W) == 1 &&
            Math.abs(a % W - b % W) == 1 ;
    }

    /**
     * Returns whether <code>move</code> is legal for the given side. 
     * @param bs        the board state on which to make the move
     * @param side      the side to make the move
     * @param move      the move to test for legality
     * @return          true if <code>move</code> is legal; false otherwise
     */
    public static boolean isValidMove(int[] bs, int side, Move move)
    {
        /* The move cannot be null */
        if (move == null)
            return false;

        List<int[]> ops = Utils.convertMoveToPairwise(move);

        /* The move must have two or more locations */
        if (ops.size() == 0)
            return false;

        /* The checkers piece at source location must belong to side */
        if (bs[move.get(0)] % 4 != side)
            return false;

        if (Utils.isWalk(move))
        {
            /* The move is a walk move */

            /* Cannot perform walk move if jump moves exist */
            if (Utils.isForcedJump(bs, side))
                return false;

            /* Call Utils.canWalk helper */
            int[] op = ops.get(0);
            return Utils.canWalk(bs, op[0], op[1]);
        }
        else
        {
            /* The move is a jump move */

            /* Clone board state to partial board state */
            int[] pbs = bs.clone();

            /* Attempt to perform each jump in the sequence of jumps. If any jumps
             * fail, return false. */
            for (int[] op : ops)
            {
                if ( Utils.canJump(pbs, op[0], op[1]) )
                    Utils.jump(pbs, op[0], op[1]);
                else
                    return false;
            }

            /* Must capture all available pieces in jump sequence */
            if ( Utils.hasJump(pbs, side, move.get(move.size() - 1)) )
                return false;

            return true;
        }
    }

    /**
     * Returns whether the specified side loses because it cannot
     * make any legal moves.
     * @param bs        the board state to test game over
     * @param side      the side to test game over
     * @return          true if <code>side</code> has no legal moves;
     *                  false otherwise
     */
    public static boolean isLoser(int[] bs, int side)
    {
        /* Player loses if no legal moves may be performed */
        return Utils.getAllPossibleMoves(bs, side).size() == 0;
    }

    /**
     * Returns whether the specified side must make a jump move.
     * This method is equivalent to whether the specific side has at least
     * one jump move. 
     * @param bs        the board state on which a move will be made
     * @param side      the side to make a move
     * @return          true if the specified side must make
     *                  a jump move; false otherwise
     */
    public static boolean isForcedJump(int[] bs, int side)
    {
        for (int a = 0; a < H * W; a++)
            if (hasJump(bs, side, a))
                return true;

        return false;
    }

    /**
     * Returns a list of all the moves that the specified side may perform on 
     * the specified board state.
     * <p>
     * The ordering of moves in this list is is not defined and may change 
     * between successive calls. 
     * @param bs        the board state on which the moves will be made
     * @param side      the side to make the moves
     * @return          a list of all the moves that <code>side</code> may 
     *                  perform on <code>bs</code>
     */
    public static List<Move> getAllPossibleMoves(int[] bs, int side)
    {
        if ( Utils.isForcedJump(bs, side) )
            return Utils.findJumpMoves(bs, side);
        else
            return Utils.findWalkMoves(bs, side);
    }

    /**
     * Returns a list of all walk moves that the specified side may perform on 
     * the specified board state. This method assumes that <code>side</code>
     * is not otherwise forced to make a jump move.  The moves in this list 
     * are legal if and only if the specified side is not required to make a jump 
     * move.
     * <p>
     * The ordering of moves in this list is not defined and may change 
     * between successive calls. 
     * @param bs        the board state on which the moves are made
     * @param side      the side to make the moves
     * @return          an inclusive list of walk moves that <code>side</code> 
     *                  may perform on <code>bs</code>
     */
    public static List<Move> findWalkMoves(int[] bs, int side)
    {
        List<Move> moveList = new ArrayList<Move>();

        for (int a = 0; a < H * W; a++)
        {
            if (bs[a] % 4 != side)
                continue;

            for (int d : Utils.DIAG)
            {
                int b = a + d;
                if ( !Utils.canWalk(bs, a, b) )
                    continue;

                moveList.add(new Move( Arrays.asList(a, b) ));
            }
        }

        return moveList;
    }

    /**
     * Returns a list of all jump moves that the specified side may perform on 
     * the specified board state.
     * <p>
     * The ordering of the moves in this list is not defined and may change 
     * between successive calls. 
     * @param bs        the board state on which the moves are made
     * @param side      the side to make the moves
     * @return          an inclusive list of jump moves that <code>side</code> 
     *                  may perform on <code>bs</code>
     */
    public static List<Move> findJumpMoves(int[] bs, int side)
    {
        List<Move> moveList = new ArrayList<Move>();
        for (int a = 0; a < H * W; a++)
        {
            if (bs[a] % 4 != side)
                continue;

            MutableMove pmove = new MutableMove();
            pmove.add(a);

            Utils.findJumpMovesHelper(bs, pmove, moveList);
        }
        return moveList;
    }
    
    /**
     * Adds to <code>moveList</code> all jump moves that begin with the 
     * partial move <code>pmove</code>. 
     * This helper method recursively calls itself while updating 
     * <code>pbs</code> as the partial board state resulting from performing
     * <code>pmove</code> on the original board state. 
     * @param pbs       the partial board state resulting from performing
     *                  <code>pmove</code> on the original board state
     * @param pmove     the partial move to search from
     * @param moveList  the list of moves to add newly found jump moves to
     * @see #findJumpMoves findJumpMoves
     */
    public static void findJumpMovesHelper(int[] pbs, 
            MutableMove pmove, List<Move> moveList)
    {
        int a = pmove.get(pmove.size() - 1);
        boolean canJumpAgain = false;

        for (int d : Utils.DIAG)
        {
            int b = a + 2 * d;
            if ( !Utils.canJump(pbs, a, b) )
                continue;

            canJumpAgain = true;

            pmove.add(b);

            Stack<Integer> rv = Utils.jump(pbs, a, b);
            Utils.findJumpMovesHelper(pbs, pmove, moveList);
            Utils.revert(pbs, rv);

            pmove.remove(pmove.size() - 1);
        }
        
        if (!canJumpAgain && pmove.size() >= 2)
            moveList.add( new Move(pmove) );
    }

    /**
     * Returns whether a player may walk a checkers piece from <code>src</code>
     * to <code>dst</code>.
     * This method assumes that the player is not restricted from executing a
     * walk move by the existence of a jump move.
     * This method is undefined if <code>src</code> is empty.
     *
     * @param pbs   the partial board state on which the walk will occur
     * @param src   the source location of the walk
     * @param dst   the destination location of the walk
     * @return      undefined if <code>src</code> is empty;
     *              false if the move is not a walk move;
     *              false if the move is not legal;
     *              true otherwise
     */
    public static boolean canWalk(int[] pbs, int src, int dst)
    {
        int a = src, b = dst;

        /* Return false if src and dst are out of bounds */
        if (a < 0 || a > H * W ||
            b < 0 || b > H * W  )
            return false;

        /* src and dst must be one square apart on a diagonal */
        if ( Math.abs(a / W - b / W) != 1 ||
             Math.abs(a % W - b % W) != 1  )
            return false;

        /* RED pawns must move up the board */
        if ( pbs[a] == RED_PAWN && a / W <= b / W)
            return false;

        /* BLK pawns must move down the board */
        if ( pbs[a] == BLK_PAWN && a / W >= b / W)
            return false;

        /* The destination square must be empty */
        if (pbs[b] != BLANK)
            return false;

        return true;
    }

    /**
     * Returns whether a player may jump a checkers piece from <code>src</code>
     * to <code>dst</code>.
     * This method returns false if <code>src</code> is empty.
     * @param pbs       the partial board state on which the jump will occur
     * @param src       the source location of the jump
     * @param dst       the destination location of the jump
     * @return          true if the checkers piece at the source location may jump
     *                  to the destination location; false otherwise
     */
    public static boolean canJump(int[] pbs, int src, int dst)
    {
        int a = src, b = dst;

        /* Return false if src and dst are out of bounds */
        if (a < 0 || a >= H * W ||
            b < 0 || b >= H * W  )
            return false;

        /* a and b must be two squares apart on a diagonal */
        if ( Math.abs(a / W - b / W) != 2 ||
             Math.abs(a % W - b % W) != 2  )
            return false;

        /* Source location must be nonempty */
        if (pbs[a] == BLANK)
            return false;

        /* Midpoint square must contain opponent's checkers piece piece */
        int c = (a + b) / 2;
        if ( pbs[c] % 4 == pbs[a] % 4 )
            return false;

        /* Destination location must be empty */
        if (pbs[b] != BLANK)
            return false;

        /* Midpoint square must not be empty */
        if (pbs[c] == BLANK)
            return false;
        
        /* RED pawns must move up the board */
        if ( pbs[a] == RED_PAWN && a / W <= b / W)
            return false;

        /* BLK pawns must move down the board */
        if ( pbs[a] == BLK_PAWN && a / W >= b / W)
            return false;

        return true;
    }

    /**
     * Returns whether there exists a jump originating from <code>src</code>
     * for the specified player.
     * @param pbs       the partial board state on which the jump will occur
     * @param side      the side to perform the jump 
     * @param src       the source location of the jump
     * @return          true if there exists a destination location for the
     *                  the checkers piece at the source location to jump to;
     *                  false otherwise
     */
    public static boolean hasJump(int[] pbs, int side, int src)
    {
        int a = src;

        /* The checkers piece at source location must belong to side */
        if (pbs[a] % 4 != side)
            return false;

        /* Test jumps along each of four diagonals */
        for (int d : Utils.DIAG)
            if (Utils.canJump(pbs, a, a + 2 * d))
                return true;

        return false;
    }

    /**
     * Executes the specified move on the specified board state.
     * For performance reasons, this method does not check whether 
     * <code>move</code> is a legal move. It is the caller's responsibility
     * to ensure that <code>move</code> is legal.
     * <p>
     * This method returns a <i>revert stack</i> containing the original 
     * contents of any squares modified in making the move. The caller may use 
     * this stack to undo this move at a later time by invoking 
     * {@link #revert revert}. Each element of the revert stack is an 
     * <code>int[]</code> array with two elements: a location, and the original 
     * value at that location. Code in other classes are free to manipulate 
     * the revert stack as long as the LIFO property is maintained. 
     *
     * @param bs        the board state on which to perform the move
     * @param move      the move to perform
     * @return          the revert stack that undos this move
     * @see #isValidMove isValidMove
     * @see #revert revert
     */
    public static Stack<Integer> execute(int[] bs, Move move)
    {
        Stack<Integer> rv;

        if (Utils.isWalk(move))
            rv = Utils.walk(bs, move.get(0), move.get(1));
        else
        {
            rv = new Stack<Integer>();

            int a = move.get(0), b;
            for (int i = 1; i < move.size(); i++)
            {
                b = move.get(i);
                rv.addAll( Utils.jump(bs, a, b) );
                a = b;
            }
        }

        /* Crown kings that may have been created */
        rv.addAll( Utils.crownKings(bs) );

        return rv;
    }

    /**
     * Walks a checkers piece from <code>src</code> to <code>dst</code>.
     * <p>
     * This method is undefined if <code>src</code> is empty.
     * @param pbs       the partial board state on which to perform the walk
     * @param src       the source location
     * @param dst       the destination location
     * @return          the revert stack that undoes this method's
     *                  modifications to <code>pbs</code>
     * @see #revert revert
     */
    public static Stack<Integer> walk(int[] pbs, int src, int dst)
    {
        int a = src, b = dst;

        Stack<Integer> rv = new Stack<Integer>();

        rv.push(a);   rv.push(pbs[a]);
        rv.push(b);   rv.push(pbs[b]);

        pbs[b] = pbs[a]; 
        pbs[a] = BLANK; 

        return rv;
    }

    /**
     * Jumps a checkers piece from <code>src</code> to <code>dst</code>.
     * <code>src</code> and <code>dst</code> must be exactly two squares 
     * apart along a diagonal.
     * If the "jumped over" square contains a checker, that checkers piece is
     * removed as part of this jump.
     * <p>
     * This method is undefined if <code>src</code> is empty.
     * @param pbs       the partial board state on which to perform the jump
     * @param src       the source location
     * @param dst       the destination location
     * @return          the revert stack that undoes this method's
     *                  modifications to <code>pbs</code>
     * @see #revert revert
     */
    public static Stack<Integer> jump(int[] pbs, int src, int dst)
    {
        int a = src, b = dst;
        int c = (a + b) / 2;

        Stack<Integer> rv = new Stack<Integer>();

        rv.push(a);   rv.push(pbs[a]);
        rv.push(b);   rv.push(pbs[b]);
        rv.push(c);   rv.push(pbs[c]);

        pbs[b] = pbs[a];
        pbs[a] = BLANK;
        pbs[c] = BLANK;

        return rv;
    }

    /**
     * Crowns end-row pawns to kings on a specified board state.
     * @param pbs       the partial board state on which to crown pawns
     * @return          the revert stack that undoes this method's
     *                  modifications to <code>pbs</code>
     * @see #revert revert
     */
    public static Stack<Integer> crownKings(int[] pbs)
    {
        Stack<Integer> rv = new Stack<Integer>();

        /* Crown red pawns on top row */
        for (int j = 0 * W + 1; j < 1 * W; j += 2)
            if (pbs[j] == RED_PAWN)
            {
                rv.push(j);   rv.push(pbs[j]);
                pbs[j] = RED_KING;
            }

        /* Crown black pawns on bottom row */
        for (int j = 7 * W; j < H * W; j += 2)
            if (pbs[j] == BLK_PAWN)
            {
                rv.push(j);   rv.push(pbs[j]);
                pbs[j] = BLK_KING;
            }

        return rv;
    }

    /**
     * Undoes modifications to a board state by processing the given revert stack.
     * @param pbs       the partial board state to revert modifications
     * @param rv        the revert stack that describes the modifications
     * @see #execute execute
     */
    public static void revert(int[] pbs, Stack<Integer> rv)
    {
        while (!rv.empty())
        {
            int y = rv.pop(), x = rv.pop();

            pbs[x] = y;
        }
    }

    /**
     * Returns whether the two specified board states are equal.
     * @param A         the first board state
     * @param B         the second board state
     * @return          true if <code>A</code> and <code>B</code> represent
     *                  the same board state; false otherwise
     */
    public static boolean equalsBoardState(int[] A, int[] B)
    {
        for (int i = 0; i < H * W; i++)
                if (A[i] != B[i])
                    return false;

        return true;
    }

    /**
     * Converts the specified move as a list of pairwise locations. 
     * The list will contain <code>size() - 1</code> elements, each an 
     * <code>int[]</code> array of length two. For example, the move 
     * <code>44-30-12</code> will be formated as the pairs 
     * <code>(44, 30), (30, 12)</code>. 
     * @param move      a list of locations specifying the move
     * @return          the move formatted as a list of pairwise locations
     */
    public static List<int[]> convertMoveToPairwise(List<Integer> move)
    {
        List<int[]> ops = new LinkedList<int[]>();
        for (int i = 0; i < move.size() - 1; i++)
            ops.add( new int[]{ move.get(i), move.get(i+1) } );
        return ops;
    }

    /**
     * Loads a board state from the specified file path.
     * Please see {@link #parseBoardState parseBoardState} for a description 
     * of the expected file format of a board state.
     * @param filename  the name of the file to read from
     * @return          the board state contained in <code>filename</code>
     * @throws IOException      if an IO error occurred
     * @throws FormatException  if the data in <code>filename</code> is not in
     *                          the expected format
     * @see #reprBoardState reprBoardState
     */
    public static int[] loadBoardState(String filename) 
        throws IOException, FormatException
    {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String s = "";
        String t;
        while ((t = in.readLine()) != null)
            s += t;
        return parseBoardState(s);
    }

    /**
     * Constructs a board state by parsing the specified string.
     * The string must contain exactly 64 non-whitespace characters.
     * These correspond to the contents of the 64 squares of the checkers board.
     * The characters map to checkers pieces as follows:
     * <ul>
     * <li> <code>'-'</code> is <code>BLANK</code>
     * <li> <code>'r'</code> is <code>RED_PAWN</code>
     * <li> <code>'R'</code> is <code>RED_KING</code>
     * <li> <code>'b'</code> is <code>BLK_PAWN</code>
     * <li> <code>'B'</code> is <code>BLK_KING</code>
     * </ul>
     * Any other non-whitespace character is invalid and will result in a 
     * <code>FormatException</code>.
     * @param s         the string to construct the board state state from
     * @return          the board state represented by <code>s</code>
     * @throws FormatException  if <code>s</code> is not in the expected format
     * @see #loadBoardState loadBoardState
     * @see #reprBoardState reprBoardState
     */
    public static int[] parseBoardState(String s) throws FormatException
    {
        int[] bs = new int[H * W];

        int i = 0;
        for (char c : s.toCharArray())
        {
            if (Character.isWhitespace(c))
                continue;

            switch (c)
            {
                case '-':
                    bs[i] = BLANK;    break;
                case 'r':
                    bs[i] = RED_PAWN; break;
                case 'b':
                    bs[i] = BLK_PAWN; break;
                case 'R':
                    bs[i] = RED_KING; break;
                case 'B':
                    bs[i] = BLK_KING; break;
                default:
                    throw new FormatException(
                            "'" + c + "' is not a valid checkers piece"
                            );
            }
            i += 1;
        }

        if (i != H * W)
            throw new FormatException(
                    "End of file reached before all pieces are read"
                    );
        return bs;
    }

    /**
     * Returns a string representing the specified board state.
     * The string will be compatible with the format specified in 
     * {@link #parseBoardState parseBoardState}.
     * @param bs        the board state to represent as a string
     * @return          a string representing the specified board state
     * @see #parseBoardState parseBoardState
     */
    public static String reprBoardState(int[] bs)
    {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < H * W; i++)
        {
            switch (bs[i])
            {
                case RED_PAWN: s.append('r'); break;
                case RED_KING: s.append('R'); break;
                case BLK_PAWN: s.append('b'); break;
                case BLK_KING: s.append('B'); break;
                default:       s.append('-'); break;
            }
            s.append(' ');
            if ((i + 1) % W == 0)
                s.append('\n');
        }
        return s.toString();
    }

    /**
     * Returns a string representing a specified side in a checkers game.
     * This method returns "RED" for the red player, "BLK" for the black 
     * player, and "NEITHER" for neither player.
     * @param side      the side to represent as a string
     * @return          the string representing the side: "RED", "BLK", 
     *                  or "NEITHER"
     */
    public static String reprSide(int side)
    {
        switch(side)
        {
            case RED: return "RED";
            case BLK: return "BLK";
            case NEITHER: 
            default:
                return "NEITHER";
        }
    }

    /**
     * Parses a string representing a side in a checkers game.
     * @param str       a string representation of a side
     * @return          the side the string represents
     * @throws FormatException  if <code>str</code> is not in the expected format
     */
    public static int parseSide(String str) throws FormatException
    {
        if (str.equals("RED")) return RED;
        if (str.equals("BLK")) return BLK;
        if (str.equals("NEITHER")) return NEITHER;
        throw new FormatException(str + " is not a valid side");
    }

    /**
     * Returns a string representing a checkers board location using algebraic 
     * chess notation. This method returns <code>"??"</code> if the 
     * <code>loc</code> is out of bounds.
     * @param loc       the location to represent as a string
     * @return          the string representing the specified location
     */
    public static String reprLocation(int loc)
    {
        if (loc < 0 || loc >= W * H)
            return "??";

        String row = "" + (8 - loc / W);
        String col = "" + (char)('a' + (loc % W));
        return col + row;
    }

    /**
     * Parses a string representing a checkers board location using algebraic 
     * chess notation.
     * @param str       a string representation of a location
     * @return          the location the string represents
     * @throws FormatException  if <code>str</code> is not in the expected format
     */
    public static int parseLocation(String str) throws FormatException
    {
        int row = 8 - (str.charAt(1) - '0');
        int col = str.toLowerCase().charAt(0) - 'a';
        if (row < 0 || row >= 8 || col < 0 || col >= 8)
            throw new FormatException(str + " is not a valid location");
        return row * W + col;
    }

    /**
     * Returns a string representing a checkers piece. This method
     * returns <code>null</code> if <code>piece</code> is not defined in
     * <code>CheckersConsts</code>.
     * @param piece     the checkers piece to represent as a string
     * @return          the string representing the specified checkers piece
     */
    public static String reprCheckersPiece(int piece)
    {
        switch(piece)
        {
            case RED_PAWN: return "RED PAWN";
            case RED_KING: return "RED KING";
            case BLK_PAWN: return "BLK PAWN";
            case BLK_KING: return "BLK KING";
            case BLANK:    return "BLANK";
            default: 
                return null;
        }
    }

    /**
     * Returns a string representing a move. 
     * @param move      the list of locations representing the move
     * @return          a string representing the specified move, or "null" 
     *                  if move is null
     */
    public static String reprMove(List<Integer> move)
    {
        if (move == null)
            return "null";

        StringBuffer s = new StringBuffer();
        for (int i = 0; i < move.size(); i++)
        {
            s.append(Utils.reprLocation(move.get(i)));
            if (i != move.size() - 1)
                s.append("-");
        }
        return s.toString();
    }

    /**
     * Parses a string representing a checkers move with locations in algebraic
     * chess notation.
     * @param str       a string representation of a move
     * @return          the move the string represents
     * @throws FormatException  if <code>str</code> is not in the expected format
     */
    public static Move parseMove(String str) throws FormatException
    {
        List<Integer> move = new ArrayList<Integer>();

        if (str.length() % 3 != 2)
            throw new FormatException(str + " has incorrect length");

        for (int i = 0; i < str.length(); i += 3)
        {
            int loc = parseLocation(str.substring(i, i + 2));
            move.add(loc);

            if (str.charAt(i + 2) != '-')
                throw new FormatException(str + " is not a valid move");
        }

        return new Move(move);
    }

    /**
     * The side which moves first in a checkers game.
     */
    public static final int INITIAL_SIDE = RED;

    /**
     * The board state of the starting position in a checkers game.
     */
    public static final int[] INITIAL_BOARDSTATE;
    static
    {
        INITIAL_BOARDSTATE = new int[H * W];
        for (int i = 0; i < H * W; i++)
            INITIAL_BOARDSTATE[i] = BLANK;
         
        int[] L = new int[] {  1,  3,  5,  7,  8, 10, 12, 14, 17, 19, 21, 23 };
        for ( int i : L )
            INITIAL_BOARDSTATE[i] = BLK_PAWN;

        for ( int i : L )
            INITIAL_BOARDSTATE[W * H - 1 - i] = RED_PAWN;

    };
}
