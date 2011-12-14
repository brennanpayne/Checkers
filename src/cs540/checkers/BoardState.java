package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;
import java.io.*;

/**
 * This class encapsulates a board state, which is otherwise stored as an 
 * <code>int[]</code> array. The member methods of this class are parallel to 
 * the static methods of {@link Utils Utils}. This object encapsulation is 
 * provided as an alternative to using an <code>int[]</code> array to represent
 * a board state. This class is only slightly more user friendly than the
 * static methods in <code>Utils</code>, and not at all higher-level. All the
 * nuances in <code>Utils</code> are likely retained in <code>BoardState</code>.
 * Please read the documentation for {@link Utils Utils} before using this 
 * class.
 * <p>
 * This class, besides maintaining the board state, also maintains the active
 * side as the state variable <code>side</code>. This active side variable is 
 * automatically modified by {@link #execute execute} and 
 * {@link #revert() revert()}. 
 * <p>
 * In addition:
 * <ul>
 * <li> The revert stack is maintained as one large stack in the field 
 * {@link #rv rv}. The methods {@link #walk walk}, {@link #jump jump}, 
 * and {@link #crownKings crownKings} automatically push to that stack. 
 * <li> Unlike {@link Utils#execute Utils.execute}, the <code>execute</code> 
 * of this class keeps track of how many elements it pushes to the revert 
 * stack. The {@link #revert revert} method which takes no arguments is
 * introduced to take advantage of this feature. This behavior is similar
 * to the x86 ESP pointer. 
 * <li> The forced jump property, where the active side must make a jump
 * move, is only calculated once per state and saved in the field 
 * {@link #forcedJump forcedJump}. This value is also saved to the revert stack.
 * </ul>
 * <p>
 * The following example demonstrates the use of <code>BoardState</code>. The
 * method <code>doSomething</code>, when given <code>BoardState</code>
 * <code>bs</code>, gets the list of all possible moves, selects the first 
 * move on that list, makes that move, undoes that move, selects a random move 
 * from the list, then performs that random move. 
 * <blockquote><pre>
 * 
 * public void doSomething(BoardState bs)
 * {
 *     // Get a list of all valid moves for the active side on bs
 *     List<Move> moveList = bs.getAllPossibleMoves();
 *
 *     // Select the first move on the list of valid moves
 *     // (assumes moveList is not empty)
 *     Move move = moveList.get(0);
 *
 *     // Get the active side of bs
 *     int side = bs.side;
 *
 *     // Execute the selected move
 *     bs.execute(move);
 *
 *     // execute() and revert() swap sides. This should print
 *     // "old side = 0, new side = 1" or 
 *     // "old side = 1, new side = 0".
 *     System.out.println("old side = " + side + ", new side = " + bs.side);
 *
 *     // Undo the selected move
 *     bs.revert();
 *     
 *     // Select a random move from the list of valid moves, then execute it. 
 *     int rand = (int)(Math.random() * moveList.size());
 *     bs.execute(moveList.get(rand));
 * }
 * </pre></blockquote>
 */
public class BoardState 
{
    /**
     * The array representing the underlying board state.
     */
    public int[] D;

    /**
     * The integer signifying the active side. This variable is automatically
     * updated by {@link #execute execute} and {@link #revert() revert}.
     */
    public int side;

    /**
     * The revert stack. Items on this stack are stored in (location, value) 
     * pairs. The item on the bottom is a location on the checkers board, 
     * and the item on the top is the piece at that location before the modification.
     * The location can be a virtual location (e.g. with value >= 64), which is 
     * used for saving <code>forcedJump</code> and other information not 
     * directly on the board.
     */
    protected Stack<Integer> rv;

    /**
     * The default capacity of the revert stack. 
     */
    protected static final int RV_INITIAL_CAPACITY = 500;

    /**
     * Whether a jump is forced for the active side. Methods should not access
     * this variable directly, instead calling {@link #isForcedJump isForcedJump}. 
     * This variable is updated on demand when <code>isForcedJump</code> is called.
     */
    protected boolean forcedJump;

