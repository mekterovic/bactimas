package signalprocesser.voronoi.representation.triangulation;
import java.util.ArrayList;

import signalprocesser.voronoi.VPoint;

@SuppressWarnings("serial")
public class VCluster extends ArrayList<VPoint> {
    
    public VPoint calculateAveragePoint() {
        VPoint average = new VPoint(0,0);
        for ( VPoint point : this ) {
            average.x += point.x;
            average.y += point.y;
        }
        average.x /= super.size();
        average.y /= super.size();
        return average;
    }
    
}
