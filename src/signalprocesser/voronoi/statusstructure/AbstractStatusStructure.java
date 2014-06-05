package signalprocesser.voronoi.statusstructure;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import signalprocesser.voronoi.VoronoiTest;
import signalprocesser.voronoi.eventqueue.EventQueue;
import signalprocesser.voronoi.eventqueue.VSiteEvent;
import signalprocesser.voronoi.statusstructure.binarysearchtreeimpl.BSTStatusStructure;

abstract public class AbstractStatusStructure {
    
    /* ***************************************************** */
    // Static Entry Point
    
    public static AbstractStatusStructure createDefaultStatusStructure() {
        return new BSTStatusStructure();
        //return new DLinkedListStatusStructure();
    }
    
    /* ***************************************************** */
    // General Methods
    
    abstract public boolean isStatusStructureEmpty();
    
    abstract public void setRootNode( VSiteEvent siteevent );
    
    abstract public VLinkedNode insertNode( VLinkedNode nodetosplit, VSiteEvent siteevent );
    
    abstract public void removeNode(EventQueue eventqueue, VLinkedNode toremove);
    
    public VLinkedNode getNodeAboveSiteEvent( VSiteEvent siteevent , int sweepline ) {
        return getNodeAboveSiteEvent(siteevent.getX(), sweepline);
    }
    abstract public VLinkedNode getNodeAboveSiteEvent( int siteevent_x , int sweepline);
    
    abstract public VLinkedNode getHeadNode();
    
    /* ***************************************************** */
    // Debug print() Method
    
    public void print(Graphics2D g, VSiteEvent siteevent, int sweepline) {
        // Get clip bounds
        Rectangle bounds = g.getClipBounds();
        
        // Print straight line of y=sweepline
        g.drawLine(bounds.x, sweepline, bounds.x+bounds.width, sweepline);
        
        // Re-draw tree
        if ( this instanceof BSTStatusStructure && VoronoiTest.treedialog!=null ) {
            VoronoiTest.treedialog.setRootNode(((BSTStatusStructure)this).getRootNode(), sweepline);
        }
        
        // Print double linked list info
        if ( siteevent!=null ) {
            if ( this instanceof BSTStatusStructure ) {
                g.drawString(((BSTStatusStructure)this).strDoublyLinkedList(sweepline), 10, 10);
            }
            
            // Highlight leafnode above site
            VLinkedNode nodeabovesite = getNodeAboveSiteEvent(siteevent, sweepline);
            if ( nodeabovesite!=null ) {
                g.drawOval(nodeabovesite.siteevent.getX()-10,nodeabovesite.siteevent.getY()-10, 20, 20);
                int yintersect = sweepline + nodeabovesite.siteevent.getYValueOfParabola(siteevent.getX());
                g.drawLine( siteevent.getX(), siteevent.getY(), siteevent.getX(), yintersect );
            }
        }
        
        // Use the getLeafNodeAboveSiteEvent() rather than writing a more
        //  specialised/efficent method to verify its integrity
        for ( int x=bounds.x ; x<bounds.x+bounds.width ; x++ ) {
            VLinkedNode leafnode = getNodeAboveSiteEvent(x, sweepline);
            
            // Draw the point
            if ( leafnode!=null ) {
                leafnode.siteevent.calcParabolaConstants(sweepline);
                int ycoord = sweepline + leafnode.siteevent.getYValueOfParabola(x);
                g.fillRect(x, ycoord, 2, 2);
            }
        }
    }
    
    /* ***************************************************** */
}
