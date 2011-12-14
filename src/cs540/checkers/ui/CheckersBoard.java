package cs540.checkers.ui;
import cs540.checkers.*;
import static cs540.checkers.CheckersConsts.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * This widget draws a checkers board on which a user may select moves or 
 * observe moves. This class contains 64 buttons, each representing a square, 
 * in checkerboard colors. 
 * @see CheckersBoardModel CheckersBoardModel
 * @see CBSquare CBSquare
 * @author Justin Tritz
 * @author David He
 */
public class CheckersBoard extends JPanel implements ChangeListener, ActionListener
{
    protected CheckersBoardModel model;

    protected CBSquare[] sqs;

    public CheckersBoard(CheckersBoardModel model)
    {
        this.model = model;
        model.addChangeListener(this);

        setDoubleBuffered(true);
        
        setLayout(new GridLayout(W, H));

        sqs = new CBSquare[H * W];
        for (int i = 0; i < H * W; i++)
        {
            sqs[i] = new CBSquare(model, i);
            add(sqs[i]);
            sqs[i].getModel().addActionListener(this);
        }
    }

    protected static final Stroke arrowStroke = new BasicStroke(5.0f);
    protected static final Color arrowColor = new Color(0, 0, 128);
    protected static final double arrowWidth = 12.0;
    protected static final double arrowHeight = 18.0;
    protected static final double arrowOffset = 5.0;

    /**
     * Draws an arrow from <code>(ax, ay)</code> to <code>(bx, by)</code>.
     * The head of the arrow is a triangle specified by {@link #arrowWidth arrowWidth}
     * and {@link #arrowHeight arrowHeight}. 
     */
    /** */
    public void drawArrow(Graphics2D g, int ax, int ay, int bx, int by)
    {
        double d = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
        double zx = (bx - ax) / d;
        double zy = (by - ay) / d;

        ax += (int)(arrowOffset * zx);
        ay += (int)(arrowOffset * zy);
        bx -= (int)(arrowOffset * zx);
        by -= (int)(arrowOffset * zy);

        // projection vector
        double mx = (bx - ax) / d;
        double my = (by - ay) / d;
        // normal vector
        double nx = -my;
        double ny =  mx;

        int px = (int)(bx - mx * arrowHeight + nx * arrowWidth);
        int py = (int)(by - my * arrowHeight + ny * arrowWidth);
        int qx = (int)(bx - mx * arrowHeight - nx * arrowWidth);
        int qy = (int)(by - my * arrowHeight - ny * arrowWidth);

        Polygon arrowPoly = new Polygon();
        arrowPoly.addPoint(bx, by);
        arrowPoly.addPoint(px, py);
        arrowPoly.addPoint(qx, qy);

        g.setStroke(arrowStroke);
        g.drawLine(ax, ay, bx - (int)(arrowHeight * mx), by - (int)(arrowHeight * my));

        g.setStroke(new BasicStroke());
        g.fill(arrowPoly);
    }

    /**
     * Draws arrows for a move.
     */
    /** */
    public void paintArrows(Graphics _g)
    {
        Graphics2D g = (Graphics2D)_g;

        java.util.List<int[]> pair = Utils.convertMoveToPairwise(model.getPartialMove());
        g.setColor(arrowColor);
        for (int i = 0; i < pair.size(); i++)
        {
            int ax = (int)(CBSquare.SQUARE_WIDTH  * (pair.get(i)[0] % W + 0.5));
            int ay = (int)(CBSquare.SQUARE_HEIGHT * (pair.get(i)[0] / W + 0.5));
            int bx = (int)(CBSquare.SQUARE_WIDTH  * (pair.get(i)[1] % W + 0.5));
            int by = (int)(CBSquare.SQUARE_HEIGHT * (pair.get(i)[1] / W + 0.5));
            drawArrow(g, ax, ay, bx, by);
        }
    }

    public void paint(Graphics g)
    {
        /* 
         * Normally paintComponent() is called before paintChildren().
         * Thus we override paint so that arrows are painted on top of the 
         * checker board.
         */
        super.paint(g);
        paintArrows(g);
    }

    public void stateChanged(ChangeEvent e)
    {
    }

    public void actionPerformed(ActionEvent e)
    {
        if (model.getEnabled() == NEITHER)
            model.boardPressed();
    }

    public CheckersBoardModel getModel() { return model; }
}