    /**
     * Whether {@link #forcedJump forcedJump} is updated. This variable is cleared
     * after {@link #execute execute} and {@link #revert() revert}, and set during
     * {@link #isForcedJump isForcedJump}.
     */
    protected boolean forcedJumpKnown;

    /**
     * Constructs a <code>BoardState</code> object given a board state as an 
     * array and an integer specifying the active side.
     * @param D         an array representing a board state
     * @param side      an integer signifying the active side
     */
    public BoardState(int[] D, int side)
    {
        this.D = D.clone();
        this.side = side;

        rv = new Stack<Integer>();
        rv.ensureCapacity(RV_INITIAL_CAPACITY);

        forcedJumpKnown = false;
    }

    /**
     * Constructs a copy of a board state object.
     * @param bs        the board state object to copy
     */
    public BoardState(BoardState bs)
    {
        this(bs.D, bs.side);
    }

    /**
     * Returns whether <code>move</code> is legal for the active side. 
     * @param move      the move to test for legality
     * @return          true if <code>move</code> is legal; false otherwise
     */
    public boolean isValidMove(Move move)
    {
        return Utils.isValidMove(D, side, move);
    }

    /**
     * Returns whether the active side loses because it cannot
     * make any legal moves.
     * @return          true if <code>side</code> has no legal moves;
     *                  false otherwise
     */
    public boolean isLoser()
    {
        return getAllPossibleMoves().size() == 0;
    }

    /**
     * Returns a list of all possible moves that the active side may perform
     * on this board state.
     * <p>
     * The ordering of moves in this list is not defined and may change between 
     * successive calls. 
     * @return          a list of all the moves that the active side may 
     *                  perform on this board state
     */
    public List<Move> getAllPossibleMoves()
    {
        if (isForcedJump())
            return findJumpMoves();
        else
            return findWalkMoves();
    }

    /**
     * Returns a list of all walk moves that the active side may perform on 
     * this board state. This method assumes that the active side
     * is not otherwise forced to make a jump move. The moves in this list 
     * are legal if and only if the active side is not required to make a jump 
     * move.
     * <p>
     * The ordering of moves in this list is not defined and may change 
     * between successive calls. 
     * @return          an inclusive list of walk moves the active side may
     *                  perform on this board state
     */
    protected List<Move> findWalkMoves()
    {
        List<Move> moveList = new ArrayList<Move>();

        for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
        {
            if (D[a] % 4 != side)
                continue;

            for (int d : Utils.DIAG)
            {
                int b = a + d;
                if ( !canWalk(a, b) )
                    continue;

                moveList.add(new Move( Arrays.asList(a, b) ));
            }
        }

        return moveList;
    }

    /**
     * Returns a list of all jump moves that the active side may perform on 
     * this board state.
     * <p>
     * The ordering of the moves in this list is not defined and may change 
     * between successive calls. 
     * @return          an inclusive list of jump moves the active side
     *                  perform on this board state
     */
    protected List<Move> findJumpMoves()
    {
        List<Move> moveList = new ArrayList<Move>();
        for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
        {
            if (D[a] % 4 != side)
                continue;

            MutableMove pmove = new MutableMove();
            pmove.add(a);

            findJumpMovesHelper(moveList, pmove);
        }
        return moveList;
    }
    
    /**
     * Adds to <code>moveList</code> all jump moves that begin with the 
     * partial move <code>pmove</code>. 
     * This helper method recursively calls itself while updating this
     * board state as the partial board state resulting from performing
     * <code>pmove</code> on the original board state. 
     * @param pmove     the partial move to search from
     * @param moveList  the list of moves to add newly found jump moves to
     * @see #findJumpMoves findJumpMoves
     */
    protected void findJumpMovesHelper(List<Move> moveList, MutableMove pmove)
    {
        int a = pmove.get(pmove.size() - 1);
        boolean canJumpAgain = false;

        for (int d : Utils.DIAG)
        {
            int b = a + 2 * d;
            if ( !canJump(a, b) )
                continue;

            canJumpAgain = true;

            pmove.add(b);

            int rvTar = rv.size();
            jump(a, b);
            findJumpMovesHelper(moveList, pmove);
            revert(rvTar);

            pmove.remove(pmove.size() - 1);
        }
        
        if (!canJumpAgain && pmove.size() >= 2)
            moveList.add( new Move(pmove) );
    }

