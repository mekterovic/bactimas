package bactimas.alg;

import signalprocesser.voronoi.VPoint;


@SuppressWarnings("rawtypes")
public class ForcedPoint extends VPoint implements Comparable{
	int x, y;
	double distance;
	public ForcedPoint (int x, int y, VPoint endpoint) {
		super(x, y);
		distance = (endpoint == null) ? 0 : super.distanceTo(endpoint);
	}
	
	@Override
	public int compareTo(Object other) {
		
		return (new Double(distance)).compareTo(((ForcedPoint)other).distance);			
	}
	
    public String toString() {
        return "(" + super.toString() + ", d = " + distance;
    }
}
