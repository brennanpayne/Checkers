package cs540.checkers.ui;
import cs540.checkers.*;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;

import java.awt.event.*;
import javax.swing.event.*;

/**
 * This is the model of the {@link CheckersBoard CheckersBoard} widget. 
 * @see CheckersBoard CheckersBoard
 * @author David He
 */
public class CheckersBoardModel 
{
    protected int enable;
    protected int[] bs, pbs;
    protected int[] hint;
    protected MutableMove pmove;
    protected Map<Integer, Integer> removedPieces;

    public CheckersBoardModel()
    {
        this(Utils.INITIAL_BOARDSTATE);
    }

    public CheckersBoardModel(int[] bs)
    {
        this.bs = bs.clone();
        this.enable = NEITHER;

        this.pmove = new MutableMove();
        this.pbs = bs.clone();

        this.hint = new int[W * H];
        this.removedPieces = new HashMap<Integer, Integer>();
    }

    public boolean isSelection()
    {
        return pmove.size() > 0;
    }

    public void boardPressed()
    {
        fireActionPerformed();
    }

    public void squarePressed(int b)
    {
        appendPartialMove(b);
    }

    public void appendPartialMove(int b)
    {
        pmove.add(b);
        if (!PMoveUtils.isValidPartialMove(bs, enable, pmove))
        {
            pmove.remove(pmove.size() - 1);
            if (pmove.size() > 0 && pmove.get(pmove.size() - 1) == b)
            {
                pmove.remove(pmove.size() - 1);
            }
            else if (bs[b] % 4 == enable)
            {
                pmove.clear();
                pmove.add(b);
            }
            else if (pmove.contains(b))
            {
                pmove.subList(pmove.lastIndexOf(b) + 1, pmove.size()).clear();
            }
        }

        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public void clearPartialMove()
    {
        pmove.clear();
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public MutableMove getPartialMove()
    {
        return pmove;
    }

    public void setPartialMove(MutableMove pmove)
    {
        this.pmove = pmove;
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    protected void updateBoardState()
    {
        removedPieces.clear();
        pbs = bs.clone();
        List<int[]> ops = Utils.convertMoveToPairwise(pmove);

        if (Utils.isWalk(pmove))
        {
            int[] op = ops.get(0);
            Utils.walk(pbs, op[0], op[1]);
        }
        else
        {
            for (int[] op : ops)
            {
                int mid = (op[0] + op[1]) / 2;
                removedPieces.put(mid, pbs[mid]);
                Utils.jump(pbs, op[0], op[1]);
            }
        }

        if (Utils.isValidMove(bs, enable, new Move(pmove)))
        {
            Move move = new Move(pmove);
            pmove.clear();
            pbs = bs.clone();
            setEnabled(NEITHER);
            fireMoveSelected(move);
        }
    }

    public static final int HINT_NONE    = 0;
    public static final int HINT_VALID   = 1;
    public static final int HINT_INVALID = 2;

    /* 
     * pbs must be updated before this 
     * pmove cannot be a valid move, i.e. must be partial
     */
    protected void updateHintState()
    {
        /* Clear hints */
        for (int i = 0; i < H * W; i++)
            hint[i] = HINT_NONE;

        if (pmove.size() == 0)
            return;

        int a = pmove.get(pmove.size() - 1);

        if ( pmove.size() > 1 || Utils.isForcedJump(pbs, enable) )
            for (int d : Utils.DIAG)
            {
                int b = a + 2 * d;
                if (Utils.canJump(pbs, a, b))
                    hint[b] = HINT_VALID;
            }
        else if (pmove.size() == 1)
            for (int d : Utils.DIAG)
            {
                int b = a + d;
                if (Utils.canWalk(pbs, a, b))
                    hint[b] = HINT_VALID;
            }

        if ( pmove.size() == 1 && Utils.isForcedJump(pbs, enable) )
            for (int d : Utils.DIAG)
            {
                int b = a + d;
                if (Utils.canWalk(pbs, a, b))
                    hint[b] = HINT_INVALID;
            }
    }

    public int[] getBoardState() { return pbs.clone(); }

    public void setBoardState(int[] bs) 
    { 
        if (Arrays.equals(this.bs, bs))
            return;

        this.bs = bs.clone();
        pmove.clear();
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public int getPiece(int index)
    { 
        if (removedPieces.containsKey(index))
            return removedPieces.get(index);
        else
            return pbs[index];
    }

    public int getHint(int index) { return hint[index]; }
    
    public int getEnabled() { return enable; }

    public void setEnabled(int enable) 
    { 
        if (this.enable == enable)
            return;

        clearPartialMove();
        this.enable = enable;
        fireStateChanged();
    }

    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Adds a move listener to this component. 
     * @param listener  the MoveListener to be added
     */
    public void addMoveListener(MoveListener listener)
    { listenerList.add(MoveListener.class, listener); }

    /**
     * Removes a move listener to this component. 
     * @param listener  the MoveListener to be removed
     */
    public void removeMoveListener(MoveListener listener)
    { listenerList.remove(MoveListener.class, listener); }

    /**
     * Adds a change listener to this component. 
     * @param listener  the ChangeListener to be added
     */
    public void addChangeListener(ChangeListener listener)
    { listenerList.add(ChangeListener.class, listener); }

    /**
     * Removes a change listener to this component. 
     * @param listener  the ChangeListener to be removed 
     */
    public void removeChangeListener(ChangeListener listener)
    { listenerList.remove(ChangeListener.class, listener); }

    /**
     * Adds an action listener to this component. 
     * @param listener  the ActionListener to be added
     */
    public void addActionListener(ActionListener listener)
    { listenerList.add(ActionListener.class, listener); }

    /**
     * Removes an action listener to this component. 
     * @param listener  the ActionListener to be removed
     */
    public void removeActionListener(ActionListener listener)
    { listenerList.remove(ActionListener.class, listener); }

    /**
     * Runs each MoveListener's moveSelected method. 
     */
    protected void fireMoveSelected(Move move)
    {
        MoveEvent e = new MoveEvent(this, move);
        for (MoveListener listener : listenerList.getListeners(MoveListener.class))
            listener.moveSelected(e);
    }

    /**
     * Runs each ChangeListener's stateChanged method. 
     */
    protected void fireStateChanged()
    {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners(ChangeListener.class))
            listener.stateChanged(e);
    }

    /**
     * Runs each ActionListener's actionPerformed method. 
     */
    protected void fireActionPerformed()
    {
        ActionEvent e = new ActionEvent(this, 0, "asdf");
        for (ActionListener listener : listenerList.getListeners(ActionListener.class))
            listener.actionPerformed(e);
    }
}