    /**
     * Returns whether the active player may walk a checkers piece from <code>src</code>
     * to <code>dst</code>.
     * This method assumes that the active player is not restricted from executing a
     * walk move by the existence of a jump move.
     * This method is undefined if <code>src</code> is empty.
     * @param src   the source location of the walk
     * @param dst   the destination location of the walk
     * @return      undefined if <code>src</code> is empty;
     *              false if the move is not a walk move;
     *              false if the move is not legal;
     *              true otherwise
     */
    public boolean canWalk(int src, int dst)
    {
        return Utils.canWalk(D, src, dst);
    }

    /**
     * Returns whether the active player may jump a checkers piece from <code>src</code>
     * to <code>dst</code>.
     * This method returns false if <code>src</code> is empty.
     * @param src       the source location of the jump
     * @param dst       the destination location of the jump
     * @return          true if the checkers piece at the source location may jump
     *                  to the destination location; false otherwise
     */
    public boolean canJump(int src, int dst)
    {
        return Utils.canJump(D, src, dst);
    }

    /**
     * Returns whether there exists a jump originating from <code>src</code>
     * for the active player.
     * @param src       the source location of the jump
     * @return          true if there exists a destination location for the
     *                  the checkers piece at the source location to jump to;
     *                  false otherwise
     */
    public boolean hasJump(int src)
    {
        int a = src;

        /* The checkers piece at source location must belong to side */
        if (D[a] % 4 != side)
            return false;

        /* Test jumps along each of four diagonals */
        for (int d : Utils.DIAG)
            if (canJump(a, a + 2 * d))
                return true;

        return false;
    }

    /**
     * Returns whether the active side must make a jump move.
     * This method is equivalent to whether the active side has at least
     * one jump move. 
     * @return          true if the active side must make
     *                  a jump move; false otherwise
     */
    public boolean isForcedJump()
    {
        if (!forcedJumpKnown)
        {
            forcedJumpKnown = true;
            forcedJump = false;
            for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
                if (hasJump(a))
                {
                    forcedJump = true;
                    break;
                }
        }

        return forcedJump;
    }

    /**
     * An virtual checkers board square used for saving the 
     * <code>forcedJump</code> variable onto the revert stack.
     */
    protected static int SQ_FORCED_JUMP = W * H;

    /**
     * An virtual checkers board square used for saving the size of the 
     * revert stack before the most recent {@link #execute execute} onto
     * the revert stack. 
     */
    protected static int SQ_MOVE_RVTAR = W * H + 1;

    /**
     * Executes the specified move on this board state.
     * For performance reasons, this method does not check whether 
     * <code>move</code> is a legal move. It is the caller's responsibility
     * to ensure that <code>move</code> is legal.
     * <p>
     * This method pushes all modifications to this board state object onto
     * the revert stack. The caller may invoke {@link #revert() revert()} at a 
     * later time to undo this move.
     * @param move      the move to perform
     * @see #isValidMove isValidMove
     * @see #revert revert
     */
    public void execute(Move move)
    {
        int rvTar = rv.size();

        if (forcedJumpKnown)
        {
            rv.push(SQ_FORCED_JUMP);
            rv.push(isForcedJump() == true ? 1 : 0);
        }

        if (Utils.isWalk(move))
            walk(move.get(0), move.get(1));
        else
        {
            int a = move.get(0), b;
            for (int i = 1; i < move.size(); i++)
            {
                b = move.get(i);
                jump(a, b);
                a = b;
            }
        }

        /* Crown kings that may have been created */
        crownKings();

        /* Swap sides */
        side = (side + 1) % 2;

        /* new state: forcedJump not known */
        forcedJumpKnown = false;

        rv.push(SQ_MOVE_RVTAR);
        rv.push(rvTar);
    }

