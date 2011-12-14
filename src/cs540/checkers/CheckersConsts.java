package cs540.checkers;

/**
 * This class contains the important constants that are used throughout 
 * the checkers project as static fields. Users are encouraged to statically
 * import this class by declaring
 * <pre><code>import static cs540.checkers.CheckersConsts.*;</code></pre>
 * at the beginning of a file.
 * @author Justin Tritz
 * @author David He
 */
public class CheckersConsts
{
    /** The number of squares wide a checkers board is */
    public final static int W = 8;
    /** The number of squares tall a checkers board is */
    public final static int H = 8;

    /** The integer specifying a red pawn, as part of the checkers piece enumeration scheme */
    public final static int RED_PAWN = 0;
    /** The integer specifying a black pawn, as part of the checkers piece enumeration scheme */
    public final static int BLK_PAWN = 1; 
    /** The integer specifying a blank checkers board square, as part of the checkers piece enumeration scheme */
    public final static int BLANK    = 2;
    /** The integer specifying a red king, as part of the checkers piece enumeration scheme */
    public final static int RED_KING = 4;
    /** The integer specifying a black king, as part of the checkers piece enumeration scheme */
    public final static int BLK_KING = 5;

    /** The lowest int value not in the checkers enumeration. */
    public final static int PIECES_MAX = 6;

    /** The integer specifying the red player, as part of the side enumeration scheme */
    public final static int RED = 0;
    /** The integer specifying the black player, as part of the side enumeration scheme */
    public final static int BLK = 1;
    /** The integer specifying neither player, as part of the side enumeration scheme */
    public final static int NEITHER = 2;
}
