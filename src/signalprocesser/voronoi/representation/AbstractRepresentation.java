package signalprocesser.voronoi.representation;
import java.awt.Graphics2D;

import signalprocesser.voronoi.VPoint;

abstract public class AbstractRepresentation implements RepresentationInterface {
    
    public AbstractRepresentation() {
    }

    public abstract VPoint createPoint(int x, int y);
            
    public abstract void paint(Graphics2D g);
    
}