    /**
     * Walks a checkers piece from <code>src</code> to <code>dst</code>.
     * <p>
     * This method is undefined if <code>src</code> is empty.
     * <p>
     * All modifications made to this board state object are pushed onto its 
     * revert stack. 
     * @param src       the source location
     * @param dst       the destination location
     */
    protected void walk(int src, int dst)
    {
        int a = src, b = dst;

        rv.push(a);   rv.push(D[a]);
        rv.push(b);   rv.push(D[b]);

        D[b] = D[a]; 
        D[a] = BLANK; 
    }

    /**
     * Jumps a checkers piece from <code>src</code> to <code>dst</code>.
     * <code>src</code> and <code>dst</code> must be exactly two squares 
     * apart along a diagonal.
     * Any checkers on the square between <code>src</code> and 
     * <code>dst</code> removed as part of this jump.
     * <p>
     * This method is undefined if <code>src</code> is empty.
     * <p>
     * All modifications made to this board state object are pushed onto its 
     * revert stack. 
     * @param src       the source location
     * @param dst       the destination location
     */
    protected void jump(int src, int dst)
    {
        int a = src, b = dst;
        int c = (a + b) / 2;

        rv.push(a);   rv.push(D[a]);
        rv.push(b);   rv.push(D[b]);
        rv.push(c);   rv.push(D[c]);

        D[b] = D[a];
        D[a] = BLANK;
        D[c] = BLANK;
    }

    /**
     * Crowns end-row pawns to kings on this board state.
     * <p>
     * All modifications made to this board state object are pushed onto its 
     * revert stack. 
     */
    protected void crownKings()
    {
        /* Crown red pawns on top row */
        for (int j = 0 * W + 1; j < 1 * W; j += 2)
            if (D[j] == RED_PAWN)
            {
                rv.push(j);   rv.push(D[j]);
                D[j] = RED_KING;
            }

        /* Crown black pawns on bottom row */
        for (int j = 7 * W; j < H * W; j += 2)
            if (D[j] == BLK_PAWN)
            {
                rv.push(j);   rv.push(D[j]);
                D[j] = BLK_KING;
            }
    }

    /**
     * Undoes modifications to this board state object by popping elements off 
     * the revert stack until the size of the revert stack is <code>rvTar</code>.
     * This is a helper method to {@link #revert() revert()}.
     * @param rvTar     the target size of the revert stack
     * @see #revert() revert()
     * @see #execute execute
     */
    protected void revert(int rvTar)
    {
        while (rv.size() > rvTar)
        {
            int y = rv.pop(), x = rv.pop();

            /* Virtual location for forcedJump */
            if (x == SQ_FORCED_JUMP)
            {
                forcedJump = y == 1;
                forcedJumpKnown = true;
            }
            /* Virtual location for rvTar (do nothing; handled by revert(void) */
            else if (x == SQ_MOVE_RVTAR)
                ;
            else
                D[x] = y;
        }
    }

    /**
     * Undoes the most recent move made by <code>execute</code> on this board 
     * state. The top of the revert stack must be a move mde by <code>execute</code>.
     * @see #execute execute
     */
    public void revert()
    {
        /* Clear forcedJumpKnown; this is later set in revert(int) */
        forcedJumpKnown = false;

        /* rvTar must be on top of stack */
        int rvTar = rv.pop(), x = rv.pop();

        if (x != SQ_MOVE_RVTAR)
            throw new IllegalStateException("Top of stack is not a move!");

        /* revert(int) does remaining processing */
        revert(rvTar);

        /* Swap turns */
        side = (side + 1) % 2;
    }

    /**
     * Returns a string representation of this board state.
     * @return      a string representation of this board state
     */
    public String toString()
    {
        return Utils.reprBoardState(D);
    }

    public boolean equals(Object o)
    {
        if ((Object)this == o)
            return true;

        int[] oD = ((BoardState)o).D;
        
        return Utils.equalsBoardState(D, oD);
    }
}
