package signalprocesser.voronoi.representation.simpletriangulation;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.representation.AbstractRepresentation;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

public class SimpleTriangulationRepresentation extends AbstractRepresentation {

    /* ***************************************************** */
    // Variables
    
    private final ArrayList<VTriangle> triangles = new ArrayList<VTriangle>();
    
    /* ***************************************************** */
    // Constructor
    
    public SimpleTriangulationRepresentation() {
        // do nothing
    }

    /* ***************************************************** */
    // Create Point

    public VPoint createPoint(int x, int y) {
        return new VPoint(x, y);
    }
    
    /* ***************************************************** */
    // Data/Representation Interface Method
    
    // Executed before the algorithm begins to process (can be used to
    //   initialise any data structures required)
    public void beginAlgorithm(Collection<VPoint> points) {
        // Reset the triangle array list
        triangles.clear();
    }
    
    // Called to record that a vertex has been found
    public void siteEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 ) { }
    public void circleEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 , int circle_x , int circle_y ) {
        VTriangle triangle = new VTriangle(circle_x, circle_y);
        triangle.p1 = n1.siteevent.getPoint();
        triangle.p2 = n2.siteevent.getPoint();
        triangle.p3 = n3.siteevent.getPoint();
        triangles.add( triangle );
    }
    
    // Called when the algorithm has finished processing
    public void endAlgorithm(Collection<VPoint> points, int lastsweeplineposition, VLinkedNode headnode) {
        // do nothing
    }
    
    /* ***************************************************** */    
    // Paint Method
    
    public void paint(Graphics2D g) {
        for ( VTriangle triangle : triangles ) {
            g.drawLine( triangle.p1.x , triangle.p1.y , triangle.p2.x , triangle.p2.y );
            g.drawLine( triangle.p2.x , triangle.p2.y , triangle.p3.x , triangle.p3.y );
            g.drawLine( triangle.p3.x , triangle.p3.y , triangle.p1.x , triangle.p1.y );
        }
    }
    
    /* ***************************************************** */    
}
